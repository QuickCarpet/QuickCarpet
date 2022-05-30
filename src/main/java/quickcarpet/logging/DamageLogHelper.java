package quickcarpet.logging;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

import java.util.List;

import static quickcarpet.utils.Constants.OtherKeys.*;
import static quickcarpet.utils.Messenger.*;

public class DamageLogHelper {
    private static boolean shouldLog(String option, PlayerEntity player, Entity from, Entity to) {
        return switch (option) {
            case "all" -> true;
            case "players" -> from instanceof PlayerEntity || to instanceof PlayerEntity;
            case "me" -> from == player || to == player;
            default -> false;
        };
    }

    private static boolean shouldIgnore(LivingEntity target, DamageSource source) {
        return source.isFire() && (target.isFireImmune() || target.hasStatusEffect(StatusEffects.FIRE_RESISTANCE));
    }

    public static void register(LivingEntity target, DamageSource source, float amount) {
        if (shouldIgnore(target, source)) return;
        Loggers.DAMAGE.log((option, player) -> {
            if (!shouldLog(option, player, source.getAttacker(), target)) return null;
            return ts(DAMAGE_LOG_REGISTER, Formatting.BLUE, target.getDisplayName(), dbl(amount, Formatting.AQUA), s(source.getName(), Formatting.GOLD));
        });
    }

    public static void registerAttacker(Entity target, LivingEntity source, float amount) {
        Loggers.DAMAGE.log((option, player) -> {
            if (!shouldLog(option, player, source, target)) return null;
            return ts(DAMAGE_LOG_REGISTER_ATTACKER, Formatting.BLUE, source.getDisplayName(), target.getDisplayName(), dbl(amount, Formatting.AQUA));
        });
    }

    public static void modify(LivingEntity target, DamageSource source, float before, float after, String reason, Object... args) {
        if (before == after || shouldIgnore(target, source)) return;
        Loggers.DAMAGE.log((option, player) -> {
            if (!shouldLog(option, player, source.getAttacker(), target)) return null;
            String key = after == 0 ? DAMAGE_LOG_REDUCE_0 : before > after ? DAMAGE_LOG_REDUCE : DAMAGE_LOG_INCREASE;
            float diff = Math.abs(before - after);
            float percentage = 100 * diff / before;
            Formatting messageColor = after < before ? Formatting.DARK_RED : Formatting.DARK_GREEN;
            Formatting numberColor = after < before ? Formatting.RED : Formatting.GREEN;
            return ts(key, messageColor, dbl(after, numberColor), dbl(diff, numberColor), dbl(percentage), t(DAMAGE_LOG_REASON_PREFIX + reason, args));
        });
    }

    public static void registerFinal(LivingEntity target, DamageSource source, float amount) {
        Loggers.DAMAGE.log((option, player) -> {
            if (!shouldLog(option, player, source.getAttacker(), target)) return null;
            return ts(DAMAGE_LOG_FINAL, Formatting.BLUE, dbl(amount));
        }, () -> List.of(
            new LogParameter("attacker", source.getAttacker() == null ? null : source.getAttacker().getUuidAsString()),
            new LogParameter("target", target.getUuidAsString()),
            new LogParameter("type", source.getName()),
            new LogParameter("amount", amount)
        ));
    }
}
