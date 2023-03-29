package quickcarpet.feature;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import quickcarpet.mixin.accessor.CraftingInventoryAccessor;
import quickcarpet.utils.CarpetRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static quickcarpet.utils.Messenger.t;

@SuppressWarnings("OptionalAssignedToNull")
public class CraftingTableBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider {
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|CraftingTableBlockEntity");
    private static final int[] OUTPUT_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final int[] INPUT_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private final CraftingInventory craftingInventory = new CraftingInventory(null, 3, 3);
    public DefaultedList<ItemStack> inventory;
    public ItemStack output = ItemStack.EMPTY;
    private final List<AutoCraftingTableContainer> openContainers = new ArrayList<>();
    private @Nullable Recipe<?> lastRecipe;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private @Nullable Optional<CraftingRecipe> cachedRecipe;

    public CraftingTableBlockEntity(BlockPos pos, BlockState state) {
        this(CarpetRegistry.CRAFTING_TABLE_BLOCK_ENTITY_TYPE, pos, state);
    }

    private CraftingTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
        ((CraftingInventoryAccessor) craftingInventory).setInventory(this.inventory);
    }

    public static Type<?> getType(Schema schema) {
        return DSL.optionalFields(
            "Items", DSL.list(TypeReferences.ITEM_STACK.in(schema)),
            "Output", TypeReferences.ITEM_STACK.in(schema)
        ).toSimpleType();
    }

    public static void addBackMapping() {
        LOGGER.info("Adding back removed crafting table block entity mapping");
        Registry.register(Registries.BLOCK_ENTITY_TYPE, "carpet:crafting_table", CarpetRegistry.CRAFTING_TABLE_BLOCK_ENTITY_TYPE);
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        if (BlockEntityType.getId(this.getType()) == null) addBackMapping();
        super.writeNbt(tag);
        Inventories.writeNbt(tag, inventory);
        tag.put("Output", output.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        Inventories.readNbt(tag, inventory);
        this.output = ItemStack.fromNbt(tag.getCompound("Output"));
    }

    @Override
    protected Text getContainerName() {
        return t("container.crafting");
    }

    @Override
    protected ScreenHandler createScreenHandler(int id, PlayerInventory playerInventory) {
        AutoCraftingTableContainer container = new AutoCraftingTableContainer(id, playerInventory, this);
        this.openContainers.add(container);
        return container;
    }

    @Override
    public int[] getAvailableSlots(Direction dir) {
        if (dir == Direction.DOWN && (!output.isEmpty() || getCurrentRecipe().isPresent())) return OUTPUT_SLOTS;
        return INPUT_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot > 0 && getStack(slot).isEmpty();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (slot == 0) return !output.isEmpty() || getCurrentRecipe().isPresent();
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot != 0;
    }

    @Override
    public int size() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) return false;
        }
        return output.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot > 0) return this.inventory.get(slot - 1);
        if (!output.isEmpty()) return output;
        Optional<CraftingRecipe> recipe = getCurrentRecipe();
        return recipe.map(craftingRecipe -> craftingRecipe.craft(craftingInventory, world.getRegistryManager())).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot == 0) {
            if (output.isEmpty()) {
                output = craft();
            }
            return output.split(amount);
        }
        return Inventories.splitStack(this.inventory, slot - 1, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot == 0) {
            ItemStack output = this.output;
            this.output = ItemStack.EMPTY;
            return output;
        }
        return Inventories.removeStack(this.inventory, slot - 1);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) {
            output = stack;
            return;
        }
        inventory.set(slot - 1, stack);
        markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        cachedRecipe = null;
        for (AutoCraftingTableContainer c : openContainers) c.onContentChanged(this);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack stack : this.inventory) {
            finder.addInput(stack);
        }
    }

    @Override
    public void setLastRecipe(@Nullable Recipe<?> recipe) {
        lastRecipe = recipe;
    }

    @Nullable
    @Override
    public Recipe<?> getLastRecipe() {
        return lastRecipe;
    }

    @Override
    public void clear() {
        this.inventory.clear();
        markDirty();
    }

    private Optional<CraftingRecipe> getCurrentRecipe() {
        if (this.world == null) return Optional.empty();
        Optional<CraftingRecipe> recipe = cachedRecipe;
        if (recipe == null) {
            if (lastRecipe instanceof CraftingRecipe last && last.matches(craftingInventory, world)) {
                recipe = Optional.of(last);
            } else {
                recipe = this.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
            }
        }
        recipe.ifPresent(this::setLastRecipe);
        cachedRecipe = recipe;
        return recipe;
    }

    private ItemStack craft() {
        if (this.world == null) return ItemStack.EMPTY;
        Optional<CraftingRecipe> optionalRecipe = getCurrentRecipe();
        if (optionalRecipe.isEmpty()) return ItemStack.EMPTY;
        CraftingRecipe recipe = optionalRecipe.get();
        ItemStack result = recipe.craft(craftingInventory, world.getRegistryManager());
        DefaultedList<ItemStack> remaining = world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, craftingInventory, world);
        for (int i = 0; i < 9; i++) {
            ItemStack current = inventory.get(i);
            ItemStack remainingStack = remaining.get(i);
            if (!current.isEmpty()) {
                current.decrement(1);
            }
            if (!remainingStack.isEmpty()) {
                if (current.isEmpty()) {
                    inventory.set(i, remainingStack);
                } else if (ItemStack.areItemsEqual(current, remainingStack) && ItemStack.areNbtEqual(current, remainingStack)) {
                    current.increment(remainingStack.getCount());
                } else {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), remainingStack);
                }
            }
        }
        markDirty();
        return result;
    }

    public void onContainerClose(AutoCraftingTableContainer container) {
        this.openContainers.remove(container);
    }

    public boolean matches(Recipe<? super CraftingInventory> recipe) {
        return this.world != null && recipe.matches(this.craftingInventory, this.world);
    }
}
