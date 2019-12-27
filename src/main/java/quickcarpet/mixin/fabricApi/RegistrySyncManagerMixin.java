package quickcarpet.mixin.fabricApi;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.MutableRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.QuickCarpet;

@Mixin(value = RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
    private static final ThreadLocal<Identifier> currentRegistry = new ThreadLocal<>();

    @Redirect(method = "toTag", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/util/registry/MutableRegistry;get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;"
    ))
    private static <T> T redirectGetRegistry(MutableRegistry<T> mutableRegistry, Identifier id) {
        currentRegistry.set(id);
        return mutableRegistry.get(id);
    }

    @Redirect(method = "toTag", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/util/registry/MutableRegistry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"
    ))
    private static <T> Identifier redirectGetId(MutableRegistry<T> mutableRegistry, T entry) {
        Identifier id = mutableRegistry.getId(entry);
        if (QuickCarpet.getInstance().isIgnoredForRegistrySync(currentRegistry.get(), id)) return null;
        return id;
    }
}
