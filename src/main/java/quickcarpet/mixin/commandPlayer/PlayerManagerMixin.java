package quickcarpet.mixin.commandPlayer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.patches.FakeServerPlayNetworkHandler;
import quickcarpet.patches.FakeServerPlayerEntity;

import java.util.List;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void quickcarpet$player$spawn$fixStartingPos(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> cir) {
        if (player instanceof FakeServerPlayerEntity) {
            ((FakeServerPlayerEntity) player).applyStartingPosition();
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/server/network/ServerPlayNetworkHandler"))
    private ServerPlayNetworkHandler quickcarpet$player$replaceNew(MinecraftServer server, ClientConnection clientConnection, ServerPlayerEntity playerIn) {
        boolean isServerPlayerEntity = playerIn instanceof FakeServerPlayerEntity;
        if (isServerPlayerEntity) {
            return new FakeServerPlayNetworkHandler(server, clientConnection, playerIn);
        } else {
            return new ServerPlayNetworkHandler(server, clientConnection, playerIn);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void quickcarpet$player$shadow$preventDoubleDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
        if (!this.players.contains(player)) {
            ci.cancel();
        }
    }
}
