package quickcarpet.mixin.core;

import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.CarpetRegistry;

import java.util.Map;

@Feature("core")
@Mixin(TagContainer.class)
public abstract class TagContainerMixin<T> {
    @Shadow @Final private String entryType;

    @Inject(method = "applyReload", at = @At("HEAD"))
    private void onReload(Map<Identifier, Tag.Builder<T>> builders, CallbackInfo ci) {
        if (this.entryType.equals("block")) {
            for (Tag<Block> t : CarpetRegistry.VIRTUAL_BLOCK_TAGS) {
                Tag.Builder<Block> tBuilder = new Tag.Builder<>();
                tBuilder.add(t.values().toArray(new Block[0]));
                builders.put(t.getId(), (Tag.Builder<T>) tBuilder);
            }
        }
    }
}
