package quickcarpet.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.Feature;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Feature("tickSpeed")
@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
    protected ClientWorldMixin(MutableWorldProperties mutableWorldProperties, DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean bl2, long l) {
        super(mutableWorldProperties, dimensionType, supplier, bl, bl2, l);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickFreeze(BooleanSupplier shouldContinueTicking, CallbackInfo ci) {
        if (QuickCarpet.getInstance().client.tickSpeed.isPaused()) ci.cancel();
    }

    @Redirect(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager;shouldTickEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean tickFreezeEntities(ClientChunkManager clientChunkManager, Entity entity) {
        if (QuickCarpet.getInstance().client.tickSpeed.isPaused()) {
            return false;
        }
        return clientChunkManager.shouldTickEntity(entity);
    }
}
