package quickcarpet.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.annotation.Feature;
import quickcarpet.patches.FakeServerPlayNetworkHandler;
import quickcarpet.patches.FakeServerPlayerEntity;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Feature("fakePlayer")
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(ServerPlayerEntity player, CallbackInfoReturnable<CompoundTag> cir) {
        if (player instanceof FakeServerPlayerEntity) {
            ((FakeServerPlayerEntity) player).applyStartingPosition();
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/server/network/ServerPlayNetworkHandler"))
    private ServerPlayNetworkHandler replaceNew(MinecraftServer server, ClientConnection clientConnection,
                                                ServerPlayerEntity playerIn) {
        boolean isServerPlayerEntity = playerIn instanceof FakeServerPlayerEntity;
        if (isServerPlayerEntity) {
            return new FakeServerPlayNetworkHandler(this.server, clientConnection, playerIn);
        } else {
            return new ServerPlayNetworkHandler(this.server, clientConnection, playerIn);
        }
    }

    @Redirect(method = "createPlayer", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", remap = false))
    private boolean cancelWhileLoop(Iterator iterator) {
        return false;
    }

    @Inject(method = "createPlayer", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Ljava/util/Iterator;hasNext()Z", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void newWhileLoop(GameProfile gameProfile_1, CallbackInfoReturnable<ServerPlayerEntity> cir, UUID uUID_1,
                              List list_1, Iterator var5) {
        while (var5.hasNext()) {
            ServerPlayerEntity serverPlayerEntity_3 = (ServerPlayerEntity) var5.next();
            if (serverPlayerEntity_3 instanceof FakeServerPlayerEntity) {
                serverPlayerEntity_3.kill();
                continue;
            }
            serverPlayerEntity_3.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.duplicate_login", new Object[0]));
        }
    }

}
