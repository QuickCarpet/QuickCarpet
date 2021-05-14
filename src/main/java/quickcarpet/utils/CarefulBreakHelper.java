package quickcarpet.utils;

import net.minecraft.server.network.ServerPlayerEntity;

public final class CarefulBreakHelper {
    private CarefulBreakHelper() {}

    public static ThreadLocal<ServerPlayerEntity> miningPlayer = new ThreadLocal<>();
}
