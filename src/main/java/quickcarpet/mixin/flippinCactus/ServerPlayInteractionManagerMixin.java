package quickcarpet.mixin.flippinCactus;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.settings.Settings;
import quickcarpet.utils.BlockRotator;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayInteractionManagerMixin {
    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", shift = At.Shift.BEFORE), cancellable = true)
    private void useCactus(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (stack.getItem() != Items.CACTUS || !Settings.flippinCactus) return;
        ItemUsageContext usageContext = new ItemUsageContext(player, hand, hit);
        if (BlockRotator.flipBlock(usageContext)) {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
        }

    }
}
