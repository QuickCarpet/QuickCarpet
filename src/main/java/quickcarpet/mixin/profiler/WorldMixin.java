package quickcarpet.mixin.profiler;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.utils.CarpetProfiler;

import java.util.function.Consumer;

@Mixin(World.class)
public class WorldMixin {
    @Shadow @Final public boolean isClient;

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    private void quickcarpet$profiler$startBlockEntities(CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endSection((World) (Object) this, CarpetProfiler.SectionType.ENTITIES);
            CarpetProfiler.startSection((World) (Object) this, CarpetProfiler.SectionType.BLOCK_ENTITIES);
        }
    }

    @Inject(method = "tickBlockEntities", at = @At("TAIL"))
    private void quickcarpet$profiler$endBlockEntities(CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endSection((World) (Object) this, CarpetProfiler.SectionType.BLOCK_ENTITIES);
        }
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
    private void quickcarpet$profiler$tickBlockEntity(BlockEntityTickInvoker tickable) {
        if (!this.isClient) {
            CarpetProfiler.startBlockEntity((World) (Object) this, tickable);
            tickable.tick();
            CarpetProfiler.endBlockEntity((World) (Object) this);
        } else {
            tickable.tick();
        }
    }

    @Inject(method = "tickEntity", at = @At("HEAD"))
    private void quickcarpet$profiler$startEntity(Consumer<Entity> tick, Entity e, CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.startEntity((World) (Object) this, e);
        }
    }

    @Inject(method = "tickEntity", at = @At("TAIL"))
    private void quickcarpet$profiler$endEntity(Consumer<Entity> tick, Entity e, CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endEntity((World) (Object) this);
        }
    }
}
