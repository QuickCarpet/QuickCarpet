package quickcarpet.mixin.hopperMinecartCooldown;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.settings.Settings;

import java.util.List;

/**
 * @author 2No2Name
 */
@Mixin(HopperMinecartEntity.class)
public abstract class HopperMinecartEntityMixin extends StorageMinecartEntity implements Hopper {
    @Shadow @Final @Mutable private BlockPos currentBlockPos = BlockPos.ORIGIN;

    protected HopperMinecartEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    //Bugfix 0: Make the cart remember its last BlockPos, otherwise it will always compare to BlockPos.ORIGIN
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/HopperMinecartEntity;setTransferCooldown(I)V", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void rememberBlockPos(CallbackInfo ci) {
        if (Settings.hopperMinecartCooldown != 0) {
            this.currentBlockPos = this.getBlockPos();
        } else {
            this.currentBlockPos = BlockPos.ORIGIN;
        }
    }

    //Bugfix 1: Picking up an item doesn't set the cooldown because the return value is false even when successful
    @Inject(method = "canOperate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void extractAndReturnSuccess(CallbackInfoReturnable<Boolean> cir, List<?> list_1) {
        if (Settings.hopperMinecartCooldown != 0) {
            boolean result = HopperBlockEntity.extract(this, (ItemEntity) list_1.get(0));
            cir.setReturnValue(result);
            cir.cancel();
        }
    }

    //Bugfix 2: Make the cooldown custom, not 4 (debatable, 4gt might be intended)
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 4))
    private int cooldownAmount(int original) {
        if (Settings.hopperMinecartCooldown != 0 && original == 4) {
            return Settings.hopperMinecartCooldown;
        } else {
            return original;
        }
    }
}