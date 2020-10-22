package quickcarpet.network.impl;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import quickcarpet.api.network.server.ServerPluginChannelHandler;
import quickcarpet.api.network.server.ServerPluginChannelManager;
import quickcarpet.mixin.accessor.CustomPayloadC2SPacketAccessor;

import java.util.*;
import java.util.stream.Collectors;

public class PluginChannelManager implements ServerPluginChannelManager {
    private final MinecraftServer server;
    public final PluginChannelTracker tracker;
    private Map<Identifier, ServerPluginChannelHandler> channelHandlers = new HashMap<>();

    public PluginChannelManager(MinecraftServer server) {
        this.server = server;
        this.tracker = new PluginChannelTracker(server);
    }

    @Override
    public void register(ServerPluginChannelHandler handler) {
        Identifier[] channels = handler.getChannels();
        for (Identifier channel : channels) {
            channelHandlers.put(channel, handler);
        }
        PlayerManager playerList = server.getPlayerManager();
        // make sure server started up
        if (playerList != null) {
            sendChannelUpdate(playerList.getPlayerList(), REGISTER, Arrays.asList(channels));
        }
    }

    @Override
    public void unregister(ServerPluginChannelHandler handler) {
        Identifier[] channels = handler.getChannels();
        for (Identifier channel : channels) {
            for (ServerPlayerEntity player : tracker.getPlayers(channel)) {
                handler.unregister(channel, player);
                tracker.unregister(player, channel);
            }
            channelHandlers.remove(channel);
        }
        sendChannelUpdate(server.getPlayerManager().getPlayerList(), UNREGISTER, Arrays.asList(channels));
    }

    @Override
    public void process(ServerPlayerEntity player, CustomPayloadC2SPacket packet) {
        CustomPayloadC2SPacketAccessor packetAccessor = (CustomPayloadC2SPacketAccessor) packet;
        Identifier channel = packetAccessor.getChannel();
        PacketByteBuf payload = packetAccessor.getData();
        switch(channel.toString()) {
            case "minecraft:register": {
                this.processRegister(player, payload);
                return;
            }
            case "minecraft:unregister": {
                this.processUnregister(player, payload);
                return;
            }
        }
        ServerPluginChannelHandler handler = channelHandlers.get(channel);
        if (handler != null) {
            handler.onCustomPayload(packet, player);
        }
    }

    private void processRegister(ServerPlayerEntity player, PacketByteBuf payload) {
        List<Identifier> channels = getChannels(payload);
        for (Identifier channel : channels) {
            ServerPluginChannelHandler handler = channelHandlers.get(channel);
            if (handler != null && handler.register(channel, player)) {
                tracker.register(player, channel);
            }
        }
    }

    private void processUnregister(ServerPlayerEntity player, PacketByteBuf payload) {
        List<Identifier> channels = getChannels(payload);
        for (Identifier channel : channels) {
            if (!tracker.isRegistered(player, channel)) continue;
            ServerPluginChannelHandler handler = channelHandlers.get(channel);
            if (handler != null) handler.unregister(channel, player);
            tracker.unregister(player, channel);
        }
    }

    @Override
    public void onPlayerConnect(ServerPlayerEntity player) {
        sendChannelUpdate(Collections.singleton(player), REGISTER, channelHandlers.keySet());
    }

    @Override
    public void onPlayerDisconnect(ServerPlayerEntity player) {
        for (Map.Entry<Identifier, ServerPluginChannelHandler> handler : channelHandlers.entrySet()) {
            handler.getValue().unregister(handler.getKey(), player);
        }
        tracker.unregisterAll(player);
    }

    private void sendChannelUpdate(Collection<ServerPlayerEntity> players, Identifier updateType, Collection<Identifier> channels) {
        if (players.isEmpty()) return;
        String joinedChannels = channels.stream().map(Identifier::toString).collect(Collectors.joining("\0"));
        ByteBuf payload = Unpooled.wrappedBuffer(joinedChannels.getBytes(Charsets.UTF_8));
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(updateType, new PacketByteBuf(payload));
        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(packet);
        }
    }

    private static List<Identifier> getChannels(PacketByteBuf buff) {
        buff.resetReaderIndex();
        byte[] bytes = new byte[buff.readableBytes()];
        buff.readBytes(bytes);
        String channelString = new String(bytes, Charsets.UTF_8);
        return Arrays.stream(channelString.split("\0")).map(Identifier::new).collect(Collectors.toList());
    }
}
