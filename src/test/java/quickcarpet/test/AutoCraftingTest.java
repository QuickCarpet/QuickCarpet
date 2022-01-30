package quickcarpet.test;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import quickcarpet.feature.CraftingTableBlockEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static quickcarpet.test.TestUtils.assertStackEquals;
import static quickcarpet.test.TestUtils.testAt;

public class AutoCraftingTest {
    private static final BlockPos CRAFTING_TABLE_POS = new BlockPos(0, 3, 0);
    private static final BlockPos ABOVE_HOPPER_POS = CRAFTING_TABLE_POS.up();
    private static final BlockPos BELOW_HOPPER_POS = CRAFTING_TABLE_POS.down();
    private static final BlockPos NEXT_TO_ABOVE_HOPPER_POS = ABOVE_HOPPER_POS.east();
    private static final BlockPos NEXT_TO_BELOW_HOPPER_POS = BELOW_HOPPER_POS.east();

    private HopperBlockEntity getHopperAbove(TestContext ctx) {
        HopperBlockEntity hopper = ((HopperBlockEntity) ctx.getBlockEntity(ABOVE_HOPPER_POS));
        testAt(ctx, ABOVE_HOPPER_POS, () -> assertNotNull(hopper));
        return hopper;
    }

    private CraftingTableBlockEntity getCraftingTable(TestContext ctx) {
        CraftingTableBlockEntity craftingTable = ((CraftingTableBlockEntity) ctx.getBlockEntity(CRAFTING_TABLE_POS));
        testAt(ctx, CRAFTING_TABLE_POS, () -> assertNotNull(craftingTable));
        return craftingTable;
    }

    private HopperBlockEntity getHopperBelow(TestContext ctx) {
        HopperBlockEntity hopper = ((HopperBlockEntity) ctx.getBlockEntity(BELOW_HOPPER_POS));
        testAt(ctx, BELOW_HOPPER_POS, () -> assertNotNull(hopper));
        return hopper;
    }

    private void setAbove(TestContext ctx, ItemStack... stacks) {
        if (stacks.length > 5) throw new IllegalArgumentException("At most 5 stacks allowed");
        HopperBlockEntity hopper = getHopperAbove(ctx);
        assert hopper != null;
        for (int i = 0; i < stacks.length; i++) {
            hopper.setStack(i, stacks[i]);
        }
        hopper.markDirty();
    }

    private void lockHoppers(TestContext ctx) {
        ctx.setBlockState(NEXT_TO_ABOVE_HOPPER_POS, Blocks.REDSTONE_BLOCK);
        ctx.setBlockState(NEXT_TO_BELOW_HOPPER_POS, Blocks.REDSTONE_BLOCK);
    }

    @GameTest(structureName = "hopper_table_hopper")
    public void vanilla$noTransfer(TestContext ctx) {
        setAbove(ctx, new ItemStack(Items.REDSTONE_BLOCK));
        ctx.waitAndRun(10, () -> {
            ctx.expectContainerWith(ABOVE_HOPPER_POS, Items.REDSTONE_BLOCK);
            ctx.expectEmptyContainer(CRAFTING_TABLE_POS);
            ctx.expectEmptyContainer(BELOW_HOPPER_POS);
            lockHoppers(ctx);
            ctx.complete();
        });
    }

    @GameTest(structureName = "hopper_table_hopper", batchId = "rules/autoCraftingTable=true")
    public void qc$unpackRedstoneBlock(TestContext ctx) {
        setAbove(ctx, new ItemStack(Items.REDSTONE_BLOCK));
        ctx.waitAndRun(8, () -> {
            ctx.expectEmptyContainer(ABOVE_HOPPER_POS);
            CraftingTableBlockEntity craftingTable = getCraftingTable(ctx);
            testAt(ctx, CRAFTING_TABLE_POS, () -> {
                // output slot redstone
                assertStackEquals(new ItemStack(Items.REDSTONE, 8), craftingTable.getStack(0));
                // input slots empty
                for (int i = 1; i < 10; i++) {
                    assertStackEquals(ItemStack.EMPTY, craftingTable.getStack(i));
                }
            });
            ctx.expectContainerWith(BELOW_HOPPER_POS, Items.REDSTONE);
            ctx.waitAndRun(8 * 8, () -> {
                ctx.expectEmptyContainer(CRAFTING_TABLE_POS);
                testAt(ctx, BELOW_HOPPER_POS, () -> {
                    assertStackEquals(new ItemStack(Items.REDSTONE, 9), getHopperBelow(ctx).getStack(0));
                });
                lockHoppers(ctx);
                ctx.complete();
            });
        });
    }

    @GameTest(structureName = "hopper_table_hopper", batchId = "rules/autoCraftingTable=true")
    public void qc$packRedstoneBlock(TestContext ctx) {
        setAbove(ctx, new ItemStack(Items.REDSTONE, 9));
        ctx.setBlockState(NEXT_TO_BELOW_HOPPER_POS, Blocks.REDSTONE_BLOCK);
        ctx.waitAndRun(8, () -> {
            testAt(ctx, ABOVE_HOPPER_POS, () -> {
                assertStackEquals(new ItemStack(Items.REDSTONE, 8), getHopperAbove(ctx).getStack(0));
            });
            CraftingTableBlockEntity craftingTable = getCraftingTable(ctx);
            testAt(ctx, CRAFTING_TABLE_POS, () -> {
                // first input slot redstone
                assertStackEquals(new ItemStack(Items.REDSTONE), craftingTable.getStack(1));
                // output and other inputs empty
                for (int i = 0; i < 10; i++) {
                    if (i == 1) continue;
                    assertStackEquals(ItemStack.EMPTY, craftingTable.getStack(i));
                }
            });
            ctx.expectEmptyContainer(BELOW_HOPPER_POS);
            ctx.waitAndRun(8 * 8, () -> {
                ctx.expectEmptyContainer(ABOVE_HOPPER_POS);
                testAt(ctx, CRAFTING_TABLE_POS, () -> {
                    // output showing redstone block preview
                    assertStackEquals(new ItemStack(Items.REDSTONE_BLOCK), craftingTable.getStack(0));
                    // but nothing crafted yet
                    assertStackEquals(ItemStack.EMPTY, craftingTable.output);
                    // input filled with redstone
                    for (int i = 1; i < 10; i++) {
                        assertStackEquals(new ItemStack(Items.REDSTONE), craftingTable.getStack(i));
                    }
                });
                ctx.expectEmptyContainer(BELOW_HOPPER_POS);
                ctx.setBlockState(NEXT_TO_BELOW_HOPPER_POS, Blocks.AIR);
                ctx.waitAndRun(1, () -> {
                    ctx.expectEmptyContainer(ABOVE_HOPPER_POS);
                    ctx.expectEmptyContainer(CRAFTING_TABLE_POS);
                    ctx.expectContainerWith(BELOW_HOPPER_POS, Items.REDSTONE_BLOCK);
                    lockHoppers(ctx);
                    ctx.complete();
                });
            });
        });
    }
}
