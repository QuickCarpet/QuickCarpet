package quickcarpet.mixin.calmNetherFires;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    @Shadow
    private static int method_26155(Random random) {
        throw new AbstractMethodError();
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;method_26155(Ljava/util/Random;)I"))
    private int calmNetherFires(Random random, BlockState state, ServerWorld world, BlockPos pos) {
        int vanilla = method_26155(random);
        if (Settings.calmNetherFires <= 1) return vanilla;
        boolean infiniburn = world.getBlockState(pos.down()).isIn(world.getDimension().getInfiniburnBlocks());
        return infiniburn ? vanilla * Settings.calmNetherFires : vanilla;
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean doFireTick(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule, BlockState state, ServerWorld world, BlockPos pos) {
        if (!gameRules.getBoolean(rule)) return false;
        if (Settings.calmNetherFires != 0) return true;
        boolean infiniburn = world.getBlockState(pos.down()).isIn(world.getDimension().getInfiniburnBlocks());
        return !infiniburn;
    }
}
