package quickcarpet.utils;

import com.google.common.collect.AbstractIterator;
import net.minecraft.server.world.ServerChunkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BooleanSupplier;

public class Reflection {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static MethodHandle getMethod(Class<?> owner, String name, Class<?> retType, Class<?> ...args) throws IllegalAccessException, NoSuchMethodException {
        Method m = owner.getDeclaredMethod(name, args);
        m.setAccessible(true);
        return LOOKUP.unreflect(m);
    }

    private static MethodHandle getAnyMethod(Class<?> owner, String[] names, Class<?> retType, Class<?> ...args) throws IllegalAccessException, NoSuchMethodException {
        NoSuchMethodException ex = null;
        for (String name : names) {
            try {
                return getMethod(owner, name, retType, args);
            } catch (NoSuchMethodException e) {
                if (ex == null) ex = e;
                else ex.addSuppressed(e);
            }
        }
        assert ex != null;
        throw ex;
    }

    public static <T> T callDeprecatedPrivateConstructor(Class<T> cls) {
        try {
            Constructor<T> constr = cls.getDeclaredConstructor();
            if ((constr.getModifiers() & Modifier.PUBLIC) == 0) {
                LOGGER.warn("Deprecated: making " + constr + " accessible, please use a public constructor instead");
                constr.setAccessible(true);
            }
            return constr.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getCallingClass(int frames) throws ClassNotFoundException {
        StackTraceElement[] elements = new Error().getStackTrace();
        return Class.forName(elements[frames + 1].getClassName());
    }

    // WTF Fabric? https://github.com/FabricMC/intermediary/issues/6
    private static class ServerChunkManagerTickHandler {
        private static final MethodHandle tick;

        static {
            try {
                tick = getAnyMethod(ServerChunkManager.class, new String[]{"a", "method_12127", "tick"}, void.class, BooleanSupplier.class);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static void tickChunkManager(ServerChunkManager chunkManager, BooleanSupplier shouldKeepTicking) {
        try {
            ServerChunkManagerTickHandler.tick.invokeExact(chunkManager, shouldKeepTicking);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static <B, S extends B> Iterable<Class<? extends B>> iterateSuperClasses(Class<S> start, Class<B> base) {
        return () -> new AbstractIterator<Class<? extends B>>() {
            private Class<? extends B> cls;
            @Override
            protected Class<? extends B> computeNext() {
                if (cls == null) {
                    cls = start;
                    return start;
                }
                if (cls == base) {
                    return endOfData();
                }
                cls = (Class<? extends B>) cls.getSuperclass();
                return cls;
            }
        };
    }
}
