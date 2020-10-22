package quickcarpet.mixin.renewableFromAnvil;

import net.minecraft.block.Block;
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
import quickcarpet.api.annotation.Feature;
import quickcarpet.settings.Settings;

@Feature("renewableSand")
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    public FallingBlockEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    private int frostedIceCount;
    private int iceCount;
    private int packedIceCount;

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 1,
            target = "Lnet/minecraft/entity/FallingBlockEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void onTick(CallbackInfo ci, Block block, BlockPos pos) {
        if (block.isIn(BlockTags.ANVIL)) {
            BlockPos posBelow = new BlockPos(this.getX(), this.getY() - 0.06, this.getZ());
            Block blockBelow = this.world.getBlockState(posBelow).getBlock();
            if (blockBelow == Blocks.COBBLESTONE) {
                if (Settings.renewableSand == Settings.RenewableGravelOrSandOption.ANVIL) {
                    world.breakBlock(posBelow, false);
                    world.setBlockState(posBelow, Blocks.SAND.getDefaultState(), 3);
                } else if (Settings.renewableGravel == Settings.RenewableGravelOrSandOption.ANVIL) {
                    world.breakBlock(posBelow, false);
                    world.setBlockState(posBelow, Blocks.GRAVEL.getDefaultState(), 3);
                }
            } else if (blockBelow == Blocks.FROSTED_ICE && Settings.anvilledIce > 0) {
                if (++frostedIceCount < Settings.anvilledIce) {
                    world.breakBlock(posBelow, false);
                    onGround = false;
                    ci.cancel();
                } else {
                    world.breakBlock(posBelow, false);
                    world.setBlockState(posBelow, Blocks.ICE.getDefaultState(), 3);
                }
            } else if (blockBelow == Blocks.ICE && Settings.anvilledPackedIce > 0) {
                if (++iceCount < Settings.anvilledPackedIce) {
                    world.breakBlock(posBelow, false);
                    onGround = false;
                    ci.cancel();
                } else {
                    world.breakBlock(posBelow, false);
                    world.setBlockState(posBelow, Blocks.PACKED_ICE.getDefaultState(), 3);
                }
            } else if (blockBelow == Blocks.PACKED_ICE && Settings.anvilledBlueIce > 0) {
                if (++packedIceCount < Settings.anvilledBlueIce) {
                    world.breakBlock(posBelow, false);
                    onGround = false;
                    ci.cancel();
                } else {
                    world.breakBlock(posBelow, false);
                    world.setBlockState(posBelow, Blocks.BLUE_ICE.getDefaultState(), 3);
                }
            }
        }
    }
}
