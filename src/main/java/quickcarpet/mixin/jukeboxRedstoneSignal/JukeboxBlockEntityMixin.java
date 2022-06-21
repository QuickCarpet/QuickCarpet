package quickcarpet.mixin.jukeboxRedstoneSignal;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity {
    public JukeboxBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/JukeboxBlockEntity;isPlaying:Z", shift = At.Shift.AFTER))
    private static void quickcarpet$jukeboxRedstoneSignal$updateNeighbors(World world, BlockPos pos, BlockState state, JukeboxBlockEntity blockEntity, CallbackInfo ci) {
        if (Settings.jukeboxRedstoneSignal) world.updateNeighbors(pos, state.getBlock());
    }

    @Inject(method = "startPlaying", at = @At("TAIL"))
    private void quickcarpet$updateComparators(CallbackInfo ci) {
        if (Settings.jukeboxRedstoneSignal && world != null) world.updateNeighbors(pos, getCachedState().getBlock());
    }
}
