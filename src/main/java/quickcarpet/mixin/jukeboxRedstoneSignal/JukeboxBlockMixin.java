package quickcarpet.mixin.jukeboxRedstoneSignal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import quickcarpet.mixin.accessor.JukeboxBlockEntityAccessor;

import static quickcarpet.settings.Settings.jukeboxRedstoneSignal;

@Mixin(JukeboxBlock.class)
public abstract class JukeboxBlockMixin extends BlockWithEntity {
    protected JukeboxBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return jukeboxRedstoneSignal;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!jukeboxRedstoneSignal) return 0;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity jukebox
            && jukebox.getRecord().getItem() instanceof MusicDiscItem
            && ((JukeboxBlockEntityAccessor) jukebox).getIsPlaying()) {
            return 15;
        }
        return 0;
    }

    @ModifyConstant(method = "onUse", constant = @Constant(intValue = Block.NOTIFY_LISTENERS))
    private int quickcarpet$jukeboxRedstoneSignal$updateNeighbors(int constant) {
        return jukeboxRedstoneSignal ? constant | Block.NOTIFY_NEIGHBORS : constant;
    }
}
