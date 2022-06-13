package quickcarpet.mixin.movableBlockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.mixin.accessor.BlockEntityAccessor;
import quickcarpet.settings.Settings;
import quickcarpet.utils.mixin.extensions.ExtendedPistonBlockEntity;
import quickcarpet.utils.mixin.extensions.ExtendedWorld;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin extends BlockEntity implements ExtendedPistonBlockEntity {
    @Shadow private boolean source;
    @Shadow private BlockState pushedBlock;
    private BlockEntity carriedBlockEntity;
    private boolean renderCarriedBlockEntity = false;
    private boolean renderSet = false;

    public PistonBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (carriedBlockEntity != null) carriedBlockEntity.setWorld(world);
    }

    /**
     * @author 2No2Name
     */
    @Override
    public BlockEntity quickcarpet$getCarriedBlockEntity() {
        return carriedBlockEntity;
    }

    @Override
    public void quickcarpet$setCarriedBlockEntity(BlockEntity blockEntity) {
        this.carriedBlockEntity = blockEntity;
        if (this.carriedBlockEntity != null) {
            ((BlockEntityAccessor) this.carriedBlockEntity).setPos(this.pos);
            if (world != null) carriedBlockEntity.setWorld(world);
        }
    }

    @Override
    public boolean quickcarpet$isRenderModeSet() {
        return renderSet;
    }

    @Override
    public boolean quickcarpet$getRenderCarriedBlockEntity() {
        return renderCarriedBlockEntity;
    }

    @Override
    public void quickcarpet$setRenderCarriedBlockEntity(boolean b) {
        renderCarriedBlockEntity = b;
        renderSet = true;
    }

    /**
     * @author 2No2Name
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private static boolean quickcarpet$movableBlockEntities$setBlockState0(World world, BlockPos pos, BlockState state, int flags, World world1, BlockPos blockPos, BlockState blockState, PistonBlockEntity pistonBlockEntity) {
        if (!Settings.movableBlockEntities)
            return world.setBlockState(pos, state, flags);
        else
            return ((ExtendedWorld) (world)).quickcarpet$setBlockStateWithBlockEntity(pos, state, ((ExtendedPistonBlockEntity) pistonBlockEntity).quickcarpet$getCarriedBlockEntity(), flags);
    }

    @Redirect(method = "finish", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean quickcarpet$movableBlockEntities$setBlockState1(World world, BlockPos blockPos_1, BlockState blockState_2, int int_1) {
        if (!Settings.movableBlockEntities)
            return world.setBlockState(blockPos_1, blockState_2, int_1);
        else {
            boolean ret = ((ExtendedWorld) (world)).quickcarpet$setBlockStateWithBlockEntity(blockPos_1, blockState_2,
                    this.carriedBlockEntity, int_1);
            this.carriedBlockEntity = null; //this will cancel the finishHandleBroken
            return ret;
        }
    }

    @Inject(method = "finish", at = @At(value = "RETURN"))
    private void quickcarpet$movableBlockEntities$finishHandleBroken(CallbackInfo cir) {
        //Handle TNT Explosions or other ways the moving Block is broken
        //Also /setblock will cause this to be called, and drop e.g. a moving chest's contents.
        // This is MC-40380 (BlockEntities that aren't Inventories drop stuff when setblock is called )
        if (Settings.movableBlockEntities && this.carriedBlockEntity != null && this.world != null && !this.world.isClient &&
                this.world.getBlockState(this.pos).getBlock() == Blocks.AIR) {
            BlockState blockState_2;
            if (this.source)
                blockState_2 = Blocks.AIR.getDefaultState();
            else
                blockState_2 = Block.postProcessState(this.pushedBlock, this.world, this.pos);
            ((ExtendedWorld) (this.world)).quickcarpet$setBlockStateWithBlockEntity(this.pos, blockState_2, this.carriedBlockEntity, 3);
            this.world.breakBlock(this.pos, false);
        }
    }

    @Inject(method = "readNbt", at = @At(value = "TAIL"))
    private void quickcarpet$movableBlockEntities$onFromTag(NbtCompound compoundTag_1, CallbackInfo ci) {
        if (Settings.movableBlockEntities && compoundTag_1.contains("carriedTileEntity", 10)) {
            if (this.pushedBlock.getBlock() instanceof BlockEntityProvider) {
                BlockEntity carried = ((BlockEntityProvider) (this.pushedBlock.getBlock())).createBlockEntity(pos, pushedBlock);
                if (carried != null) { //Can actually be null, as BlockPistonMoving.createNewTileEntity(...) returns null
                    carried.readNbt(compoundTag_1.getCompound("carriedTileEntity"));
                    this.quickcarpet$setCarriedBlockEntity(carried);
                }
            }
        }
    }

    @Inject(method = "writeNbt", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void quickcarpet$movableBlockEntities$onToTag(NbtCompound compoundTag_1, CallbackInfo ci) {
        if (Settings.movableBlockEntities && this.carriedBlockEntity != null) {
            //Leave name "carriedTileEntity" instead of "carriedBlockEntity" for upgrade compatibility with 1.12 movable TE
            compoundTag_1.put("carriedTileEntity", this.carriedBlockEntity.createNbtWithId());
        }
    }
}
