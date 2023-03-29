package quickcarpet.utils;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import quickcarpet.feature.stateinfo.BlockInfoProvider;
import quickcarpet.feature.stateinfo.FluidInfoProvider;
import quickcarpet.logging.Logger;
import quickcarpet.logging.Loggers;

import java.util.Map;
import java.util.function.Supplier;

public final class QuickCarpetRegistries {
    private static final org.slf4j.Logger LOG = LogUtils.getLogger();
    private static final Map<Identifier, Supplier<?>> DEFAULT_ENTRIES = Maps.newLinkedHashMap();

    private static final MutableRegistry<MutableRegistry<?>> ROOT = new SimpleRegistry<>(createRegistryKey("root"), Lifecycle.experimental(), false);
    public static final Registry<? extends Registry<?>> REGISTRIES = ROOT;

    public static final RegistryKey<Registry<BlockInfoProvider<?>>> BLOCK_INFO_PROVIDER_KEY = createRegistryKey("block_info_provider");
    public static final RegistryKey<Registry<FluidInfoProvider<?>>> FLUID_INFO_PROVIDER_KEY = createRegistryKey("fluid_info_provider");
    public static final RegistryKey<Registry<Logger>> LOGGER_KEY = createRegistryKey("logger");

    public static final Registry<BlockInfoProvider<?>> BLOCK_INFO_PROVIDER = create(BLOCK_INFO_PROVIDER_KEY, r -> BlockInfoProvider.AIR);
    public static final Registry<FluidInfoProvider<?>> FLUID_INFO_PROVIDER = create(FLUID_INFO_PROVIDER_KEY, r -> FluidInfoProvider.EMPTY);
    public static final Registry<Logger> LOGGER = create(LOGGER_KEY, r -> Loggers.TPS);

    static {
        DEFAULT_ENTRIES.forEach((id, defaultEntry) -> {
            if (defaultEntry.get() == null) {
                LOG.error("Unable to bootstrap registry '{}'", id);
            }

        });
    }

    public static void init() {
        REGISTRIES.freeze();
        for (Registry<?> registry : REGISTRIES) {
            registry.freeze();
        }
    }

    private static <T> RegistryKey<Registry<T>> createRegistryKey(String registryId) {
        return RegistryKey.ofRegistry(new Identifier("quickcarpet", registryId));
    }

    private static <T> Registry<T> create(RegistryKey<? extends Registry<T>> key, DefaultEntryGetter<T> defaultEntryGetter) {
        return create(key, new SimpleRegistry<>(key, Lifecycle.experimental(), false), defaultEntryGetter, Lifecycle.experimental());
    }

    @SuppressWarnings("unchecked")
    private static <T, R extends MutableRegistry<T>> R create(RegistryKey<? extends Registry<T>> key, R registry, DefaultEntryGetter<T> defaultEntryGetter, Lifecycle lifecycle) {
        Identifier identifier = key.getValue();
        DEFAULT_ENTRIES.put(identifier, () -> defaultEntryGetter.run(registry));
        ROOT.add((RegistryKey<MutableRegistry<?>>) key, registry, lifecycle);
        return registry;
    }

    private QuickCarpetRegistries() {}

    @FunctionalInterface
    interface DefaultEntryGetter<T> {
        T run(Registry<T> registry);
    }
}
