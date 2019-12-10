package quickcarpet.mixin.accessor;

import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import quickcarpet.annotation.Feature;

@Feature("core")
@Mixin(BlockTags.class)
public interface BlockTagsAccessor {
    @Invoker("register")
    @SuppressWarnings("PublicStaticMixinMember")
    static Tag<Block> register(String id) {
        throw new AssertionError();
    }
}
