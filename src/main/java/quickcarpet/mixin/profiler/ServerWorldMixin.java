package quickcarpet.mixin.profiler;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
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
    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l) {
        super(properties, registryKey, dimensionType, supplier, bl, bl2, l);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 0))
    private void startTickBlocks(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.BLOCKS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 0, shift = At.Shift.AFTER))
    private void endTickBlocks(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.BLOCKS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 1))
    private void startTickFluids(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.FLUIDS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 1, shift = At.Shift.AFTER))
    private void endTickFluids(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.FLUIDS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/raid/RaidManager;tick()V"))
    private void startRaid(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.RAIDS);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/raid/RaidManager;tick()V", shift = At.Shift.AFTER))
    private void endRaid(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.RAIDS);
    }

    @Inject(method = "tickChunk", at = @At("HEAD"))
    private void startTickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.RANDOM_TICKS);
    }

    @Inject(method = "tickChunk", at = @At("RETURN"))
    private void endTickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.RANDOM_TICKS);
    }

    @Inject(method = "processSyncedBlockEvents", at = @At("HEAD"))
    private void startBlockEvents(CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.BLOCK_EVENTS);
    }

    @Inject(method = "processSyncedBlockEvents", at = @At("RETURN"))
    private void endBlockEvents(CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.BLOCK_EVENTS);
    }

    @Inject(method = "tick", at = @At(value = "CONSTANT", args = "stringValue=entities"))
    private void startEntities(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.ENTITIES);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerEntityManager;tick()V"))
    private void startEntityManager(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.startSection(this, CarpetProfiler.SectionType.ENTITY_MANAGER);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerEntityManager;tick()V", shift = At.Shift.AFTER))
    private void endEntityManager(BooleanSupplier b, CallbackInfo ci) {
        CarpetProfiler.endSection(this, CarpetProfiler.SectionType.ENTITY_MANAGER);
    }
}
