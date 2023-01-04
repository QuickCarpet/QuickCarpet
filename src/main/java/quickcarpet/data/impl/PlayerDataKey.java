package quickcarpet.data.impl;

import quickcarpet.QuickCarpetServer;

import java.util.Map;
import java.util.UUID;

public record PlayerDataKey<T>(int index, Class<T> type) implements quickcarpet.api.data.PlayerDataKey<T> {
    private static int nextId = 0;

    public static <T> PlayerDataKey<T> create(Class<T> type) {
        return new PlayerDataKey<>(nextId++, type);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T get(UUID player) {
        return QuickCarpetServer.getInstance().playerData.get(player, this);
    }

    @Override
    public T set(UUID player, T value) {
        return QuickCarpetServer.getInstance().playerData.set(player, this, value);
    }

    @Override
    public Map<UUID, T> getAllNonnull() {
        return QuickCarpetServer.getInstance().playerData.getNonnullMap(this);
    }
}
