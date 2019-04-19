package quickcarpet.mixin;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerListHeaderS2CPacket.class)
public interface IPlayerListHeaderS2CPacket {
    @Accessor("header")
    void setHeader(TextComponent header);

    @Accessor("footer")
    void setFooter(TextComponent footer);
}
