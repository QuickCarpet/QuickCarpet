package quickcarpet.mixin.accessor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockStateFlattening.class)
public interface BlockStateFlatteningAccessor {
    @Accessor("OLD_BLOCK_TO_ID")
    static Object2IntMap<String> getOldBlockToIdMap() { throw new AbstractMethodError(); };
}
