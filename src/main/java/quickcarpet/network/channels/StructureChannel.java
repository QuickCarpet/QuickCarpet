package quickcarpet.network.channels;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.network.PacketSplitter;
import quickcarpet.network.PluginChannelHandler;

import java.util.*;

public class StructureChannel implements PluginChannelHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int RESEND_TIMEOUT = 30 * 20;
    public static final Identifier CHANNEL = new Identifier("carpet:structures");
    public static final int VERSION = 1;

    public static final int PACKET_S2C_DATA = 0;

    public static StructureChannel instance;

    private final CompoundTag metadata = new CompoundTag();
    private Map<ServerPlayerEntity, Object2IntMap<ChunkPos>> playerMap = new WeakHashMap<>();

    public StructureChannel() {
        instance = this;
        metadata.putInt("Version", VERSION);
        metadata.putInt("Timeout", RESEND_TIMEOUT);
    }

    @Override
    public Identifier[] getChannels() {
        return new Identifier[] {CHANNEL};
    }

    @Override
    public synchronized boolean register(Identifier channel, ServerPlayerEntity player) {
        sendData(player, metadata);
        playerMap.put(player, new Object2IntOpenHashMap<>());
        int viewDistance = player.getServer().getPlayerManager().getViewDistance();
        ChunkPos playerPos = player.getCameraPosition().toChunkPos();
        ServerWorld world = player.getServerWorld();
        for (int x = playerPos.x - viewDistance; x <= playerPos.x + viewDistance; x++) {
            for (int z = playerPos.z - viewDistance; z <= playerPos.z + viewDistance; z++) {
                if (!world.isChunkLoaded(x, z)) continue;
                recordChunkSent(player, new ChunkPos(x, z));
            }
        }
        return true;
    }

    @Override
    public synchronized void unregister(Identifier channel, ServerPlayerEntity player) {
        playerMap.remove(player);
    }

    public synchronized void tick() {
        for (Map.Entry<ServerPlayerEntity, Object2IntMap<ChunkPos>> playerEntry : playerMap.entrySet()) {
            List<ChunkPos> updated = new ArrayList<>();
            List<ChunkPos> removed = new ArrayList<>();
            Object2IntMap<ChunkPos> chunks = playerEntry.getValue();
            for (Object2IntMap.Entry<ChunkPos> chunkEntry : chunks.object2IntEntrySet()) {
                int age = chunkEntry.getIntValue();
                if (age == 0) updated.add(chunkEntry.getKey());
                chunkEntry.setValue(++age);
                if (age > RESEND_TIMEOUT) removed.add(chunkEntry.getKey());
            }
            for (ChunkPos pos : removed) chunks.removeInt(pos);
            if (!updated.isEmpty()) {
                try {
                    sendUpdate(playerEntry.getKey(), updated);
                } catch (Exception e) {
                    LOGGER.error("Error sending structures to " + playerEntry.getKey().getEntityName(), e);
                }
            }
        }
    }

    private void sendUpdate(ServerPlayerEntity player, Collection<ChunkPos> chunks) {
        World world = player.getServerWorld();
        Map<String, LongSet> references = new HashMap<>();
        for (ChunkPos pos : chunks) {
            if (!world.isChunkLoaded(pos.x, pos.z)) continue;
            Chunk chunk = world.getChunk(pos.x, pos.z);
            for (Map.Entry<String, LongSet> e : chunk.getStructureReferences().entrySet()) {
                references.merge(e.getKey(), e.getValue(), (a, b) -> {
                    LongSet c = new LongOpenHashSet(a);
                    c.addAll(b);
                    return c;
                });
            }
        }
        ListTag starts = new ListTag();
        Object2IntMap<ChunkPos> chunkMap = playerMap.get(player);
        for (Map.Entry<String, LongSet> ref : references.entrySet()) {
            for (long pos : ref.getValue()) {
                ChunkPos chunkPos = new ChunkPos(pos);
                if (chunkMap.computeIntIfAbsent(chunkPos, c -> 1) > 1) continue;
                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
                starts.add(chunk.getStructureStart(ref.getKey()).toTag(chunkPos.x, chunkPos.z));
            }
        }
        CompoundTag data = new CompoundTag();
        data.put("Structures", starts);
        sendData(player, data);
    }

    private void sendData(ServerPlayerEntity player, CompoundTag data) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(PACKET_S2C_DATA);
        buf.writeCompoundTag(data);
        // LOGGER.info(data);
        PacketSplitter.send(player.networkHandler, CHANNEL, buf);
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {

    }

    public synchronized void recordChunkSent(ServerPlayerEntity player, ChunkPos pos) {
        if (!playerMap.containsKey(player)) return;
        playerMap.get(player).putIfAbsent(pos, 0);
    }
}
