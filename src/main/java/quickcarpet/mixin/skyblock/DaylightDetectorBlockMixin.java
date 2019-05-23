package quickcarpet.mixin.skyblock;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import quickcarpet.utils.IDaylightDetectorBlockEntity;

import static net.minecraft.block.DaylightDetectorBlock.POWER;
import static net.minecraft.block.DaylightDetectorBlock.updateState;

@Mixin(DaylightDetectorBlock.class)
public abstract class DaylightDetectorBlockMixin extends BlockWithEntity
{
    
    
    @Shadow
    @Final
    public static BooleanProperty INVERTED;
    private Ingredient TOGGLE_BLOCKLIGHT_ITEMS = null;//= Ingredient.ofItems(Items.GLOWSTONE, Items.TORCH, Items.REDSTONE_LAMP, Items.BEACON, Items.JACK_O_LANTERN, Items.CAMPFIRE, Items.LANTERN, Items.SEA_LANTERN, Items.REDSTONE_TORCH, Items.END_ROD);
    
    @Shadow
    public abstract BlockEntity createBlockEntity(BlockView var1);
    
    protected DaylightDetectorBlockMixin(Settings block$Settings_1)
    {
        super(block$Settings_1);
    }
    
    /**
     * @author 2No2Name
     */
    @Inject(method = "updateState", at = @At("HEAD"), cancellable = true)
    private static void myUpdateState(BlockState blockState_1, World world_1, BlockPos blockPos_1, CallbackInfo ci)
    {
        if (!quickcarpet.settings.Settings.blockLightDetector)
            return;
        
        DaylightDetectorBlockEntity myBlockEntity = null;
        boolean blockLightEnabled;
        //false for normal vanilla behavior (day and moonlight detector)
        //true for only blocklight (moonlight mode)
        //true for maximum of block and skylight (no moonlight option)
        
        
        BlockEntity bE = world_1.getBlockEntity(blockPos_1);
        if (bE instanceof DaylightDetectorBlockEntity)
            myBlockEntity = (DaylightDetectorBlockEntity) bE;
        blockLightEnabled = ((IDaylightDetectorBlockEntity) myBlockEntity).getBlockLightDetection();
        
        
        if (blockLightEnabled)
        { //false is vanilla, true for modified
            boolean isInverted = (Boolean) blockState_1.get(INVERTED); //Inverted for case 1: Only blocklight
            
            int blockLight = world_1.getLightLevel(LightType.BLOCK, blockPos_1);
            int skyLight = 0;
            if (!isInverted)
            {
                skyLight = world_1.getLightLevel(LightType.SKY, blockPos_1) - world_1.getAmbientDarkness();
                float float_1 = world_1.getSkyAngleRadians(1.0F);
                if (skyLight > 0)
                {
                    float float_2 = float_1 < 3.1415927F ? 0.0F : 6.2831855F;
                    float_1 += (float_2 - float_1) * 0.2F;
                    skyLight = Math.round((float) skyLight * MathHelper.cos(float_1));
                }
            }
            int newLight = skyLight > blockLight ? skyLight : blockLight;
            newLight = MathHelper.clamp(newLight, 0, 15);
            if ((Integer) blockState_1.get(POWER) != newLight)
            {
                world_1.setBlockState(blockPos_1, (BlockState) blockState_1.with(POWER, newLight), 3);
            }
            ci.cancel();
        }
        
    }
    
    @Redirect(method = "activate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    boolean doNothing(World world, BlockPos blockPos_1, BlockState blockState_1, int int_1)
    {
        return false;
    }
    
    @Inject(method = "activate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/DaylightDetectorBlock;updateState(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void activate0(BlockState blockState_1, World world_1, BlockPos blockPos_1, PlayerEntity playerEntity_1, Hand arg4, BlockHitResult arg5, CallbackInfoReturnable<Boolean> cir, BlockState blockState_2)
    {
        if (quickcarpet.settings.Settings.blockLightDetector)
        {
            ItemStack itemStack_1 = playerEntity_1.getStackInHand(arg4);
            if (!itemStack_1.isEmpty())
            {
                if (TOGGLE_BLOCKLIGHT_ITEMS == null)
                    TOGGLE_BLOCKLIGHT_ITEMS = Ingredient.ofItems(Items.GLOWSTONE, Items.TORCH, Items.REDSTONE_LAMP, Items.BEACON, Items.JACK_O_LANTERN, Items.CAMPFIRE, Items.LANTERN, Items.SEA_LANTERN, Items.REDSTONE_TORCH, Items.END_ROD);
                if (TOGGLE_BLOCKLIGHT_ITEMS.method_8093(itemStack_1))
                {
                    blockState_2 = blockState_2.cycle(INVERTED);
                    BlockEntity blockEntity = world_1.getBlockEntity(blockPos_1);
                    if (blockEntity instanceof DaylightDetectorBlockEntity)
                    {
                        ((IDaylightDetectorBlockEntity) blockEntity).toggleBlockLightDetection();
                    }
                }
            }
        }
        if (blockState_1 != blockState_2 || !quickcarpet.settings.Settings.blockLightDetector)
            world_1.setBlockState(blockPos_1, blockState_2, 4);
        updateState(blockState_2, world_1, blockPos_1);
    }
    
    @Redirect(method = "activate", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/DaylightDetectorBlock;updateState(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void doNothing(BlockState blockState_1, World world_1, BlockPos blockPos_1)
    {
    }
}
