package quickcarpet.mixin.fillUpdates;

import net.minecraft.block.Block;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants;

@Mixin(SetBlockCommand.class)
public class SetBlockCommandMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/argument/BlockStateArgument;setBlockState(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;I)Z"))
    private static boolean quickcarpet$fillUpdates(BlockStateArgument blockStateArgument, ServerWorld serverWorld, BlockPos blockPos, int flags) {
        return blockStateArgument.setBlockState(serverWorld, blockPos, Constants.SetBlockState.modifyFlags(flags));
    }

    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;updateNeighbors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V"))
    private static void quickcarpet$fillUpdates(ServerWorld serverWorld, BlockPos pos, Block block) {
        if (!Settings.fillUpdates) return;
        serverWorld.updateNeighbors(pos, block);
    }
}
