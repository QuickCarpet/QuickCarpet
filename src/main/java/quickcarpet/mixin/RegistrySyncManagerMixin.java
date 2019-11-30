package quickcarpet.mixin;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.CarpetRegistry;

@Mixin(value = RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
    @Redirect(method = "toTag", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V",
        ordinal = 0
    ))
    private static void redirectPutInt(CompoundTag compoundTag, String key, int value) {
        if (CarpetRegistry.isIgnoredForSync(key)) return;
        compoundTag.putInt(key, value);
    }
}
