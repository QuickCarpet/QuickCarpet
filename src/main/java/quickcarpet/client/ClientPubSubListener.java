package quickcarpet.client;

import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import quickcarpet.QuickCarpetClient;
import quickcarpet.api.network.client.ClientPluginChannelHandler;
import quickcarpet.network.impl.PacketSplitter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static quickcarpet.pubsub.PubSubMessenger.*;

@Environment(EnvType.CLIENT)
public class ClientPubSubListener implements ClientPluginChannelHandler {
    private final Logger LOGGER = LogUtils.getLogger();
    private final QuickCarpetClient client = QuickCarpetClient.getInstance();

    @Override
    public Identifier[] getChannels() {
        return new Identifier[] {CHANNEL_NAME};
    }

    @Override
    public void onCustomPayload(CustomPayloadS2CPacket packet, ClientPlayPacketListener netHandler) {
        PacketByteBuf buf = PacketSplitter.receive(netHandler, packet);
        if (buf == null) return;
        int type = buf.readVarInt();
        if (type == PACKET_S2C_UPDATE) {
            Map<String, Object> updates = parseUpdatePacket(buf);
            if (!onUpdate(updates, "minecraft.performance.tps", Number.class, tps -> client.tickSpeed.setTickRateGoal(tps.floatValue()))) {
                onUpdate(updates, "carpet.tick-rate.tps-goal", Number.class, goal -> client.tickSpeed.setTickRateGoal(goal.floatValue()));
            }
            onUpdate(updates, "carpet.tick-rate.paused", Boolean.class, paused -> client.tickSpeed.setPaused(paused));
            onUpdate(updates, "carpet.tick-rate.step", Number.class, step -> client.tickSpeed.setStepAmount(step.intValue()));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean onUpdate(Map<String, Object> updates, String key, Class<T> type, Consumer<T> action) {
        Object obj = updates.get(key);
        if (!type.isInstance(obj)) return false;
        action.accept((T) obj);
        return true;
    }

    private Map<String, Object> parseUpdatePacket(PacketByteBuf buf) {
        int numUpdates = buf.readVarInt();
        Map<String, Object> values = new LinkedHashMap<>(numUpdates);
        for (int i = 0; i < numUpdates; i++) {
            String name = buf.readString();
            int valueType = buf.readVarInt();
            try {
                values.put(name, switch (valueType) {
                    case TYPE_NBT -> {
                        NbtCompound compound = buf.readNbt();
                        yield compound != null && compound.contains("") ? compound.get("") : compound;
                    }
                    case TYPE_STRING -> buf.readString();
                    case TYPE_INT -> buf.readInt();
                    case TYPE_FLOAT -> buf.readFloat();
                    case TYPE_LONG -> buf.readLong();
                    case TYPE_DOUBLE -> buf.readDouble();
                    case TYPE_BOOLEAN -> buf.readBoolean();
                    default -> throw new IllegalArgumentException();
                });
            } catch (IllegalArgumentException ignored) {
                LOGGER.warn("Could not parse pubsub update {} of type {}", name, valueType);
                return values;
            }
        }
        return values;
    }

    public void subscribe(String ...nodes) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(PACKET_C2S_SUBSCRIBE);
        buf.writeVarInt(nodes.length);
        for (String node : nodes) buf.writeString(node);
        PacketSplitter.send(MinecraftClient.getInstance().getNetworkHandler(), CHANNEL_NAME, buf);
    }

    public void unsubscribe(String ...nodes) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(PACKET_C2S_UNSUBSCRIBE);
        buf.writeVarInt(nodes.length);
        for (String node : nodes) buf.writeString(node);
        PacketSplitter.send(MinecraftClient.getInstance().getNetworkHandler(), CHANNEL_NAME, buf);
    }
}
