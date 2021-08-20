package quickcarpet.test;

import net.minecraft.entity.Entity;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.function.Executable;

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
}
