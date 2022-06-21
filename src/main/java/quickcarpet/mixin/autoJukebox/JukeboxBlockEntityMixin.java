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
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity implements Inventory {
    @Shadow private ItemStack record;

    @Shadow public abstract void setRecord(ItemStack stack);

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
        setRecord(ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (!Settings.autoJukebox || slot != 0) return;
        if (stack.isEmpty()) {
            removeStack(slot);
            return;
        }
        ((JukeboxBlock) Blocks.JUKEBOX).setRecord(null, getWorld(), pos, getCachedState(), stack);
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

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/JukeboxBlockEntity;isPlaying:Z", shift = At.Shift.AFTER))
    private static void quickcarpet$autoJukebox$updateComparators(World world, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity, CallbackInfo ci) {
        if (Settings.autoJukebox) blockEntity.markDirty();
    }

    @Inject(method = "startPlaying", at = @At("TAIL"))
    private void quickcarpet$autoJukebox$updateComparators(CallbackInfo ci) {
        if (Settings.autoJukebox) this.markDirty();
    }
}
