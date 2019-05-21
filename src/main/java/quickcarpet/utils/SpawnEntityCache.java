package quickcarpet.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public interface SpawnEntityCache {
    <T extends Entity> T getCachedEntity(EntityType<T> type);
    <T extends Entity> void setCachedEntity(EntityType<T> type, T entity);
}
