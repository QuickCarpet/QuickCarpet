package quickcarpet.mixin.client;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.LayeredVertexConsumerStorage;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.BugFix;
import quickcarpet.annotation.Feature;
import quickcarpet.settings.Settings;
import quickcarpet.utils.IPistonBlockEntity;

@Mixin(PistonBlockEntityRenderer.class)
public abstract class PistonBlockEntityRendererMixin extends BlockEntityRenderer<PistonBlockEntity> {
    public PistonBlockEntityRendererMixin(BlockEntityRenderDispatcher renderDispatcher) {
        super(renderDispatcher);
    }

    @Inject(method = "method_3576", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/entity/PistonBlockEntityRenderer;method_3575(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/MatrixStack;Lnet/minecraft/client/render/LayeredVertexConsumerStorage;Lnet/minecraft/world/World;ZI)V",
            ordinal = 3))
    private void updateRenderBool(PistonBlockEntity pistonBlockEntity_1, double xOffset, double yOffset, double zOffset, float partialTicks, MatrixStack transform, LayeredVertexConsumerStorage bufferWrapper, int int_1, int int_2, CallbackInfo ci) {
        IPistonBlockEntity pistonBlockEntityExt = (IPistonBlockEntity) pistonBlockEntity_1;
        if (!pistonBlockEntityExt.isRenderModeSet())
            pistonBlockEntityExt.setRenderCarriedBlockEntity(Settings.movableBlockEntities && pistonBlockEntityExt.getCarriedBlockEntity() != null);
    }

    @Inject(method = "method_3576", at = @At("RETURN"))
    private void endMethod3576(PistonBlockEntity pistonBlockEntity_1, double xOffset, double yOffset, double zOffset, float partialTicks, MatrixStack transform, LayeredVertexConsumerStorage bufferWrapper, int int_1, int int_2, CallbackInfo ci) {
        IPistonBlockEntity pistonBlockEntityExt = (IPistonBlockEntity) pistonBlockEntity_1;
        if (pistonBlockEntityExt.getRenderCarriedBlockEntity()) {
            BlockEntity carriedBlockEntity = pistonBlockEntityExt.getCarriedBlockEntity();
            if (carriedBlockEntity != null) {
                carriedBlockEntity.setPos(pistonBlockEntity_1.getPos());
                transform.translate(
                    pistonBlockEntity_1.getRenderOffsetX(partialTicks),
                    pistonBlockEntity_1.getRenderOffsetY(partialTicks),
                    pistonBlockEntity_1.getRenderOffsetZ(partialTicks)
                );
                BlockEntityRenderDispatcher.INSTANCE.render(carriedBlockEntity, partialTicks, transform, bufferWrapper, xOffset, yOffset, zOffset);
            }
        }
    }

    @Feature(value = "smoothPistons", bug = @BugFix(""))
    @ModifyConstant(method = "method_3576", constant = @Constant(floatValue = 4f))
    private float fixShort(float shortCutoff) {
        return 0.5f;
    }
}
