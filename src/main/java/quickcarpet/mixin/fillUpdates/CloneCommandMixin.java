package quickcarpet.mixin.fillUpdates;

import net.minecraft.block.BlockState;
import net.minecraft.server.command.CloneCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.Constants;

@Mixin(CloneCommand.class)
public class CloneCommandMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private static boolean fillUpdates(ServerWorld serverWorld, BlockPos pos, BlockState state, int flags) {
        return serverWorld.setBlockState(pos, state, Constants.SetBlockState.modifyFlags(flags));
    }
}
