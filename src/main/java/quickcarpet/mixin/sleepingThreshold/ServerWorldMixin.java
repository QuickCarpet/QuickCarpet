package quickcarpet.mixin.sleepingThreshold;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Feature("sleepingThreshold")
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    @Shadow
    @Final
    private List<ServerPlayerEntity> players;
    @Shadow private boolean allPlayersSleeping;

    protected ServerWorldMixin(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient) {
        super(levelProperties, dimensionType, chunkManagerProvider, profiler, isClient);
        throw new AbstractMethodError();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z", remap = false))
    private boolean testSleepingLongEnough(Stream<ServerPlayerEntity> stream, Predicate<ServerPlayerEntity> predicate) {
        return arePlayersSleeping(ServerPlayerEntity::isSleepingLongEnough);
    }

    /**
     * @author skyrising
     * @reason Whole method is changed anyway
     */
    @Overwrite
    public void updatePlayersSleeping() {
        this.allPlayersSleeping = arePlayersSleeping(ServerPlayerEntity::isSleeping);
    }

    private boolean arePlayersSleeping(Predicate<ServerPlayerEntity> condition) {
        int nonSpectators = 0;
        int sleeping = 0;
        for (ServerPlayerEntity player : this.players) {
            if (player.isSpectator()) continue;
            nonSpectators++;
            if (condition.test(player)) sleeping++;
        }
        if (sleeping == 0) return false;
        return sleeping * 100 >= nonSpectators * Settings.sleepingThreshold;
    }
}
