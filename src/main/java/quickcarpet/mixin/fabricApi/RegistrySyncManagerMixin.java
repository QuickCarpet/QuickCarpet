package quickcarpet.mixin.fabricApi;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.QuickCarpet;

@Mixin(value = RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
    private static final ThreadLocal<Identifier> currentRegistry = new ThreadLocal<>();

    @Redirect(method = "createAndPopulateRegistryMap", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/util/registry/Registry;get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;"
    ))
    private static <T> T quickcarpet$redirectGetRegistry(Registry<T> registry, Identifier id) {
        currentRegistry.set(id);
        return registry.get(id);
    }

    @Redirect(method = "createAndPopulateRegistryMap", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/util/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"
    ))
    private static <T> Identifier quickcarpet$redirectGetId(Registry<T> mutableRegistry, T entry) {
        Identifier id = mutableRegistry.getId(entry);
        if (QuickCarpet.getInstance().isIgnoredForRegistrySync(currentRegistry.get(), id)) return null;
        return id;
    }
}
