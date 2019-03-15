package quickcarpet.mixin;

import net.minecraft.class_1419;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.PortalForcer;
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
import quickcarpet.helper.TickSpeed;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

//@Mixin(ServerWorld.class)
public abstract class MixinServerWorld //extends World
{

    /*
    @Shadow
    @Final
    protected class_1419 field_13958;
    @Shadow
    private boolean field_13953;
    @Shadow
    @Final
    private PortalForcer portalForcer;

    protected MixinServerWorld(LevelProperties levelProperties_1, DimensionType dimensionType_1, BiFunction<World, Dimension, ChunkManager> biFunction_1, Profiler profiler_1, boolean boolean_1) {
        super(levelProperties_1, dimensionType_1, biFunction_1, profiler_1, boolean_1);
    }

    @Shadow
    public abstract boolean method_14172();

    @Shadow
    protected abstract void method_14200();

    @Shadow
    public abstract void tickScheduledTicks();

    @Shadow
    protected abstract void sendBlockActions();

    // Adding TickSpeed parameter to if statement
    @Redirect(method = "method_8429", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;invalid:Z", ordinal = 2))
    private boolean isProcessEntities1(Entity entity_2) {
        return !entity_2.invalid && TickSpeed.process_entities;
    }

    // Adding TickSpeed parameter to if statement
    @Redirect(method = "method_8429", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;)Z", ordinal = 0))
    private boolean isProcessEntities2(ServerWorld serverWorld, BlockPos blockPos_1) {
        return this.isBlockLoaded(blockPos_1) && TickSpeed.process_entities;
    }
    */

}
