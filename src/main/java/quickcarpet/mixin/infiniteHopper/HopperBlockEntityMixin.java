package quickcarpet.mixin.infiniteHopper;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.helper.HopperCounter;
import quickcarpet.helper.WoolTool;
import quickcarpet.settings.Settings;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(method = "insert", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;markDirty()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void infiniteHopperPutBack(World world, BlockPos pos, BlockState state, Inventory input, CallbackInfoReturnable<Boolean> cir, Inventory output, Direction direction, int slot, ItemStack original) {
        if (Settings.infiniteHopper) {
            DyeColor color = WoolTool.getWoolColorAtPosition(world, pos.up());
            if (color != null) {
                if (Settings.hopperCounters) {
                    HopperCounter.COUNTERS.get(HopperCounter.Key.get(color)).add(world.getServer(), original.getItem(), -1);
                }
                input.setStack(slot, original);
            }
        }
    }
}
