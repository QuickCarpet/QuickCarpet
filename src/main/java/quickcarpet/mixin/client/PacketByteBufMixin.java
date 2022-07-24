package quickcarpet.mixin.client;

import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.client.ClientSetting;

@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin {
    @ModifyConstant(method = "readNbt()Lnet/minecraft/nbt/NbtCompound;", constant = @Constant(longValue = 0x200000L))
    private long quickcarpet$removeNbtSizeLimit(long limit) {
        return ClientSetting.REMOVE_NBT_SIZE_LIMIT.get() ? Long.MAX_VALUE : limit;
    }
}
