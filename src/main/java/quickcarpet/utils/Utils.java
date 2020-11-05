package quickcarpet.utils;

import net.minecraft.entity.player.PlayerEntity;
import quickcarpet.client.ClientSetting;
import quickcarpet.settings.Settings;

public final class Utils {
    private Utils() {}

    public static boolean isNoClip(PlayerEntity player) {
        if (player.isSpectator()) return true;
        if (!player.isCreative() || !player.method_31549().flying) return false;
        if (!Settings.creativeNoClip && !ClientSetting.CREATIVE_NO_CLIP_OVERRIDE.get()) return false;
        return !player.world.isClient() || ClientSetting.CREATIVE_NO_CLIP.get();
    }
}
