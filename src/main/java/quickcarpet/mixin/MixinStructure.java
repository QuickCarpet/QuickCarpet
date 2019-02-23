package quickcarpet.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.QuickCarpetSettings;

import java.util.Iterator;
import java.util.List;

@Mixin(Structure.class)
public abstract class MixinStructure {

    @Redirect(method = "method_15172", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorld;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    private void cancelUpdateNeighbours(IWorld iWorld, BlockPos var1, Block var2) {
    }

    @Inject(method = "method_15172", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/IWorld;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void newNeighbourUpdates(IWorld iWorld_1, BlockPos blockPos_1, StructurePlacementData structurePlacementData_1, int int_1, CallbackInfoReturnable<Boolean> cir,
                                     List list_1, MutableIntBoundingBox mutableIntBoundingBox_1, List list_2, List list_3, Iterator var18,
                                     Pair pair_2, BlockPos blockPos_7, BlockState blockState_3, BlockState blockState_4) {
        if (QuickCarpetSettings.getBool("fillUpdates")) {
            iWorld_1.updateNeighbors(blockPos_7, blockState_4.getBlock());
        }
    }

    @Redirect(method = "method_15172", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/IWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/Clearable;clear(Ljava/lang/Object;)V")))
    private boolean cancelSetBlockState1(IWorld iWorld, BlockPos var1, BlockState var2, int var3) {
        return false;
    }

    @Inject(method = "method_15172", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/world/IWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/Clearable;clear(Ljava/lang/Object;)V")))
    private void newSetBlockState1(IWorld iWorld_1, BlockPos blockPos_1, StructurePlacementData structurePlacementData_1,
                                   int int_1, CallbackInfoReturnable<Boolean> cir, List list_1, MutableIntBoundingBox mutableIntBoundingBox_1,
                                   List list_2, List list_3, int int_2, int int_3, int int_4, int int_5, int int_6, int int_7,
                                   List list_4, Iterator var16, Structure.StructureBlockInfo structure$StructureBlockInfo_1,
                                   BlockPos blockPos_2) {
        iWorld_1.setBlockState(blockPos_2, Blocks.BARRIER.getDefaultState(), 4 | (QuickCarpetSettings.getBool("fillUpdates") ? 0 : 1024));
    }

    @Redirect(method = "method_15172", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/IWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 3)))
    private boolean addifSetBlockState(IWorld iWorld, BlockPos var1, BlockState var2, int var3) {
        return iWorld.setBlockState(var1, var2, var3 | (QuickCarpetSettings.getBool("fillUpdates") ? 0 : 1024));
    }

}
