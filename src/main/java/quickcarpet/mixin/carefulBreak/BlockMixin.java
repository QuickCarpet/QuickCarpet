package quickcarpet.mixin.carefulBreak;

import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.QuickCarpetServer;
import quickcarpet.logging.Loggers;
import quickcarpet.settings.Settings;
import quickcarpet.utils.CarefulBreakHelper;

import java.util.function.Supplier;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "dropStack(Lnet/minecraft/world/World;Ljava/util/function/Supplier;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setToDefaultPickupDelay()V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void quickcarpet$carefulBreak(World world, Supplier<ItemEntity> supplier, ItemStack stack, CallbackInfo ci, ItemEntity item) {
        ServerPlayerEntity player = CarefulBreakHelper.miningPlayer.get();
        if (Settings.carefulBreak && player != null && player.isSneaking() && QuickCarpetServer.getInstance().loggers.isSubscribed(player, Loggers.CAREFUL_BREAK)) {
            item.onPlayerCollision(player);
            if (item.isRemoved()) {
                Vec3d pos = item.getPos();
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, pos.getX(), pos.getY(), pos.getZ(), 0.2F, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4F + 2.0F));
                ci.cancel();
            }
        }
    }
}
