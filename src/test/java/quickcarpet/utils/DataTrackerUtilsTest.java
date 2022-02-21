package quickcarpet.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DataTrackerUtilsTest {
    @Disabled
    @Test
    public void validate() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
        Object2IntMap<Class<? extends Entity>> vanillaCounts = getVanillaPropertyCounts();
        if (vanillaCounts == null) fail("Could not get vanilla data tracker property counts");
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
            Int2ObjectMap<Pair<String, DataTrackerUtils.KnownType>> known = DataTrackerUtils.collectKnownProperties(cls);
            int knownCount = known.size();
            assertEquals(vanillaCount, knownCount, () -> {
                Int2ObjectMap<DataTrackerUtils.KnownType> vanilla = getVanillaTypes(cls);
                List<Pair<DataTrackerUtils.KnownType, DataTrackerUtils.KnownType>> mismatch = new ArrayList<>();
                for (int i = 0; i < Math.max(knownCount, vanillaCount); i++) {
                    mismatch.add(Pair.of(known.get(i).getValue(), vanilla.get(i)));
                }
                return "Mismatching property count for " + cls.getSimpleName() + ": expected " + vanillaCount + ", got " + knownCount + ": " + mismatch;
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static Object2IntMap<Class<? extends Entity>> getVanillaPropertyCounts() {
        for (Field f : DataTracker.class.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.STATIC) == 0) continue;
            if (f.getType() != Object2IntMap.class) continue;
            f.setAccessible(true);
            try {
                return (Object2IntMap<Class<? extends Entity>>) f.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("TRACKED_ENTITIES field not found");
    }

    private static Int2ObjectMap<DataTrackerUtils.KnownType> getVanillaTypes(Class<? extends Entity> cls) {
        Class<?> superCls = cls.getSuperclass();
        Int2ObjectMap<DataTrackerUtils.KnownType> types = superCls == Object.class ? new Int2ObjectOpenHashMap<>() : getVanillaTypes((Class<? extends Entity>)superCls);
        for (Field f : cls.getDeclaredFields()) {
            if ((f.getModifiers() & Modifier.STATIC) == 0) continue;
            if (f.getType() != TrackedData.class) continue;
            f.setAccessible(true);
            try {
                TrackedData<?> entry = (TrackedData<?>) f.get(null);
                types.put(entry.getId(), DataTrackerUtils.KnownType.get(entry.getType()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return types;
    }
}
