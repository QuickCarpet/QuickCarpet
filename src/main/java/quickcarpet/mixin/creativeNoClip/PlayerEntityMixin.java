package quickcarpet.mixin.creativeNoClip;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.Utils;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Redirect(method = {"tick", "tickMovement", "updateSize"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"))
    private boolean noClip(PlayerEntity player) {
        return Utils.isNoClip(player);
    }
}
