package quickcarpet.utils;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import quickcarpet.client.ClientSetting;
import quickcarpet.settings.Settings;

public final class Utils {
    private Utils() {}

    public static boolean isNoClip(PlayerEntity player) {
        if (player.isSpectator()) return true;
        if (!Settings.creativeNoClip) return false;
        if (player instanceof ClientPlayerEntity && !ClientSetting.CREATIVE_NO_CLIP.get()) return false;
        return player.isCreative() && player.abilities.flying;
    }
}
