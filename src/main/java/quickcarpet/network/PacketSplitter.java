package quickcarpet.network;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.apache.commons.lang3.tuple.Pair;
import quickcarpet.mixin.ICustomPayloadC2SPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PacketSplitter {
    public static final int MAX_TOTAL_PER_PACKET_S2C = 1048576;
    public static final int MAX_PAYLOAD_PER_PACKET_S2C = MAX_TOTAL_PER_PACKET_S2C - 5;
    public static final int MAX_TOTAL_PER_PACKET_C2S = 32767;
    public static final int MAX_PAYLOAD_PER_PACKET_C2S = MAX_TOTAL_PER_PACKET_C2S - 5;
    public static final int DEFAULT_MAX_RECEIVE_SIZE = 104876;

    private static final Map<Pair<ServerPlayerEntity, Identifier>, ReadingSession> readingSessions = new HashMap<>();

    public static void send(ServerPlayerEntity player, Identifier channel, PacketByteBuf packet) {
        send(packet, MAX_PAYLOAD_PER_PACKET_S2C, buf -> player.networkHandler.sendPacket(new CustomPayloadS2CPacket(channel, buf)));
    }

    public static void send(PacketByteBuf packet, int payloadLimit, Consumer<PacketByteBuf> sender) {
        int len = packet.writerIndex();
        packet.resetReaderIndex();
        for (int offset = 0; offset < len; offset += payloadLimit) {
            int thisLen = Math.min(len - offset, payloadLimit);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer(thisLen));
            buf.resetWriterIndex();
            if (offset == 0) buf.writeVarInt(len);
            buf.writeBytes(packet, thisLen);
            sender.accept(buf);
        }
        packet.release();
    }

    public static PacketByteBuf receive(ServerPlayerEntity player, CustomPayloadC2SPacket message) {
        return receive(player, message, DEFAULT_MAX_RECEIVE_SIZE);
    }

    public static PacketByteBuf receive(ServerPlayerEntity player, CustomPayloadC2SPacket message, int maxLength) {
        ICustomPayloadC2SPacket messageAccessor = (ICustomPayloadC2SPacket) message;
        Pair<ServerPlayerEntity, Identifier> key = Pair.of(player, messageAccessor.getChannel());
        return readingSessions.computeIfAbsent(key, ReadingSession::new).receive(messageAccessor.getData(), maxLength);
    }

    private static class ReadingSession {
        private final Pair<ServerPlayerEntity, Identifier> key;
        private int expectedSize = -1;
        private PacketByteBuf received;
        private ReadingSession(Pair<ServerPlayerEntity, Identifier> key) {
            this.key = key;
        }

        private PacketByteBuf receive(PacketByteBuf data, int maxLength) {
            if (expectedSize < 0) {
                expectedSize = data.readVarInt();
                if (expectedSize > maxLength) throw new IllegalArgumentException("Payload too large");
                received = new PacketByteBuf(Unpooled.buffer(expectedSize));
            }
            received.writeBytes(data.readBytes(data.readableBytes()));
            if (received.writerIndex() >= expectedSize) {
                readingSessions.remove(key);
                return received;
            }
            return null;
        }
    }
}
