package quickcarpet.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.SoundEvent;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.BugFix;
import quickcarpet.settings.Settings;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity {

    public BeaconBlockEntityMixin() {
        super(BlockEntityType.BEACON);
    }

    @BugFix("blockEntityNullWorldFix")
    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
    private void fixNullWorld(SoundEvent soundEvent, CallbackInfo ci) {
        if (Settings.blockEntityNullWorldFix && !hasWorld()) {
            LogManager.getLogger().warn("BeaconBlockEntity has no world: " + this.getPos());
            ci.cancel();
        }
    }
}
