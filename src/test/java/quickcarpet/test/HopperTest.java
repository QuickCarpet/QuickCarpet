package quickcarpet.test;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.WoolTool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static quickcarpet.test.TestUtils.testAt;

public class HopperTest {
    private static final BlockPos HOPPER_POS = new BlockPos(0, 3, 0);
    private static final BlockPos ABOVE_HOPPER_POS = HOPPER_POS.up();
    private static final BlockPos BELOW_HOPPER_POS = HOPPER_POS.down();
    private static final Block[] WOOL_BLOCKS = Registry.BLOCK.stream().filter(BlockTags.WOOL::contains).toArray(Block[]::new);
    private static int woolIndex;

    private void clearCounters(TestContext ctx) {
        HopperCounter.getCounter(HopperCounter.Key.ALL).reset(ctx.getWorld().getServer());
    }

    private HopperBlockEntity getHopper(TestContext ctx) {
        HopperBlockEntity hopper = (HopperBlockEntity) ctx.getBlockEntity(HOPPER_POS);
        testAt(ctx, HOPPER_POS, () -> assertNotNull(hopper));
        return hopper;
    }

    private ChestBlockEntity chestBelow(TestContext ctx) {
        BlockPos chestPos = HOPPER_POS.down();
        ctx.setBlockState(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) ctx.getBlockEntity(chestPos);
        testAt(ctx, chestPos, () -> assertNotNull(chest));
        return chest;
    }

    private HopperCounter nextWool(TestContext ctx, BlockPos pos) {
        Block wool = WOOL_BLOCKS[woolIndex++ % WOOL_BLOCKS.length];
        ctx.setBlockState(pos, wool);
        return HopperCounter.getCounter(WoolTool.getCounterKey(ctx.getWorld(), ctx.getAbsolutePos(pos)));
    }

    @GameTest(structureName = "wool_hopper_wool")
    public void vanilla$woolHopperWool(TestContext ctx) {
        nextWool(ctx, ABOVE_HOPPER_POS);
        nextWool(ctx, BELOW_HOPPER_POS);
        getHopper(ctx).setStack(0, new ItemStack(Items.STONE, 1));
        ctx.waitAndRun(8, () -> {
            ctx.expectContainerWith(HOPPER_POS, Items.STONE);
            ctx.complete();
        });
    }

    @GameTest(structureName = "wool_hopper_wool", batchId = "rules/hopperCounters=true")
    public void qc$hopperCounters$woolHopperWool(TestContext ctx) {
        clearCounters(ctx);
        nextWool(ctx, ABOVE_HOPPER_POS);
        HopperCounter below = nextWool(ctx, BELOW_HOPPER_POS);
        getHopper(ctx).setStack(0, new ItemStack(Items.STONE, 1));
        ctx.waitAndRun(1, () -> {
            ctx.expectEmptyContainer(HOPPER_POS);
            testAt(ctx, HOPPER_POS, () -> {
                assertEquals(1, below.getTotalItems());
                ctx.complete();
            });
        });
    }

    @GameTest(structureName = "wool_hopper_wool", batchId = "rules/hopperCounters=true,infiniteHopper=true")
    public void qc$infiniteHopper$woolHopperWool(TestContext ctx) {
        clearCounters(ctx);
        HopperCounter aboveCounter = nextWool(ctx, ABOVE_HOPPER_POS);
        HopperCounter belowCounter = nextWool(ctx, BELOW_HOPPER_POS);
        getHopper(ctx).setStack(0, new ItemStack(Items.STONE, 1));
        ctx.waitAndRun(1, () -> {
            ctx.expectContainerWith(HOPPER_POS, Items.STONE);
            testAt(ctx, HOPPER_POS, () -> {
                assertEquals(1, belowCounter.getTotalItems());
                assertEquals(-1, aboveCounter.getTotalItems());
            });
            ctx.waitAndRun(1, () -> {
                ctx.expectContainerWith(HOPPER_POS, Items.STONE);
                testAt(ctx, HOPPER_POS, () -> {
                    assertEquals(2, belowCounter.getTotalItems());
                    assertEquals(-2, aboveCounter.getTotalItems());
                    ctx.complete();
                });
            });
        });
    }

    @GameTest(structureName = "wool_hopper_wool", batchId = "rules/hopperCounters=true,infiniteHopper=true")
    public void qc$infiniteHopper$woolHopperChest(TestContext ctx) {
        clearCounters(ctx);
        HopperCounter aboveCounter = nextWool(ctx, ABOVE_HOPPER_POS);
        ChestBlockEntity chest = chestBelow(ctx);
        getHopper(ctx).setStack(0, new ItemStack(Items.STONE, 1));
        ctx.waitAndRun(1, () -> {
            ctx.expectContainerWith(HOPPER_POS, Items.STONE);
            testAt(ctx, HOPPER_POS, () -> {
                assertEquals(-1, aboveCounter.getTotalItems(), "counter incorrect");
            });
            testAt(ctx, chest.getPos(), () -> {
                assertEquals(1, chest.getStack(0).getCount(), "chest incorrect");
            });
            ctx.waitAndRun(8, () -> {
                ctx.expectContainerWith(HOPPER_POS, Items.STONE);
                testAt(ctx, HOPPER_POS, () -> {
                    assertEquals(-2, aboveCounter.getTotalItems(), "counter incorrect");
                });
                testAt(ctx, chest.getPos(), () -> {
                    assertEquals(2, chest.getStack(0).getCount(), "chest incorrect");
                });
                ctx.complete();
            });
        });
    }
}
