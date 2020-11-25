package quickcarpet.mixin.hopperCounters;

import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.api.annotation.Feature;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.WoolTool;
import quickcarpet.settings.Settings;

@Feature("hopperCounters")
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity {
    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow public abstract void setStack(int int_1, ItemStack itemStack_1);
    @Shadow public abstract int size();

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private static void onInsert(World world, BlockPos blockPos, BlockState blockState, Inventory inventory, CallbackInfoReturnable<Boolean> cir) {
        if (Settings.hopperCounters) {
            DyeColor woolColor = WoolTool.getWoolColorAtPosition(
                    world,
                    blockPos.offset(blockState.get(HopperBlock.FACING)));


            if (woolColor != null) {
                for (int i = 0; i < inventory.size(); ++i) {
                    if (!inventory.getStack(i).isEmpty()) {
                        ItemStack itemstack = inventory.getStack(i);//.copy();
                        HopperCounter.COUNTERS.get(HopperCounter.Key.get(woolColor)).add(world.getServer(), itemstack);
                        inventory.setStack(i, ItemStack.EMPTY);
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }
}
