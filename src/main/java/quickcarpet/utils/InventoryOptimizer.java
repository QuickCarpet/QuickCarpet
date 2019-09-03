package quickcarpet.utils;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

import java.util.Objects;

public class InventoryOptimizer {
    private final DefaultedList<ItemStack> stackList;
    protected long bloomFilter;
    protected long preEmptyBloomFilter;
    protected int occupiedSlots;
    protected int fullSlots;
    protected int totalSlots;
    protected int firstFreeSlot = -1;

    public InventoryOptimizer(DefaultedList<ItemStack> stackList) {
        this.stackList = stackList;
    }

    protected ItemStack getSlot(int index) {
        return this.stackList.get(index);
    }

    protected int size() {
        return this.stackList.size();
    }

    public void recalculate() {
        long bloomFilter = 0;
        int occupiedSlots = 0;
        int fullSlots = 0;
        int firstFreeSlot = -1;
        this.totalSlots = size();
        for (int i = 0; i < totalSlots; i++) {
            ItemStack stack = getSlot(i);
            long hash = hash(stack);
            bloomFilter |= hash;
            if (firstFreeSlot < 0) preEmptyBloomFilter |= hash;
            if (!stack.isEmpty()) {
                occupiedSlots++;
                if (stack.getCount() >= stack.getMaxCount()) fullSlots++;
            } else if (firstFreeSlot < 0) {
                firstFreeSlot = occupiedSlots;
            }
        }
        this.bloomFilter = bloomFilter;
        this.occupiedSlots = occupiedSlots;
        this.fullSlots = fullSlots;
        this.firstFreeSlot = firstFreeSlot;
    }

    public int getFirstFreeSlot() {
        return firstFreeSlot;
    }

    public boolean isFull() {
        return fullSlots >= totalSlots;
    }

    public boolean maybeContains(ItemStack stack) {
        if (stack.isEmpty()) return getFirstFreeSlot() >= 0;
        if (occupiedSlots == 0) return false;
        long hash = hash(stack);
        return (bloomFilter & hash) == hash;
    }

    public boolean canMaybeInsert(ItemStack stack) {
        if (getFirstFreeSlot() >= 0) return true;
        if (isFull()) return false;
        return maybeContains(stack);
    }

    private static long hash(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        long hash = HashCommon.mix((long) stack.getItem().hashCode());
        hash ^= HashCommon.mix((long) stack.getDamage());
        hash ^= HashCommon.mix((long) Objects.hashCode(stack.getTag()));
        return hash == 0 ? 1 : hash;
    }

    public int indexOf(ItemStack stack) {
        if (!maybeContains(stack)) return -1;
        for (int i = 0; i < totalSlots; i++) {
            ItemStack slot =  getSlot(i);
            if (areItemsAndTagsEqual(stack, slot)) return i;
        }
        return -1;
    }

    public boolean hasFreeSlots() {
        return getFirstFreeSlot() >= 0;
    }

    public int findInsertSlot(ItemStack stack) {
        return findInsertSlot(stack, 0);
    }

    public int findInsertSlot(ItemStack stack, int start) {
        if (!canMaybeInsert(stack)) return -1;
        int firstFreeSlot = getFirstFreeSlot();
        if (firstFreeSlot >= 0) {
            long hash = hash(stack);
            if ((preEmptyBloomFilter & hash) != hash) return firstFreeSlot;
            for (int i = 0; i < firstFreeSlot; i++) {
                ItemStack slot = getSlot(i);
                if (slot.getCount() >= slot.getMaxCount()) continue;
                if (areItemsAndTagsEqual(stack, slot)) return i;
            }
            return firstFreeSlot;
        }
        if (!maybeContains(stack)) return -1;
        for (int i = start; i < totalSlots; i++) {
            ItemStack slot = getSlot(i);
            if (slot.getCount() >= slot.getMaxCount()) continue;
            if (areItemsAndTagsEqual(stack, slot)) return i;
        }
        return -1;
    }

    private static boolean areItemsAndTagsEqual(ItemStack a, ItemStack b) {
        if (!ItemStack.areItemsEqual(a, b)) return false;
        return ItemStack.areTagsEqual(a, b);
    }
}
