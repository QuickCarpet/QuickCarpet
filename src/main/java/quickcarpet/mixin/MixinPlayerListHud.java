package quickcarpet.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.utils.IPlayerListHud;

@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud implements IPlayerListHud
{
    @Shadow private Component footer;
    
    @Shadow private Component header;
    
    public boolean hasFooterOrHeader()
    {
        return footer != null || header != null;
    }
}
