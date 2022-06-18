package quickcarpet.mixin.fabricApi;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.QuickCarpet;

import java.util.Map;

@Mixin(value = RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
    @Inject(method = "createAndPopulateRegistryMap", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private static void quickcarpet$removeIgnored(boolean isClientSync, @Nullable Map<Identifier, Object2IntMap<Identifier>> activeMap, CallbackInfoReturnable<@Nullable Map<Identifier, Object2IntMap<Identifier>>> cir, Map<Identifier, Object2IntMap<Identifier>> map) {
        if (!isClientSync) return;
        for (var e : map.entrySet()) {
            var registry = e.getKey();
            e.getValue().object2IntEntrySet().removeIf(id -> QuickCarpet.getInstance().isIgnoredForRegistrySync(registry, id.getKey()));
        }
    }
}
