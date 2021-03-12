package quickcarpet.mixin.core;

import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.Translations;

import java.util.Map;

@Mixin(targets = "net.minecraft.util.Language$1")
public abstract class DefaultLanguageMixin extends Language {
    @Redirect(method = "get(Ljava/lang/String;)Ljava/lang/String;", at = @At(value = "INVOKE", target = "Ljava/util/Map;getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false), require = 1, allow = 1)
    private Object get(Map<String, String> map, Object key, Object defaultValue) {
        //noinspection SuspiciousMethodCalls
        Object value = map.get(key);
        if (value != null) return value;
        return Translations.hasTranslation((String) key) ? Translations.get((String) key) : defaultValue;
    }

    @Redirect(method = "hasTranslation(Ljava/lang/String;)Z", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z", remap = false), require = 1, allow = 1)
    private boolean hasTranslation(Map<String, String> map, Object key) {
        //noinspection SuspiciousMethodCalls
        return map.containsKey(key) || Translations.hasTranslation((String) key);
    }
}
