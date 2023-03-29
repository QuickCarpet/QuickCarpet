package quickcarpet.test;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import quickcarpet.logging.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static quickcarpet.test.TestUtils.expectEntityAt;
import static quickcarpet.test.TestUtils.testAt;

public class TntTest {
    private static final BlockPos CENTER = new BlockPos(2, 3, 2);

    private static void createExplosion(TestContext ctx, World.ExplosionSourceType type) {
        BlockPos pos = ctx.getAbsolutePos(CENTER);
        ctx.getWorld().createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, type);
    }

    @GameTest(templateName = "contained_explosion")
    public void vanilla$explosionBlockDamage(TestContext ctx) {
        createExplosion(ctx, World.ExplosionSourceType.TNT);
        ctx.expectBlock(Blocks.AIR, CENTER);
        ctx.complete();
    }

    @GameTest(templateName = "contained_explosion")
    public void vanilla$explosionNoBlockDamage(TestContext ctx) {
        createExplosion(ctx, World.ExplosionSourceType.NONE);
        ctx.expectBlock(Blocks.WHITE_STAINED_GLASS, CENTER);
        ctx.complete();
    }

    @GameTest(templateName = "contained_explosion", batchId = "rules/explosionBlockDamage=false")
    public void qc$explosionNoBlockDamage(TestContext ctx) {
        createExplosion(ctx, World.ExplosionSourceType.TNT);
        ctx.expectBlock(Blocks.WHITE_STAINED_GLASS, CENTER);
        ctx.complete();
    }

    @GameTest(templateName = "contained_explosion")
    public void vanilla$tntUpdateOnPlace(TestContext ctx) {
        ctx.setBlockState(CENTER.down(), Blocks.REDSTONE_BLOCK);
        List<Map<String, Object>> logParams = new ArrayList<>();
        Runnable stopLoggerTest = Loggers.TNT.test((msg, params) -> {
            logParams.add(params);
        });
        ctx.setBlockState(CENTER, Blocks.TNT);
        ctx.expectBlock(Blocks.AIR, CENTER);
        ctx.waitAndRun(80, () -> {
            stopLoggerTest.run();
            testAt(ctx, CENTER, () -> {
                assertEquals(1, logParams.size(), "Expected 1 log message");
                BlockPos absCenter = ctx.getAbsolutePos(CENTER);
                assertEquals(absCenter.getX() + 0.5, logParams.get(0).get("primed.x"));
                assertEquals(absCenter.getY() + 0.0, logParams.get(0).get("primed.y"));
                assertEquals(absCenter.getZ() + 0.5, logParams.get(0).get("primed.z"));
                assertEquals(logParams.get(0).get("primed.y"), logParams.get(0).get("exploded.y"));
            });
            ctx.expectBlock(Blocks.AIR, CENTER.down());
            ctx.complete();
        });
    }

    @GameTest(templateName = "contained_explosion", batchId = "rules/tntUpdateOnPlace=false")
    public void qc$tntUpdateOnPlace(TestContext ctx) {
        ctx.setBlockState(CENTER.down(), Blocks.REDSTONE_BLOCK);
        ctx.setBlockState(CENTER, Blocks.TNT);
        ctx.expectBlock(Blocks.TNT, CENTER);
        ctx.complete();
    }

    @GameTest(templateName = "contained_explosion", batchId = "rules/tntPrimeMomentum=false")
    public void qc$tntPrimeMomentum(TestContext ctx) {
        ctx.setBlockState(CENTER.down(), Blocks.REDSTONE_BLOCK);
        ctx.setBlockState(CENTER, Blocks.TNT);
        ctx.expectBlock(Blocks.AIR, CENTER);
        TntEntity tnt = expectEntityAt(ctx, EntityType.TNT, CENTER);
        testAt(ctx, tnt, () -> assertEquals(new Vec3d(0, 0.2, 0), tnt.getVelocity()));
        tnt.kill();
        ctx.complete();
    }

    @GameTest(templateName = "contained_explosion", batchId = "rules/tntHardcodeAngle=90")
    public void qc$tntHardcodeAngle(TestContext ctx) {
        ctx.setBlockState(CENTER.down(), Blocks.REDSTONE_BLOCK);
        ctx.setBlockState(CENTER, Blocks.TNT);
        ctx.expectBlock(Blocks.AIR, CENTER);
        TntEntity tnt = expectEntityAt(ctx, EntityType.TNT, CENTER);
        testAt(ctx, tnt, () -> {
            assertEquals(-0.02, tnt.getVelocity().getX(), 1e-10);
            assertEquals(0.2, tnt.getVelocity().getY(), 1e-10);
            assertEquals(0, tnt.getVelocity().getZ(), 1e-10);
        });
        tnt.kill();
        ctx.complete();
    }
}
