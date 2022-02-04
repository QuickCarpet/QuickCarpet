package quickcarpet.test;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTestException;
import net.minecraft.test.PositionedException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

public class TestUtils {
    public static void test(TestContext ctx, Executable executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            try {
                ctx.throwGameTestException(throwable.getMessage());
            } catch (GameTestException e) {
                e.initCause(throwable);
                throw e;
            }
        }
    }

    public static void testAt(TestContext ctx, BlockPos pos, Executable executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            try {
                ctx.throwPositionedException(throwable.getMessage(), pos);
            } catch (GameTestException e) {
                e.initCause(throwable);
                throw e;
            }
        }
    }

    public static void testAt(TestContext ctx, Entity entity, Executable executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            try {
                ctx.throwPositionedException(throwable.getMessage(), entity);
            } catch (GameTestException e) {
                e.initCause(throwable);
                throw e;
            }
        }
    }

    public static void assertStackEquals(ItemStack expected, ItemStack actual) {
        if (!ItemStack.areEqual(expected, actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    public static <T extends Entity> T expectEntityAt(TestContext ctx, EntityType<T> type, BlockPos pos) {
        BlockPos blockPos = ctx.getAbsolutePos(pos);
        List<T> list = ctx.getWorld().getEntitiesByType(type, new Box(blockPos), Entity::isAlive);
        if (list.isEmpty()) {
            throw new PositionedException("Expected " + type.getUntranslatedName(), blockPos, pos, ctx.getTick());
        }
        return list.get(0);
    }
}
