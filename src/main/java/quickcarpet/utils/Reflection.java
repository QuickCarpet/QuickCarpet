package quickcarpet.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Lazy;
import net.minecraft.util.WorldSavePath;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class Reflection {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static HashMap<String, String> PRIMITIVE_TYPE_DESCRIPTORS = new HashMap<>();
    static {
        PRIMITIVE_TYPE_DESCRIPTORS.put("void", "V");
        PRIMITIVE_TYPE_DESCRIPTORS.put("byte", "B");
        PRIMITIVE_TYPE_DESCRIPTORS.put("char", "C");
        PRIMITIVE_TYPE_DESCRIPTORS.put("double", "D");
        PRIMITIVE_TYPE_DESCRIPTORS.put("float", "F");
        PRIMITIVE_TYPE_DESCRIPTORS.put("int", "I");
        PRIMITIVE_TYPE_DESCRIPTORS.put("long", "J");
        PRIMITIVE_TYPE_DESCRIPTORS.put("short", "S");
        PRIMITIVE_TYPE_DESCRIPTORS.put("boolean", "Z");
    }

    private static Lazy<MappingResolver> MAPPINGS = new Lazy<>(() -> FabricLoader.getInstance().getMappingResolver());

    private static String unmapToIntermediary(Class<?> cls) {
        return MAPPINGS.get().unmapClassName("intermediary", cls.getName());
    }

    private static Class<?> classForName(String intermediary) throws ClassNotFoundException {
        return Class.forName(MAPPINGS.get().mapClassName("intermediary", intermediary));
    }

    private static MethodHandle getMappedMethod(Class<?> owner, String intermediary, Class<?> retType, Class<?> ...args) throws IllegalAccessException, NoSuchMethodException {
        StringBuilder descriptor = new StringBuilder("(");
        for (Class<?> arg : args) {
            if (arg.isPrimitive()) {
                descriptor.append(PRIMITIVE_TYPE_DESCRIPTORS.get(arg.getName()));
            } else {
                descriptor.append("L").append(unmapToIntermediary(arg).replace('.', '/')).append(";");
            }
        }
        descriptor.append(')');
        if (retType.isPrimitive()) {
            descriptor.append(PRIMITIVE_TYPE_DESCRIPTORS.get(retType.getName()));
        } else {
            descriptor.append("L").append(unmapToIntermediary(retType).replace('.', '/')).append(";");
        }
        String obf = MAPPINGS.get().mapMethodName("intermediary", unmapToIntermediary(owner), intermediary, descriptor.toString());
        return getMethod(owner, obf, retType, args);
    }

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

    public static <T> T callPrivateConstructor(Class<T> cls) {
        try {
            Constructor<T> constr = cls.getDeclaredConstructor();
            constr.setAccessible(true);
            return constr.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFinalField(Object target, String name, Class<?> type, Object value) {
        Class<?> targetType = target.getClass();
        String desc = type.isPrimitive() ? PRIMITIVE_TYPE_DESCRIPTORS.get(type.getName()) : "L" + type.getName().replace('.', '/') + ";";
        String fieldName = MAPPINGS.get().mapFieldName("intermediary", unmapToIntermediary(target.getClass()), name, desc);
        try {
            Field f = targetType.getField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getCallingClass(int frames) throws ClassNotFoundException {
        //noinspection ThrowableNotThrown
        StackTraceElement[] elements = new Error().getStackTrace();
        return Class.forName(elements[frames + 1].getClassName());
    }

    private static class TeleportHandler {
        private static final String INT_LOOK_TARGET = "net.minecraft.class_3143$class_3144";
        private static final MethodHandle teleport;

        static {
            try {
                teleport = getMappedMethod(TeleportCommand.class,
                    "method_13766", void.class,
                    ServerCommandSource.class, Entity.class, ServerWorld.class,
                    double.class, double.class, double.class, Set.class,
                    float.class, float.class, classForName(INT_LOOK_TARGET));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static void teleport(ServerCommandSource source, Entity entity_1, ServerWorld world, double x, double y, double z, Set<PlayerPositionLookS2CPacket.Flag> flags, float yaw, float pitch) {
        try {
            TeleportHandler.teleport.invoke(source, entity_1, world, x, y, z, flags, yaw, pitch, null);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
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

    private static MethodHandle newWorldSavePathHandle = getWorldSavePathConstructor();

    private static MethodHandle getWorldSavePathConstructor() {
        try {
            Constructor<WorldSavePath> constructor = WorldSavePath.class.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return LOOKUP.unreflectConstructor(constructor);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static WorldSavePath newWorldSavePath(String relative) {
        try {
            return (WorldSavePath) newWorldSavePathHandle.invokeExact(relative);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
