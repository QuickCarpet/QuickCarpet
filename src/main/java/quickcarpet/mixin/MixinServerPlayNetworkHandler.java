package quickcarpet.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.packet.PlayerInputC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveServerMessage;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.helper.TickSpeed;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler
{
    
    // CommandTick - added new if statement
    @Inject(method = "onPlayerMove", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSleeping()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void resetActiveTime1(PlayerMoveServerMessage playerMoveServerMessage_1, CallbackInfo ci, ServerWorld serverWorld_1,
            double double_1, double double_2, double double_3, double double_4, double double_5, double double_6, double double_7,
            float float_1, float float_2, double double_8, double double_9, double double_10, double double_11, double double_12)
    {
        if (double_12 > 0.0001D)
        {
            TickSpeed.reset_player_active_timeout();
        }
    }
    
    // CommandTick - added new if statement
    @Inject(method = "onPlayerInput", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;method_14218(FFZZ)V"))
    private void resetActiveTime2(PlayerInputC2SPacket playerLookC2SPacket_1, CallbackInfo ci)
    {
        if (playerLookC2SPacket_1.getSideways() != 0.0F || playerLookC2SPacket_1.getForward() != 0.0F || playerLookC2SPacket_1.isJumping() || playerLookC2SPacket_1.isSneaking())
        {
            TickSpeed.reset_player_active_timeout();
        }
    }
}
