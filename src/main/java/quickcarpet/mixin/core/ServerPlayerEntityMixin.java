package quickcarpet.mixin.core;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.mixin.accessor.ClientSettingsC2SPacketAccessor;
import quickcarpet.utils.extensions.PlayerWithLanguage;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerWithLanguage {
    private String language = "en_US";

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
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
