package quickcarpet.mixin.hopperCounters;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.feature.HopperCounter;
import quickcarpet.settings.Settings;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity {
    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow public abstract void setStack(int int_1, ItemStack itemStack_1);
    @Shadow public abstract int size();

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private static void quickcarpet$hopperCounters$onInsert(World world, BlockPos pos, BlockState state, Inventory hopper, CallbackInfoReturnable<Boolean> cir) {
        if (Settings.hopperCounters && HopperCounter.tryCount(world, pos, state, hopper, null)) {
            markDirty(world, pos, state);
            cir.setReturnValue(false);
        }
    }
}
