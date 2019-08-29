package quickcarpet.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.BugFix;
import quickcarpet.annotation.Feature;
import quickcarpet.client.ClientSetting;
import quickcarpet.settings.Settings;
import quickcarpet.utils.IBlockEntityRenderDispatcher;
import quickcarpet.utils.IPistonBlockEntity;

@Mixin(PistonBlockEntityRenderer.class)
public abstract class PistonBlockEntityRendererMixin extends BlockEntityRenderer<PistonBlockEntity> {
    @Inject(method = "method_3576", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/entity/PistonBlockEntityRenderer;method_3575(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/client/render/BufferBuilder;Lnet/minecraft/world/World;Z)Z",
            ordinal = 3))
    private void updateRenderBool(PistonBlockEntity pistonBlockEntity_1, double double_1, double double_2, double double_3,
                                  float float_1, int int_1, CallbackInfo ci) {
        if (!((IPistonBlockEntity) pistonBlockEntity_1).isRenderModeSet())
            ((IPistonBlockEntity) pistonBlockEntity_1).setRenderCarriedBlockEntity(Settings.movableBlockEntities && ((IPistonBlockEntity) pistonBlockEntity_1).getCarriedBlockEntity() != null);
    }

    @Inject(method = "method_3576", at = @At("RETURN"))
    private void endMethod3576(PistonBlockEntity pistonBlockEntity_1, double double_1, double double_2, double double_3,
                               float float_1, int int_1, CallbackInfo ci) {
        if (((IPistonBlockEntity) pistonBlockEntity_1).getRenderCarriedBlockEntity()) {
            BlockEntity carriedBlockEntity = ((IPistonBlockEntity) pistonBlockEntity_1).getCarriedBlockEntity();
            if (carriedBlockEntity != null) {
                carriedBlockEntity.setPos(pistonBlockEntity_1.getPos());
                ((IBlockEntityRenderDispatcher) BlockEntityRenderDispatcher.INSTANCE).renderBlockEntityOffset(carriedBlockEntity, float_1, int_1, pistonBlockEntity_1.getRenderOffsetX(float_1), pistonBlockEntity_1.getRenderOffsetY(float_1), pistonBlockEntity_1.getRenderOffsetZ(float_1));
            }
        }
    }

    @Redirect(method = "method_3576", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;disableCull()V"))
    private void fixCulling1() {
        if (ClientSetting.MOVING_BLOCK_CULLING.get()) GlStateManager.enableCull();
    }

    @Inject(method = "method_3576", at = @At("RETURN"))
    private void fixCulling2(PistonBlockEntity blockEntity, double x, double y, double z, float partialTicks, int int_1, CallbackInfo ci) {
        GlStateManager.disableCull();
    }

    @Feature(value = "smoothPistons", bug = @BugFix(""))
    @ModifyConstant(method = "method_3576", constant = @Constant(floatValue = 4f))
    private float fixShort(float shortCutoff) {
        return 0.5f;
    }
}
