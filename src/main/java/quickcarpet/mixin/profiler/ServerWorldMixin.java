package quickcarpet.mixin.profiler;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.CarpetProfiler;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

@Feature("profiler")
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient) {
        super(levelProperties, dimensionType, chunkManagerProvider, profiler, isClient);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 0)
    )
    private void tickBlocks(ServerTickScheduler<Block> blockTickScheduler) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.BLOCKS);
        blockTickScheduler.tick();
        CarpetProfiler.endSection(this);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 1)
    )
    private void tickFluids(ServerTickScheduler<Fluid> fluidTickScheduler) {
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

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=raid"))
    private void startRaid(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.VILLAGES);
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=blockEvents"))
    private void endRaidStartBlockEvents(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(this);
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.BLOCK_EVENTS);
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=entities"))
    private void endBlockEventsStartEntities(BooleanSupplier booleanSupplier_1, CallbackInfo ci) {
        CarpetProfiler.endSection(this);
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.ENTITIES);
    }
}
