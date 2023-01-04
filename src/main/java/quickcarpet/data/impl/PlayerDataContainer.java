package quickcarpet.data.impl;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataContainer {
    private final Map<UUID, Int2ObjectMap<Object>> data = new HashMap<>();

    private Int2ObjectMap<Object> getPlayer(UUID player) {
        return data.computeIfAbsent(player, p -> new Int2ObjectOpenHashMap<>());
    }

    public <T> T get(UUID player, PlayerDataKey<T> key) {
        return key.getType().cast(getPlayer(player).get(key.index()));
    }

    public <T> T set(UUID player, PlayerDataKey<T> key, T value) {
        return key.getType().cast(getPlayer(player).put(key.index(), value));
    }

    public <T> Map<UUID, T> getNonnullMap(PlayerDataKey<T> key) {
        Map<UUID, T> result = new HashMap<>();
        Class<T> type = key.getType();
        for (Map.Entry<UUID, Int2ObjectMap<Object>> entry : data.entrySet()) {
            Object value = entry.getValue().get(key.index());
            if (value != null) result.put(entry.getKey(), type.cast(value));
        }
        return Collections.unmodifiableMap(result);
    }
}
