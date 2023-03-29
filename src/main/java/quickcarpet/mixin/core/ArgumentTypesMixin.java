package quickcarpet.mixin.core;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.command.argument.ArgumentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArgumentTypes.class)
public class ArgumentTypesMixin {
    @Redirect(method = "register(Lnet/minecraft/registry/Registry;)Lnet/minecraft/command/argument/serialize/ArgumentSerializer;", at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;isDevelopment:Z"))
    private static boolean quickcarpet$isDevelopment() {
        // fabric-gametest-api registers these same argument types by injecting after us
        return SharedConstants.isDevelopment || !FabricLoader.getInstance().isModLoaded("fabric-gametest-api-v1");
    }
}
