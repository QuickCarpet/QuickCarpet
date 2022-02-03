package quickcarpet.mixin.betterChunkLoading;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.settings.Settings;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 6000))
    private int quickcarpet$betterChunkLoading$autosaveInterval(int interval) {
        return Settings.betterChunkLoading ? 900 : interval;
    }
}
