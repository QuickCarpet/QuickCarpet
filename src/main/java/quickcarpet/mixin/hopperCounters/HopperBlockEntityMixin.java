package quickcarpet.mixin.hopperCounters;

import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.WoolTool;
import quickcarpet.settings.Settings;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin extends LootableContainerBlockEntity {
    protected HopperBlockEntityMixin(BlockEntityType<?> blockEntityType_1) {
        super(blockEntityType_1);
    }

    @Shadow public abstract double getHopperX();
    @Shadow public abstract double getHopperY();
    @Shadow public abstract double getHopperZ();
    @Shadow public abstract void setStack(int int_1, ItemStack itemStack_1);
    @Shadow public abstract int size();

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void onInsert(CallbackInfoReturnable<Boolean> cir) {
        if (Settings.hopperCounters) {
            DyeColor woolColor = WoolTool.getWoolColorAtPosition(
                    getWorld(),
                    new BlockPos(getHopperX(), getHopperY(), getHopperZ()).offset(this.getCachedState().get(HopperBlock.FACING)));


            if (woolColor != null) {
                for (int i = 0; i < this.size(); ++i) {
                    if (!this.getStack(i).isEmpty()) {
                        ItemStack itemstack = this.getStack(i);//.copy();
                        HopperCounter.COUNTERS.get(HopperCounter.Key.get(woolColor)).add(this.getWorld().getServer(), itemstack);
                        this.setStack(i, ItemStack.EMPTY);
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }
}
