package quickcarpet.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.IPlayerListHud;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin implements IPlayerListHud {
    @Shadow private Text footer;
    @Shadow private Text header;

    public boolean hasFooterOrHeader() {
        return footer != null || header != null;
    }
}
