package quickcarpet.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.mixin.accessor.CustomPayloadC2SPacketAccessor;

import java.util.*;
import java.util.stream.Collectors;

public class PluginChannelManager {
    public static final Identifier REGISTER = new Identifier("minecraft:register");
    public static final Identifier UNREGISTER = new Identifier("minecraft:unregister");
    public static final Logger LOG = LogManager.getLogger();

    private final MinecraftServer server;
    public final PluginChannelTracker tracker;
    private Map<Identifier, PluginChannelHandler> channelHandlers = new HashMap<>();

    public PluginChannelManager(MinecraftServer server) {
        this.server = server;
        this.tracker = new PluginChannelTracker(server);
    }

    public void register(PluginChannelHandler handler) {
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

    public void unregister(PluginChannelHandler handler) {
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
        PluginChannelHandler handler = channelHandlers.get(channel);
        if (handler != null) {
            handler.onCustomPayload(packet, player);
        }
    }

    private void processRegister(ServerPlayerEntity player, PacketByteBuf payload) {
        List<Identifier> channels = getChannels(payload);
        for (Identifier channel : channels) {
            PluginChannelHandler handler = channelHandlers.get(channel);
            if (handler != null && handler.register(channel, player)) {
                tracker.register(player, channel);
            }
        }
    }

    private void processUnregister(ServerPlayerEntity player, PacketByteBuf payload) {
        List<Identifier> channels = getChannels(payload);
        for (Identifier channel : channels) {
            if (!tracker.isRegistered(player, channel)) continue;
            PluginChannelHandler handler = channelHandlers.get(channel);
            if (handler != null) handler.unregister(channel, player);
            tracker.unregister(player, channel);
        }
    }

    public void onPlayerConnect(ServerPlayerEntity player) {
        sendChannelUpdate(Collections.singleton(player), REGISTER, channelHandlers.keySet());
    }

    public void onPlayerDisconnect(ServerPlayerEntity player) {
        for (Identifier channel : tracker.getChannels(player)) {
            PluginChannelHandler handler = channelHandlers.get(channel);
            if (handler == null) {
                LOG.warn("Player was registered to channel {} without a handler", channel);
                continue;
            }
            handler.unregister(channel, player);
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
