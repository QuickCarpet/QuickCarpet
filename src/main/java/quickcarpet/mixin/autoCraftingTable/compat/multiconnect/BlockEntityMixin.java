package quickcarpet.mixin.autoCraftingTable.compat.multiconnect;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.feature.CraftingTableBlockEntity;
import quickcarpet.utils.CarpetRegistry;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    @Inject(method = "createFromNbt", at = @At("HEAD"))
    private static void onCreateFromTag(BlockPos pos, BlockState state, NbtCompound tag, CallbackInfoReturnable<BlockEntity> cir) {
        if (!state.isOf(Blocks.CRAFTING_TABLE)) return;
        if (!"carpet:crafting_table".equals(tag.getString("id"))) return;
        if (BlockEntityType.getId(CarpetRegistry.CRAFTING_TABLE_BLOCK_ENTITY_TYPE) != null) return;
        CraftingTableBlockEntity.addBackMapping();
    }
}
