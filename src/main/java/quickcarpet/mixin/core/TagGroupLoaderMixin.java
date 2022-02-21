package quickcarpet.mixin.core;

import net.minecraft.block.Block;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.Tag;
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

import java.util.Map;

@Mixin(TagGroupLoader.class)
public abstract class TagGroupLoaderMixin<T> {
    @Shadow @Final private String dataType;

    @Inject(method = "loadTags", at = @At("RETURN"))
    private void quickcarpet$onReload(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, Tag.Builder>> cir) {
        Map<Identifier, Tag.Builder> builders = cir.getReturnValue();
        if (this.dataType.equals("tags/blocks")) {
            for (BlockPropertyTag t : CarpetRegistry.VIRTUAL_BLOCK_TAGS) {
                Tag.Builder tBuilder = new Tag.Builder();
                for (Block b : t.values()) {
                    tBuilder.add(Registry.BLOCK.getId(b), Build.ID);
                }
                builders.put(t.getKey().id(), tBuilder);
            }
        }
    }
}
