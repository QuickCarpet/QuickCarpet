package quickcarpet.mixin.profiler;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.CarpetProfiler;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V", ordinal = 0))
    private void quickcarpet$profiler$startTickBlocks(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.BLOCKS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void quickcarpet$profiler$endTickBlocks(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.BLOCKS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V", ordinal = 1))
    private void quickcarpet$profiler$startTickFluids(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.FLUIDS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void quickcarpet$profiler$endTickFluids(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.FLUIDS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/raid/RaidManager;tick()V"))
    private void quickcarpet$profiler$startRaid(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.RAIDS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/raid/RaidManager;tick()V", shift = At.Shift.AFTER))
    private void quickcarpet$profiler$endRaid(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.RAIDS);
    }

    @Inject(method = "tickChunk", at = @At("HEAD"))
    private void quickcarpet$profiler$startTickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.RANDOM_TICKS);
    }

    @Inject(method = "tickChunk", at = @At("RETURN"))
    private void quickcarpet$profiler$endTickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.RANDOM_TICKS);
    }

    @Inject(method = "processSyncedBlockEvents", at = @At("HEAD"))
    private void quickcarpet$profiler$startBlockEvents(CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.BLOCK_EVENTS);
    }

    @Inject(method = "processSyncedBlockEvents", at = @At("RETURN"))
    private void quickcarpet$profiler$endBlockEvents(CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.BLOCK_EVENTS);
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=entities"))
    private void quickcarpet$profiler$startEntities(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.ENTITIES);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerEntityManager;tick()V"))
    private void quickcarpet$profiler$startEntityManager(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.ENTITY_MANAGER);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerEntityManager;tick()V", shift = At.Shift.AFTER))
    private void quickcarpet$profiler$endEntityManager(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.ENTITY_MANAGER);
    }
}
