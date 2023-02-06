package quickcarpet.mixin.commandPlayer;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.feature.player.PlayerActionPack;

@Mixin(AbstractHorseEntity.class)
public class AbstractHorseEntityMixin {
    @Redirect(method = "interactHorse", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z"))
    private boolean quickcarpet$player$consumeClientSide(World world, PlayerEntity player) {
        return world.isClient || PlayerActionPack.isExecuting(player, PlayerActionPack.ActionType.USE);
    }
}
