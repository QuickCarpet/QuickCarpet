package quickcarpet.mixin.accessor;

import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import quickcarpet.api.annotation.Feature;

@Feature("core")
@Mixin(BlockTags.class)
public interface BlockTagsAccessor {
    @Invoker("register")
    static Tag.Identified<Block> register(String id) {
        throw new AssertionError();
    }
}
