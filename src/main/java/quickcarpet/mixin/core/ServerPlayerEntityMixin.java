package quickcarpet.mixin.core;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.mixin.accessor.ClientSettingsC2SPacketAccessor;
import quickcarpet.utils.extensions.PlayerWithLanguage;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerWithLanguage {
    private String language = "en_US";

    public ServerPlayerEntityMixin(MinecraftServer server, ServerWorld world, GameProfile profile) {
        super(world, world.getSpawnPos(), world.getSpawnAngle(), profile);
    }

    @Inject(method = "setClientSettings", at = @At("HEAD"))
    private void applyLanguage(ClientSettingsC2SPacket settings, CallbackInfo ci) {
        this.language = ((ClientSettingsC2SPacketAccessor) settings).getLanguage();
    }

    @Override
    public String getLanguage() {
        return language;
    }
}
