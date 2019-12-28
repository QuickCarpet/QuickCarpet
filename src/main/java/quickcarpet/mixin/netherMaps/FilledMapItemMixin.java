package quickcarpet.mixin.netherMaps;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.NetworkSyncedItem;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FilledMapItem.class)
public abstract class FilledMapItemMixin extends NetworkSyncedItem {
    public FilledMapItemMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "updateColors", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/dimension/Dimension;isNether()Z"
    ))
    private boolean redirectIsNether(Dimension dimension) {
        return dimension.isNether() && !quickcarpet.settings.Settings.netherMaps;
    }
}
