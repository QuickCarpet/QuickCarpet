package quickcarpet.mixin.autoJukebox;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.settings.Settings;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements Inventory {
    @Shadow private ItemStack record;

    public JukeboxBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int size() {
        return Settings.autoJukebox ? 1 : 0;
    }

    @Override
    public boolean isEmpty() {
        return !Settings.autoJukebox || record.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return Settings.autoJukebox && slot == 0 ? record : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return amount > 0 ? removeStack(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (!Settings.autoJukebox || slot != 0 || record.isEmpty()) return ItemStack.EMPTY;
        world.setBlockState(pos, getCachedState().with(JukeboxBlock.HAS_RECORD, false), 2);
        world.syncWorldEvent(WorldEvents.MUSIC_DISC_PLAYED, pos, 0);
        ItemStack stack = record;
        record = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (!Settings.autoJukebox || slot != 0) return;
        if (stack.isEmpty()) {
            removeStack(slot);
            return;
        }
        ((JukeboxBlock) Blocks.JUKEBOX).setRecord(getWorld(), pos, getCachedState(), stack);
        world.syncWorldEvent(null, WorldEvents.MUSIC_DISC_PLAYED, pos, Item.getRawId(stack.getItem()));
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return Settings.autoJukebox && slot == 0 && stack.getItem() instanceof MusicDiscItem;
    }
}
