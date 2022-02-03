package quickcarpet.mixin.carefulBreak;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.utils.CarefulBreakHelper;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow protected @Final ServerPlayerEntity player;

    @Redirect(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void quickcarpet$carefulBreak$onBreak(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player) {
        try {
            CarefulBreakHelper.miningPlayer.set(this.player);
            block.onBreak(world, pos, state, player);
        } finally {
            CarefulBreakHelper.miningPlayer.set(null);
        }
    }

    @Redirect(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private boolean quickcarpet$carefulBreak$removeBlock(ServerWorld world, BlockPos pos, boolean move) {
        try {
            CarefulBreakHelper.miningPlayer.set(this.player);
            return world.removeBlock(pos, move);
        } finally {
            CarefulBreakHelper.miningPlayer.set(null);
        }
    }

    @Redirect(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;afterBreak(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/item/ItemStack;)V"))
    private void quickcarpet$carefulBreak$afterBreak(Block block, World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        try {
            CarefulBreakHelper.miningPlayer.set(this.player);
            block.afterBreak(world, player, pos, state, blockEntity, stack);
        } finally {
            CarefulBreakHelper.miningPlayer.set(null);
        }
    }
}
