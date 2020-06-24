package quickcarpet.mixin.renewableFromSilverfish;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.InfestedBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings.RenewableGravelOrSandOption;

import static quickcarpet.settings.Settings.renewableGravel;
import static quickcarpet.settings.Settings.renewableSand;

@Feature("silverFishDropGravel")
@Mixin(InfestedBlock.class)
public abstract class InfestedBlockMixin extends Block {
    public InfestedBlockMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    @Inject(method = "onStacksDropped", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/block/InfestedBlock;spawnSilverfish(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void onOnStacksDropped(BlockState state, World world, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        if (renewableGravel == RenewableGravelOrSandOption.SILVERFISH) {
            dropStack(world, pos, new ItemStack(Blocks.GRAVEL));
        } else if (renewableSand == RenewableGravelOrSandOption.SILVERFISH) {
            dropStack(world, pos, new ItemStack(Blocks.SAND));
        }
    }
}
