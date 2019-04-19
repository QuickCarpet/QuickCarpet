package quickcarpet.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.IPlayerListHud;

@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud implements IPlayerListHud
{
    @Shadow private TextComponent footer;
    
    @Shadow private TextComponent header;
    
    public boolean hasFooterOrHeader()
    {
        return footer != null || header != null;
    }
}
