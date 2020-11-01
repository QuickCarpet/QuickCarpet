package quickcarpet.api;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class ApiUtils {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Class<?>, MethodHandle> INSTANCE_GETTERS = new HashMap<>();

    private static MethodHandle findInstanceGetter(String className, Class<?> type) {
        return INSTANCE_GETTERS.computeIfAbsent(type, type1 -> {
            Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("quickcarpet");
            if (!mod.isPresent() && System.getProperty("org.gradle.test.worker") == null) {
                throw new IllegalStateException("QuickCarpet not found");
            }
            try {
                Class<?> cls = Class.forName(className);
                MethodHandle handle = LOOKUP.findStatic(cls, "getInstance", MethodType.methodType(cls));
                return handle.asType(MethodType.methodType(type));
            } catch (NoSuchMethodException|WrongMethodTypeException e) {
                String version = mod.get().getMetadata().getVersion().getFriendlyString();
                throw new IllegalStateException("QuickCarpet version " + version + " does not support API", e);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("QuickCarpet present, but class " + className + " not found", e);
            } catch (Throwable t) {
                throw new IllegalStateException("QuickCarpet present, but failed to get instance", t);
            }
        });
    }

    static <T> T getInstance(String className, Class<T> type) {
        MethodHandle handle = findInstanceGetter(className, type);
        try {
            //noinspection unchecked
            return (T) handle.invoke();
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }
}
