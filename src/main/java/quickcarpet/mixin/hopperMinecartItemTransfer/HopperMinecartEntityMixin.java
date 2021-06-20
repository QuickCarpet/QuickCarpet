package quickcarpet.mixin.hopperMinecartItemTransfer;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

import java.util.List;
import java.util.stream.IntStream;

@Mixin(HopperMinecartEntity.class)
public abstract class HopperMinecartEntityMixin extends StorageMinecartEntity implements Hopper {
    @Shadow public abstract boolean canOperate();
    @Shadow public abstract int size();

    protected HopperMinecartEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/HopperMinecartEntity;canOperate()Z"))
    private boolean operate(HopperMinecartEntity hopperMinecartEntity) {
        if (Settings.hopperMinecartItemTransfer && this.insert()) return true;
        return this.canOperate();
    }

    @Unique
    private boolean insert() {
        if (this.isEmpty()) return false;
        Direction outDir = this.getOutputDirection();
        Inventory out = this.getOutputInventory(outDir);
        if (out == null) return false;
        Direction dir = outDir.getOpposite();
        if (isInventoryFull(out, dir)) return false;
        for (int i = 0; i < this.size(); i++) {
            ItemStack stack = this.getStack(i);
            if (stack.isEmpty()) continue;
            stack = stack.copy();
            ItemStack transferred = HopperBlockEntity.transfer(this, out, this.removeStack(i, 1), dir);
            if (transferred.isEmpty()) {
                out.markDirty();
                return true;
            }
            this.setStack(i, stack);
        }
        return false;
    }

    @Unique
    private Direction getOutputDirection() {
        BlockState railState = this.world.getBlockState(new BlockPos(MathHelper.floor(this.getX()), MathHelper.floor(this.getY()), MathHelper.floor(this.getZ())));
        if (!railState.isIn(BlockTags.RAILS)) return Direction.DOWN;
        RailShape shape = railState.get(((AbstractRailBlock) railState.getBlock()).getShapeProperty());
        return switch (shape) {
            case ASCENDING_EAST -> Direction.EAST;
            case ASCENDING_WEST -> Direction.WEST;
            case ASCENDING_NORTH -> Direction.NORTH;
            case ASCENDING_SOUTH -> Direction.SOUTH;
            default -> Direction.DOWN;
        };
    }

    @Unique
    private Inventory getOutputInventory(Direction direction) {
        double normalize = 0.7071067811865476;
        double offX = direction.getOffsetX() * normalize;
        double offY = direction.getAxis() == Direction.Axis.Y ? -1 : -normalize;
        double offZ = direction.getOffsetZ() * normalize;
        double x = getX() + offX;
        double y = getY() + offY + 0.5;
        double z = getZ() + offZ;
        Inventory inventory = null;
        BlockPos blockPos = new BlockPos(x, y, z);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof InventoryProvider) {
            inventory = ((InventoryProvider)block).getInventory(blockState, world, blockPos);
        } else if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof Inventory) {
                inventory = (Inventory)blockEntity;
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    inventory = ChestBlock.getInventory((ChestBlock)block, blockState, world, blockPos, true);
                }
            }
        }

        if (inventory == null) {
            Box box = this.getBoundingBox().stretch(direction.getOffsetX(), -1, direction.getOffsetZ()).shrink(0, 0.5, 0);
            List<Entity> entities = world.getOtherEntities(this, box, entity -> {
                if (!(entity instanceof Inventory) || !entity.isAlive()) return false;
                if (entity instanceof HopperMinecartEntity) return entity.getY() < this.getY() - 0.1;
                return true;
            });
            if (!entities.isEmpty()) {
                inventory = (Inventory)entities.get(world.random.nextInt(entities.size()));
            }
        }

        return inventory;
    }

    @Unique
    private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
        return inventory instanceof SidedInventory ? IntStream.of(((SidedInventory)inventory).getAvailableSlots(side)) : IntStream.range(0, inventory.size());
    }

    @Unique
    private static boolean isInventoryFull(Inventory inv, Direction direction) {
        return getAvailableSlots(inv, direction).allMatch((i) -> {
            ItemStack itemStack = inv.getStack(i);
            return itemStack.getCount() >= itemStack.getMaxCount();
        });
    }
}
