package quickcarpet.mixin.skyblock;

import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import quickcarpet.helper.SkyBlock;

@Mixin(LevelGeneratorType.class)
public class LevelGeneratorTypeMixin {
    private LevelGeneratorTypeMixin(int id, String name) {}

    static {
        SkyBlock.LEVEL_GENERATOR_TYPE = (LevelGeneratorType) (Object) new LevelGeneratorTypeMixin(15, "skyblock");
    }
}
