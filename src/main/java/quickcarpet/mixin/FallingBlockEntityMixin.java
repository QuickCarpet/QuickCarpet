package quickcarpet.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    public FallingBlockEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 1,
            target = "Lnet/minecraft/entity/FallingBlockEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void onTick(CallbackInfo ci, Block block_1, BlockPos blockPos_2, boolean boolean_1, boolean boolean_2, BlockState blockState_1) {
        if (block_1.matches(BlockTags.ANVIL) && Settings.renewableSand
                && this.world.getBlockState(new BlockPos(this.x, this.y - 0.059999999776482582D, this.z)).getBlock() == Blocks.COBBLESTONE) {
            world.breakBlock(blockPos_2.down(), false);
            world.setBlockState(blockPos_2.down(), Blocks.SAND.getDefaultState(), 3);
        }
    }
}
