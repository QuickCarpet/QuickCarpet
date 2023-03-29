package quickcarpet.mixin.renewableSoulSand;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.settings.Settings;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;getAttacker()Lnet/minecraft/entity/Entity;", shift = At.Shift.BEFORE))
    private void quickcarpet$renewableSoulSand$convertSandToSoulsand(DamageSource source, CallbackInfo ci) {
        if (!Settings.renewableSoulSand) return;

        BlockPos pos = new BlockPos(this.getX(), this.getY(), this.getZ());
        BlockState statePos = this.world.getBlockState(pos);

        BlockPos below = pos.down();
        BlockState stateBelow = this.world.getBlockState(below);

        if (statePos.getBlock() == Blocks.FIRE && stateBelow.isIn(BlockTags.SAND)) {
            this.world.setBlockState(below, Blocks.SOUL_SAND.getDefaultState());
        }
    }
}
