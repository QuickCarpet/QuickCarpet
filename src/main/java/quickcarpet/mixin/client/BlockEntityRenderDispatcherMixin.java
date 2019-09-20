package quickcarpet.mixin.client;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.IBlockEntityRenderDispatcher;

import javax.annotation.Nullable;

@Feature("movableBlockEntities")
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin implements IBlockEntityRenderDispatcher {
    @Shadow
    public static double renderOffsetX;
    @Shadow
    public static double renderOffsetY;
    @Shadow
    public static double renderOffsetZ;
    @Shadow
    public Camera cameraEntity;
    @Shadow
    public World world;
    
    @Shadow
    private static void renderEntity(BlockEntity blockEntity_1, Runnable runnable) {};

    @Shadow @Nullable public abstract <T extends BlockEntity> BlockEntityRenderer<T> get(@Nullable BlockEntity blockEntity_1);

    /**
     * @author 2No2Name
     */
    //Renders the BlockEntity offset by the amount specified in the arguments xOffset yOffset zOffset (the moving block moved in the animation by this)
    //Code copied and modified from BlockEntityRenderDispatcher::render(BlockEntity blockEntity, float partialTicks, int destroyStage, BlockRenderLayer renderLayer, BufferBuilder bufferBuilder)
    public void renderBlockEntityOffset(BlockEntity blockEntity, float partialTicks, int destroyStage, BlockRenderLayer renderLayer, BufferBuilder bufferBuilder,
                                        double xOffset, double yOffset, double zOffset){
        if (blockEntity.getSquaredDistance(this.cameraEntity.getPos().x - xOffset, this.cameraEntity.getPos().y - yOffset, this.cameraEntity.getPos().z - zOffset) < blockEntity.getSquaredRenderDistance()) {
            BlockEntityRenderer<BlockEntity> blockEntityRenderer_1 = this.get(blockEntity);
            if (blockEntityRenderer_1 != null) {
                if (blockEntity.hasWorld() && blockEntity.getType().supports(blockEntity.getCachedState().getBlock())) {
                    BlockPos blockPos_1 = blockEntity.getPos();
                    renderEntity(blockEntity, () ->
                            {
                                bufferBuilder.method_22629(); //add layer to stack
                                bufferBuilder.method_22626(xOffset,yOffset,zOffset); //add offset for the Blockentity
                                blockEntityRenderer_1.method_22747(blockEntity, (double)blockPos_1.getX() - renderOffsetX, (double)blockPos_1.getY() - renderOffsetY, (double)blockPos_1.getZ() - renderOffsetZ, partialTicks, destroyStage, bufferBuilder, renderLayer, blockPos_1);
                                //bufferBuilder.method_22626(-xOffset,-yOffset,-zOffset); //remove offset
                                bufferBuilder.method_22630(); //remove layer from stack
                            }
                    );
                }
            }
        }
    }
}
