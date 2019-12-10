package quickcarpet.mixin.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.annotation.Feature;
import quickcarpet.patches.FakeServerPlayNetworkHandler;
import quickcarpet.patches.FakeServerPlayerEntity;

@Feature("fakePlayer")
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(ServerPlayerEntity player, CallbackInfoReturnable<CompoundTag> cir) {
        if (player instanceof FakeServerPlayerEntity) {
            ((FakeServerPlayerEntity) player).applyStartingPosition();
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/server/network/ServerPlayNetworkHandler"))
    private ServerPlayNetworkHandler replaceNew(MinecraftServer server, ClientConnection clientConnection, ServerPlayerEntity playerIn) {
        boolean isServerPlayerEntity = playerIn instanceof FakeServerPlayerEntity;
        if (isServerPlayerEntity) {
            return new FakeServerPlayNetworkHandler(server, clientConnection, playerIn);
        } else {
            return new ServerPlayNetworkHandler(server, clientConnection, playerIn);
        }
    }
}
