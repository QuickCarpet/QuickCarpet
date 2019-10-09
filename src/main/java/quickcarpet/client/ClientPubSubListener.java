package quickcarpet.client;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.QuickCarpet;
import quickcarpet.QuickCarpetClient;
import quickcarpet.network.ClientPluginChannelHandler;
import quickcarpet.network.PacketSplitter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static quickcarpet.pubsub.PubSubMessenger.*;

public class ClientPubSubListener implements ClientPluginChannelHandler {
    private Logger LOG = LogManager.getLogger();
    private QuickCarpetClient client = QuickCarpet.getInstance().client;

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
            switch (valueType) {
                case TYPE_NBT: {
                    CompoundTag compound = buf.readCompoundTag();
                    if (compound == null) break;
                    if (compound.contains("")) {
                        values.put(name, compound.get(""));
                    } else {
                        values.put(name, compound);
                    }
                    break;
                }
                case TYPE_STRING: {
                    values.put(name, buf.readString());
                    break;
                }
                case TYPE_INT: {
                    values.put(name, buf.readInt());
                    break;
                }
                case TYPE_FLOAT: {
                    values.put(name, buf.readFloat());
                    break;
                }
                case TYPE_LONG: {
                    values.put(name, buf.readLong());
                    break;
                }
                case TYPE_DOUBLE: {
                    values.put(name, buf.readDouble());
                    break;
                }
                case TYPE_BOOLEAN: {
                    values.put(name, buf.readBoolean());
                    break;
                }
                default: {
                    LOG.warn("Could not parse pubsub update {} of type {}", name, valueType);
                    return values;
                }
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
