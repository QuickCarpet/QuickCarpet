package quickcarpet.helper;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import javax.annotation.Nullable;

public class NBTHelper {
    public static final int TAG_END = NbtElement.field_33250;
    public static final int TAG_BYTE = NbtElement.field_33251;
    public static final int TAG_SHORT = NbtElement.field_33252;
    public static final int TAG_INT = NbtElement.field_33253;
    public static final int TAG_LONG = NbtElement.field_33254;
    public static final int TAG_FLOAT = NbtElement.field_33255;
    public static final int TAG_DOUBLE = NbtElement.field_33256;
    public static final int TAG_BYTEARRAY = NbtElement.field_33257;
    public static final int TAG_STRING = NbtElement.field_33258;
    public static final int TAG_LIST = NbtElement.field_33259;
    public static final int TAG_COMPOUND = NbtElement.field_33260;
    public static final int TAG_INTARRAY = NbtElement.field_33261;
    public static final int TAG_LONGARRAY = NbtElement.field_33262;

    @Nullable
    public static NbtCompound getBlockEntityTag(ItemStack stack) {
        NbtCompound tag = stack.getTag();
        return tag == null ? null : getTagOrNull(tag, "BlockEntityTag", TAG_COMPOUND);
    }

    @Nullable
    public static <T extends NbtElement> T getTagOrNull(NbtCompound tag, String key, int type) {
        if (!tag.contains(key, type)) return null;
        //noinspection unchecked
        return (T) tag.get(key);
    }

    public static boolean cleanUpShulkerBoxTag(ItemStack stack) {
        boolean changed = false;

        NbtCompound bet = getBlockEntityTag(stack);
        if (bet == null) return false;
        NbtList items = getTagOrNull(bet, "Items", TAG_LIST);
        if (items != null && items.isEmpty()) {
            bet.remove("Items");
            changed = true;
        }

        if (bet.isEmpty()) {
            stack.setTag(null);
            changed = true;
        }

        return changed;
    }

    public static boolean hasShulkerBoxItems(ItemStack stack) {
        NbtCompound bet = getBlockEntityTag(stack);
        if (bet == null) return false;
        NbtList items = getTagOrNull(bet, "Items", TAG_LIST);
        return items != null && !items.isEmpty();
    }

    public static boolean isEmptyShulkerBox(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof ShulkerBoxBlock && !hasShulkerBoxItems(stack);
    }
}
