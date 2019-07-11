package quickcarpet.mixin;

import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarpetProfiler;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Shadow public abstract ServerChunkManager method_14178();

    protected ServerWorldMixin(LevelProperties levelProperties_1, DimensionType dimensionType_1, BiFunction<World, Dimension, ChunkManager> biFunction_1, Profiler profiler_1, boolean boolean_1) {
        super(levelProperties_1, dimensionType_1, biFunction_1, profiler_1, boolean_1);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 0)
    )
    private void tickBlocks(ServerTickScheduler blockTickScheduler) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.BLOCKS);
        blockTickScheduler.tick();
        CarpetProfiler.endSection(this);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 1)
    )
    private void tickFluids(ServerTickScheduler fluidTickScheduler) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.FLUIDS);
        fluidTickScheduler.tick();
        CarpetProfiler.endSection(this);
    }

    @Inject(method = "tickChunk", at = @At("HEAD"))
    private void startTickChunk(WorldChunk worldChunk_1, int int_1, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.RANDOM_TICKS);
    }

    @Inject(method = "tickChunk", at = @At("TAIL"))
    private void endTickChunk(WorldChunk worldChunk_1, int int_1, CallbackInfo ci) {
        CarpetProfiler.endSection(this);
    }

    @Inject(
            method = "tick",
            at = @At(value = "CONSTANT", args = "stringValue=portalForcer")
    )
    private void startPortals(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.PORTALS);
    }

    @Inject(
            method = "tick",
            at = @At(value = "CONSTANT", args = "stringValue=raid")
    )
    private void endPortalsStartRaid(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(this);
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.VILLAGES);
    }

    @Inject(
            method = "tick",
            at = @At(value = "CONSTANT", args = "stringValue=blockEvents")
    )
    private void endRaidStartBlockEvents(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(this);
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.BLOCK_EVENTS);
    }

    @Inject(
            method = "tick",
            at = @At(value = "CONSTANT", args = "stringValue=entities")
    )
    private void endBlockEventsStartEntities(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(this);
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.ENTITIES);
    }

    @ModifyConstant(method = "setSpawnPos", constant = @Constant(intValue = 11), require = 2)
    private int adjustSpawnChunkLevel(int level) {
        return Settings.spawnChunkLevel;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickFreeze(BooleanSupplier shouldContinueTicking, CallbackInfo ci) {
        if (QuickCarpet.getInstance().tickSpeed.isPaused()) {
            for (ServerPlayerEntity p : this.players) p.tick();
            this.method_14178().tick(shouldContinueTicking);
            ci.cancel();
        }
    }

    @Redirect(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;getFluidState(III)Lnet/minecraft/fluid/FluidState;"))
    private FluidState optimizedFluidTick(ChunkSection chunkSection, int x, int y, int z) {
        if (Settings.optimizedFluidTicks && !chunkSection.hasRandomFluidTicks()) return Fluids.EMPTY.getDefaultState();
        return chunkSection.getFluidState(x, y, z);
    }
}
