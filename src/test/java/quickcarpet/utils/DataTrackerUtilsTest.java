package quickcarpet.utils;

import net.minecraft.Bootstrap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DataTrackerUtilsTest {
    @Test
    public void validate() {
        Bootstrap.initialize();
        DataTrackerUtils.check();
        Map<Class<? extends Entity>, Integer> vanillaCounts = getVanillaPropertyCounts();
        if (vanillaCounts == null) {
            fail("Could not get vanilla data tracker property counts");
            return;
        }
        Set<Class<? extends Entity>> vanillaTypes = vanillaCounts.keySet();
        Set<Class<? extends Entity>> knownTypes = DataTrackerUtils.KNOWN_PROPERTIES.keySet();
        Set<Class<? extends Entity>> allTypes = new LinkedHashSet<>();
        allTypes.addAll(knownTypes);
        allTypes.addAll(vanillaTypes);
        if (vanillaTypes.size() < allTypes.size()) {
            for (Class<? extends Entity> cls : allTypes) {
                assertTrue(vanillaTypes.contains(cls), () -> "Extra entity " + cls.getSimpleName());
            }
        }
        if (knownTypes.size() < allTypes.size()) {
            for (Class<? extends Entity> cls : allTypes) {
                assertTrue(knownTypes.contains(cls), () -> "Missing entity " + cls.getSimpleName());
            }
        }
        for (Class<? extends Entity> cls : allTypes) {
            int vanillaCount = vanillaCounts.getOrDefault(cls, -1) + 1;
            int knownCount = DataTrackerUtils.collectKnownProperties(cls).size();
            assertEquals(knownCount, vanillaCount, () -> "Mismatching property count for " + cls.getSimpleName() + ": expected " + vanillaCount + ", got " + knownCount);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<? extends Entity>, Integer> getVanillaPropertyCounts() {
        for (Field f : DataTracker.class.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.STATIC) == 0) continue;
            if (f.getType() != Map.class) continue;
            f.setAccessible(true);
            try {
                return (Map<Class<? extends Entity>, Integer>) f.get(null);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }
}
