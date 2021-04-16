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
    public static final int TAG_END = NbtElement.NULL_TYPE;
    public static final int TAG_BYTE = NbtElement.BYTE_TYPE;
    public static final int TAG_SHORT = NbtElement.SHORT_TYPE;
    public static final int TAG_INT = NbtElement.INT_TYPE;
    public static final int TAG_LONG = NbtElement.LONG_TYPE;
    public static final int TAG_FLOAT = NbtElement.FLOAT_TYPE;
    public static final int TAG_DOUBLE = NbtElement.DOUBLE_TYPE;
    public static final int TAG_BYTEARRAY = NbtElement.BYTE_ARRAY_TYPE;
    public static final int TAG_STRING = NbtElement.STRING_TYPE;
    public static final int TAG_LIST = NbtElement.LIST_TYPE;
    public static final int TAG_COMPOUND = NbtElement.COMPOUND_TYPE;
    public static final int TAG_INTARRAY = NbtElement.INT_ARRAY_TYPE;
    public static final int TAG_LONGARRAY = NbtElement.LONG_ARRAY_TYPE;

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
