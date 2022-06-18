package quickcarpet.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public final class ThreadLocals {
    private ThreadLocals() {}

    public static final ThreadLocal<ServerPlayerEntity> miningPlayer = new ThreadLocal<>();
    public static final ThreadLocal<Collection<BlockPos>> movedBlocks = new ThreadLocal<>();
}
