package quickcarpet.api.data;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.UUID;

public interface PlayerDataKey<T> {
    Class<T> getType();
    T get(UUID player);
    T set(UUID player, T value);

    Map<UUID, T> getAllNonnull();

    default T get(PlayerEntity player) {
        return get(player.getUuid());
    }

    default T set(PlayerEntity player, T value) {
        return set(player.getUuid(), value);
    }
}
