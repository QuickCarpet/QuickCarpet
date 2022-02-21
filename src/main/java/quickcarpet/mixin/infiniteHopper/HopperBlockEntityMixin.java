package quickcarpet.mixin.infiniteHopper;

import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.WoolTool;
import quickcarpet.settings.Settings;

import java.util.function.BooleanSupplier;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity {
    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow protected abstract boolean isFull();
    @Shadow protected abstract void setTransferCooldown(int cooldown);

    @Shadow @Nullable private static Inventory getOutputInventory(World world, BlockPos pos, BlockState state) { throw new AbstractMethodError(); }

    @Shadow private static boolean isInventoryFull(Inventory inventory, Direction direction) { throw new AbstractMethodError(); }

    @Inject(method = "insertAndExtract", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;insert(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/inventory/Inventory;)Z"), cancellable = true)
    private static void quickcarpet$infiniteHopper$beforeLithiumInsert(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier, CallbackInfoReturnable<Boolean> cir) {
        if (!Settings.infiniteHopper) return;
        HopperCounter.Key color = WoolTool.getCounterKey(world, pos.up());
        if (color == null) return;
        boolean bl = insertInfinite(world, pos, state, blockEntity, color);
        HopperBlockEntityMixin hopper = ((HopperBlockEntityMixin) (Object) blockEntity);
        if (!hopper.isFull()) {
            bl |= booleanSupplier.getAsBoolean();
        }

        if (bl) {
            hopper.setTransferCooldown(8);
            BlockEntity.markDirty(world, pos, state);
            cir.setReturnValue(true);
        }
        cir.setReturnValue(false);
    }

    @Unique
    private static boolean insertInfinite(World world, BlockPos pos, BlockState state, Inventory hopper, HopperCounter.Key color) {
        HopperCounter from = HopperCounter.getCounter(color);
        if (Settings.hopperCounters && WoolTool.tryCount(world, pos, state, hopper, from)) {
            return false;
        }
        Inventory output = getOutputInventory(world, pos, state);
        if (output == null ) {
            return false;
        }
        Direction direction = state.get(HopperBlock.FACING).getOpposite();
        if (isInventoryFull(output, direction)) return false;
        for(int slot = 0; slot < hopper.size(); ++slot) {
            if (!hopper.getStack(slot).isEmpty()) {
                ItemStack original = hopper.getStack(slot).copy();
                Item item = original.getItem();
                original.setCount(1);
                ItemStack afterTransfer = HopperBlockEntity.transfer(hopper, output, original, direction);
                if (afterTransfer.isEmpty()) {
                    output.markDirty();
                    if (Settings.hopperCounters) {
                        from.add(world.getServer(), item, -1);
                    }
                    return true;
                }
                hopper.setStack(slot, original);
            }
        }
        return false;
    }
}
