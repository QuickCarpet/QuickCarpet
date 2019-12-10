package quickcarpet.mixin.piston;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.annotation.Feature;
import quickcarpet.client.ClientSetting;
import quickcarpet.settings.Settings;
import quickcarpet.utils.extensions.ExtendedPistonBlockEntity;
import quickcarpet.utils.extensions.ExtendedWorld;

@Feature("movableBlockEntities")
@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin extends BlockEntity implements ExtendedPistonBlockEntity {
    @Shadow private boolean source;
    @Shadow private BlockState pushedBlock;
    @Shadow private float progress;

    @Shadow private float lastProgress;
    private BlockEntity carriedBlockEntity;
    private boolean renderCarriedBlockEntity = false;
    private boolean renderSet = false;
    private float actualProgress;

    public PistonBlockEntityMixin(BlockEntityType<?> blockEntityType_1) {
        super(blockEntityType_1);
    }

    /**
     * @author 2No2Name
     */
    public BlockEntity getCarriedBlockEntity() {
        return carriedBlockEntity;
    }

    public void setCarriedBlockEntity(BlockEntity blockEntity) {
        this.carriedBlockEntity = blockEntity;
        if (this.carriedBlockEntity != null)
            this.carriedBlockEntity.setPos(this.pos); //actually this.pos is not initialized properly
    }

    public boolean isRenderModeSet() {
        return renderSet;
    }

    public boolean getRenderCarriedBlockEntity() {
        return renderCarriedBlockEntity;
    }

    public void setRenderCarriedBlockEntity(boolean b) {
        renderCarriedBlockEntity = b;
        renderSet = true;
    }

    /**
     * @author 2No2Name
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean movableTEsetBlockState0(World world, BlockPos blockPos_1, BlockState blockAState_2, int int_1) {
        if (!Settings.movableBlockEntities)
            return world.setBlockState(blockPos_1, blockAState_2, int_1);
        else
            return ((ExtendedWorld) (world)).setBlockStateWithBlockEntity(blockPos_1, blockAState_2, this.carriedBlockEntity, int_1);
    }

    @Redirect(method = "finish", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean movableTEsetBlockState1(World world, BlockPos blockPos_1, BlockState blockState_2, int int_1) {
        if (!Settings.movableBlockEntities)
            return world.setBlockState(blockPos_1, blockState_2, int_1);
        else {
            boolean ret = ((ExtendedWorld) (world)).setBlockStateWithBlockEntity(blockPos_1, blockState_2,
                    this.carriedBlockEntity, int_1);
            this.carriedBlockEntity = null; //this will cancel the finishHandleBroken
            return ret;
        }
    }

    @Inject(method = "finish", at = @At(value = "RETURN"))
    private void finishHandleBroken(CallbackInfo cir) {
        //Handle TNT Explosions or other ways the moving Block is broken
        //Also /setblock will cause this to be called, and drop e.g. a moving chest's contents.
        // This is MC-40380 (BlockEntities that aren't Inventories drop stuff when setblock is called )
        if (Settings.movableBlockEntities && this.carriedBlockEntity != null && this.world != null && !this.world.isClient &&
                this.world.getBlockState(this.pos).getBlock() == Blocks.AIR) {
            BlockState blockState_2;
            if (this.source)
                blockState_2 = Blocks.AIR.getDefaultState();
            else
                blockState_2 = Block.getRenderingState(this.pushedBlock, this.world, this.pos);
            ((ExtendedWorld) (this.world)).setBlockStateWithBlockEntity(this.pos, blockState_2, this.carriedBlockEntity, 3);
            this.world.breakBlock(this.pos, true);
        }
    }

    @Inject(method = "fromTag", at = @At(value = "TAIL"))
    private void onFromTag(CompoundTag compoundTag_1, CallbackInfo ci) {
        if (Settings.movableBlockEntities && compoundTag_1.contains("carriedTileEntity", 10)) {
            if (this.pushedBlock.getBlock() instanceof BlockEntityProvider)
                this.carriedBlockEntity = ((BlockEntityProvider) (this.pushedBlock.getBlock())).createBlockEntity(this.world);
            if (carriedBlockEntity != null) //Can actually be null, as BlockPistonMoving.createNewTileEntity(...) returns null
                this.carriedBlockEntity.fromTag(compoundTag_1.getCompound("carriedTileEntity"));
        }
        this.actualProgress = Math.max(0f, lastProgress - 0.5f);
    }

    @Inject(method = "toTag", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void onToTag(CompoundTag compoundTag_1, CallbackInfoReturnable<CompoundTag> cir) {
        if (Settings.movableBlockEntities && this.carriedBlockEntity != null) {
            //Leave name "carriedTileEntity" instead of "carriedBlockEntity" for upgrade compatibility with 1.12 movable TE
            compoundTag_1.put("carriedTileEntity", this.carriedBlockEntity.toTag(new CompoundTag()));
        }
    }

    @Feature("smoothPistons")
    @Inject(method = "getProgress", at = @At(value = "HEAD"), cancellable = true)
    private void smoothPistons(float partialTicks, CallbackInfoReturnable<Float> cir) {
        float val;
        if (this.world != null && this.world.isClient && ClientSetting.SMOOTH_PISTONS.get()) {
            val = (this.progress * 2.0F + partialTicks) * 0.33333334F;
            cir.setReturnValue(val);
        }
    }

    @Feature("smoothPistons")
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/PistonBlockEntity;progress:F", ordinal = 0))
    private void setActualProgress(CallbackInfo ci) {
        this.actualProgress = this.lastProgress;
    }
}
