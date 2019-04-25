package quickcarpet.mixin;

import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.helper.SkyBlock;

@Mixin(LevelGeneratorType.class)
public class MixinLevelGeneratorType {
    private MixinLevelGeneratorType(int id, String name) {}

    static {
        SkyBlock.LEVEL_GENERATOR_TYPE = (LevelGeneratorType) (Object) new MixinLevelGeneratorType(15, "skyblock");
    }
}
