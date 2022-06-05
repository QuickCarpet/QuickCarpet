package quickcarpet.mixin.loggers.damage;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.logging.DamageLogHelper;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 1))
    private void quickcarpet$log$damage$modify$respawn(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.modify(this, source, amount, 0, "respawn");
    }

    @Inject(method = "damage", at = {@At(value = "RETURN", ordinal = 2), @At(value = "RETURN", ordinal = 3)})
    private void quickcarpet$log$damage$modify$pvp(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        DamageLogHelper.modify(this, source, amount, 0, "pvp");
    }
}
