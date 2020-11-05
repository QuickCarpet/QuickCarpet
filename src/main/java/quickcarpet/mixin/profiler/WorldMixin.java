package quickcarpet.mixin.profiler;

import net.minecraft.class_5562;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.api.annotation.Feature;
import quickcarpet.utils.CarpetProfiler;

import java.util.function.Consumer;

@Feature("profiler")
@Mixin(World.class)
public class WorldMixin {
    @Shadow @Final public boolean isClient;

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    private void startBlockEntities(CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endSection((World) (Object) this); // end entities
            CarpetProfiler.startSection((World) (Object) this, CarpetProfiler.SectionType.BLOCK_ENTITIES);
        }
    }

    @Inject(method = "tickBlockEntities", at = @At("TAIL"))
    private void endBlockEntities(CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endSection((World) (Object) this);
        }
    }

    @Redirect(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_5562;method_31703()V")
    )
    private void tickBlockEntity(class_5562 tickable) {
        if (!this.isClient) {
            CarpetProfiler.startBlockEntity((World) (Object) this, tickable);
            tickable.method_31703();
            CarpetProfiler.endBlockEntity((World) (Object) this);
        } else {
            tickable.method_31703();
        }
    }

    @Inject(method = "tickEntity", at = @At("HEAD"))
    private void startEntity(Consumer<Entity> tick, Entity e, CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.startEntity((World) (Object) this, e);
        }
    }

    @Inject(method = "tickEntity", at = @At("TAIL"))
    private void endEntity(Consumer<Entity> tick, Entity e, CallbackInfo ci) {
        if (!this.isClient) {
            CarpetProfiler.endEntity((World) (Object) this);
        }
    }
}
