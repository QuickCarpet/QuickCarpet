package quickcarpet.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.QuickCarpetSettings;

@Mixin(targets = "net/minecraft/block/dispenser/DispenserBehavior$3")
public class MixinDispenserBehaviorFireCharge {
    @Inject(method = "dispenseStack", at = @At("HEAD"), cancellable = true)
    private void convertNetherrack(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!QuickCarpetSettings.getBool("fireChargeConvertsToNetherrack")) return;
        World world = pointer.getWorld();
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        BlockPos front = pointer.getBlockPos().offset(direction);
        BlockState state = world.getBlockState(front);
        if (state.getBlock() == Blocks.COBBLESTONE) {
            world.setBlockState(front, Blocks.NETHERRACK.getDefaultState());
            stack.subtractAmount(1);
            cir.setReturnValue(stack);
            cir.cancel();
        }
    }
}
