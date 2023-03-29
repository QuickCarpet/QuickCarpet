package quickcarpet.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpetClient;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$tickFreeze(BooleanSupplier shouldContinueTicking, CallbackInfo ci) {
        if (QuickCarpetClient.getInstance().tickSpeed.isPaused()) ci.cancel();
    }

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$tickFreezeEntities(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity) && QuickCarpetClient.getInstance().tickSpeed.isPaused()) {
            ci.cancel();
        }
    }
}
