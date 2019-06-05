package quickcarpet.mixin;

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
import quickcarpet.utils.CarpetRegistry;

import java.util.Map;

@Mixin(TagContainer.class)
public abstract class TagContainerMixin<T> {
    @Shadow @Final private String entryType;

    @Shadow private Map<Identifier, Tag<T>> idMap;

    @Inject(method = "applyReload", at = @At("HEAD"))
    private void onReload(Map<Identifier, Tag.Builder<T>> builders, CallbackInfo ci) {
        if (this.entryType.equals("block")) {
            for (Tag<Block> t : CarpetRegistry.VIRTUAL_BLOCK_TAGS) {
                this.idMap.put(t.getId(), (Tag<T> )t);
            }
        }
    }
}
