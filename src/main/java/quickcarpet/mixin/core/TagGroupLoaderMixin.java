package quickcarpet.mixin.core;

import net.minecraft.block.Block;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.TagEntry;
import net.minecraft.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.Build;
import quickcarpet.utils.BlockPropertyTag;
import quickcarpet.utils.CarpetRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(TagGroupLoader.class)
public abstract class TagGroupLoaderMixin<T> {
    @Shadow @Final private String dataType;

    @Inject(method = "loadTags", at = @At("RETURN"))
    private void quickcarpet$onReload(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, List<TagGroupLoader.TrackedEntry>>> cir) {
        Map<Identifier, List<TagGroupLoader.TrackedEntry>> tags = cir.getReturnValue();
        if (this.dataType.equals("tags/blocks")) {
            for (BlockPropertyTag t : CarpetRegistry.VIRTUAL_BLOCK_TAGS) {
                List<TagGroupLoader.TrackedEntry> entries = new ArrayList<>();
                for (Block b : t.values()) {
                    entries.add(new TagGroupLoader.TrackedEntry(TagEntry.create(Registry.BLOCK.getId(b)), Build.ID));
                }
                tags.put(t.getKey().id(), entries);
            }
        }
    }
}
