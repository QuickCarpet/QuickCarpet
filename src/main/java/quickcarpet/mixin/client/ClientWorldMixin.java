package quickcarpet.mixin.client;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;
import quickcarpet.annotation.Feature;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

@Feature("tickSpeed")
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
    protected ClientWorldMixin(LevelProperties properties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> biFunction_1, Profiler profiler, boolean boolean_1) {
        super(properties, dimensionType, biFunction_1, profiler, boolean_1);
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
