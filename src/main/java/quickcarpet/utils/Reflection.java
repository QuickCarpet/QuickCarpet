package quickcarpet.utils;

import com.google.common.collect.AbstractIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class Reflection {
    private static final Logger LOGGER = LogManager.getLogger();

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

    public static <B, S extends B> Iterable<Class<? extends B>> iterateSuperClasses(Class<S> start, Class<B> base) {
        return () -> new AbstractIterator<>() {
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
