package quickcarpet.mixin.betterStatistics;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import net.minecraft.datafixer.fix.StatsCounterFix;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.mixin.accessor.BlockStateFlatteningAccessor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(StatsCounterFix.class)
public class StatsCounterFixMixin {
    /**
     * Subtract sub item values from their base items since they are counted for both in 1.12 carpet
     */
    @org.spongepowered.asm.mixin.Dynamic("lambda in makeRule")
    @SuppressWarnings({"target", "OptionalUsedAsFieldOrParameterType"})
    @Redirect(method = "method_5169(Lcom/mojang/datafixers/types/Type;Lcom/mojang/datafixers/Typed;)Lcom/mojang/datafixers/Typed;", at = @At(value = "INVOKE", target = "Ljava/util/Optional;get()Ljava/lang/Object;", remap = false))
    private Object modifyInitialMap(Optional<Map<Dynamic<?>, Dynamic<?>>> opt) {
        //noinspection OptionalGetWithoutIsPresent
        Map<Dynamic<?>, Dynamic<?>> map = new LinkedHashMap<>(opt.get());
        for (Map.Entry<Dynamic<?>, Dynamic<?>> e : map.entrySet()) {
            String key = e.getKey().asString("");
            int fourthDot = StringUtils.ordinalIndexOf(key, ".", 4);
            if (fourthDot < 0) continue;
            if (".0".equals(key.substring(fourthDot))) continue;
            String base = key.substring(0, fourthDot);
            Dynamic<?> value = e.getValue();
            Dynamic<?> baseKey = value.createString(base);
            map.computeIfPresent(baseKey, (k, v) -> v.createInt(Math.max(0, v.asInt(0) - value.asInt(0))));
        }
        return map;
    }

    /**
     * Sum up colliding keys, so that for example minecraft.stone and minecraft.stone.0 both get included in minecraft:stone
     */
    @org.spongepowered.asm.mixin.Dynamic("lambda in makeRule")
    @SuppressWarnings("target")
    @Redirect(method = "method_5169(Lcom/mojang/datafixers/types/Type;Lcom/mojang/datafixers/Typed;)Lcom/mojang/datafixers/Typed;", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Dynamic;set(Ljava/lang/String;Lcom/mojang/serialization/Dynamic;)Lcom/mojang/serialization/Dynamic;", ordinal = 0, remap = false))
    private Dynamic<?> sumStats(Dynamic<?> dyn, String key, Dynamic<?> value) {
        int prev = dyn.get(key).asInt(0);
        Dynamic<?> newValue = value.createInt(prev + value.asInt(0));
        return dyn.set(key, newValue);
    }

    /** Use meta from {@code <stat>.minecraft.<item>.<meta>} (converted to ':'s) */
    @Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
    private void updateCarpetItem(String id, CallbackInfoReturnable<String> cir) {
        int dot = StringUtils.ordinalIndexOf(id, ":", 2);
        if (dot > 0) {
            String base = id.substring(0, dot);
            if ("minecraft:filled_map".equals(base)) {
                cir.setReturnValue(base);
                return;
            }
            try {
                int meta = Integer.parseInt(id.substring(dot + 1));
                cir.setReturnValue(ItemInstanceTheFlatteningFix.getItem(base, meta));
            } catch (NumberFormatException ignored) {}
        }
    }

    /**
     * Use meta from {@code <stat>.minecraft.<block>.<meta>} (converted to ':'s)
     */
    @Inject(method = "getBlock", at = @At("HEAD"), cancellable = true)
    private void updateCarpetBlock(String id, CallbackInfoReturnable<String> cir) {
        int dot = StringUtils.ordinalIndexOf(id, ":", 2);
        if (dot > 0) {
            try {
                int meta = Integer.parseInt(id.substring(dot + 1));
                int numeridId = BlockStateFlatteningAccessor.getOldBlockToIdMap().getInt(id.substring(0, dot)) >> 4;
                cir.setReturnValue(BlockStateFlattening.lookupStateBlock(numeridId << 4 | meta));
            } catch (NumberFormatException ignored) {}
        }
    }
}
