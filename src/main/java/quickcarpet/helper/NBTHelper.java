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
    @Nullable
    public static NbtCompound getBlockEntityTag(ItemStack stack) {
        NbtCompound tag = stack.getTag();
        return tag == null ? null : getTagOrNull(tag, "BlockEntityTag", (int) NbtElement.COMPOUND_TYPE);
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
        NbtList items = getTagOrNull(bet, "Items", NbtElement.LIST_TYPE);
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
        NbtList items = getTagOrNull(bet, "Items", NbtElement.LIST_TYPE);
        return items != null && !items.isEmpty();
    }

    public static boolean isEmptyShulkerBox(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof ShulkerBoxBlock && !hasShulkerBoxItems(stack);
    }
}
