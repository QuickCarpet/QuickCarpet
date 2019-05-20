package quickcarpet.mixin.skyblock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Tickable;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.settings.Settings;
import quickcarpet.utils.IDaylightDetectorBlockEntity;

@Mixin(DaylightDetectorBlockEntity.class)
public abstract class MixinDaylightDetectorBlockEntity extends BlockEntity implements Tickable, IDaylightDetectorBlockEntity
{
    public boolean detectsBlockLight = false;
    
    public MixinDaylightDetectorBlockEntity(BlockEntityType<?> blockEntityType_1)
    {
        super(blockEntityType_1);
    }
    
    public boolean getBlockLightDetection()
    {
        return detectsBlockLight;
    }
    
    public void toggleBlockLightDetection() {
        this.detectsBlockLight = !this.detectsBlockLight;
        float float_1 = detectsBlockLight ? 0.75F : 0.7F;
        world.playSound((PlayerEntity)null, this.pos, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.3F, float_1);
    }
    
    public void fromTag(CompoundTag compoundTag_1)
    {
        super.fromTag(compoundTag_1);
        
        if (Settings.blockLightDetector && compoundTag_1.containsKey("blockLightMode", 3))
        {
            this.detectsBlockLight = compoundTag_1.getInt("blockLightMode") > 0;
        }
    }
    
    public CompoundTag toTag(CompoundTag compoundTag_1)
    {
        compoundTag_1 = super.toTag(compoundTag_1);
        if (Settings.blockLightDetector)
        {
            compoundTag_1.putInt("blockLightMode", this.detectsBlockLight ? 1 : 0);
        }
        return compoundTag_1;
    }
}
