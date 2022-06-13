package quickcarpet.mixin.commandPlayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.feature.player.PlayerActionPack;
import quickcarpet.utils.mixin.extensions.ActionPackOwner;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ActionPackOwner {
    private PlayerActionPack actionPack;

    public PlayerActionPack quickcarpet$getActionPack() {
        return actionPack;
    }

    public void quickcarpet$setActionPack(PlayerActionPack pack) {
        this.actionPack = pack;
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void quickcarpet$player$init(MinecraftServer server, ServerWorld world, GameProfile profile, CallbackInfo ci) {
        this.actionPack = new PlayerActionPack((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void quickcarpet$player$onTick(CallbackInfo ci) {
        actionPack.onUpdate();
    }
}
