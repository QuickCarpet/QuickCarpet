package quickcarpet.mixin.blockEntityFix;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;

@Mixin({
    DispenserBlock.class,
    DropperBlock.class
})
public class DispenserBlockMixin {
    @Redirect(method = "dispense", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPointerImpl;getBlockEntity()Lnet/minecraft/block/entity/BlockEntity;"))
    private BlockEntity fixBlockEntityType(BlockPointerImpl blockPointer) {
        BlockEntity be = blockPointer.getBlockEntity();
        if (Settings.blockEntityFix && !(be instanceof DispenserBlockEntity)) {
            be.markInvalid();
            return null;
        }
        return be;
    }

    @Inject(method = "dispense", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/DispenserBlockEntity;chooseNonEmptySlot()I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void ignoreNull(ServerWorld serverWorld, BlockPos pos, CallbackInfo ci, BlockPointerImpl blockPointer, DispenserBlockEntity be) {
        if (be == null) ci.cancel();
    }
}
