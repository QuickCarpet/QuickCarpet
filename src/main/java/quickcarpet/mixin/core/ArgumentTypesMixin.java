package quickcarpet.mixin.core;

import net.minecraft.SharedConstants;
import net.minecraft.command.argument.ArgumentTypes;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArgumentTypes.class)
public class ArgumentTypesMixin {
    @Redirect(method = "register()V", at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;isDevelopment:Z"))
    private static boolean quickcarpet$isDevelopment() {
        // fabric-gametest-api registers these same argument types by injecting after us
        return SharedConstants.isDevelopment || !QuiltLoader.isModLoaded("fabric-gametest-api-v1");
    }
}
