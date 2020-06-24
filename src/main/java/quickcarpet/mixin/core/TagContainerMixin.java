package quickcarpet.mixin.core;

import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.Build;
import quickcarpet.annotation.Feature;
import quickcarpet.utils.BlockPropertyTag;
import quickcarpet.utils.CarpetRegistry;

import java.util.Map;

@Feature("core")
@Mixin(TagContainer.class)
public abstract class TagContainerMixin<T> {
    @Shadow @Final private String entryType;

    @Inject(method = "applyReload", at = @At("HEAD"))
    private void onReload(Map<Identifier, Tag.Builder> builders, CallbackInfo ci) {
        if (this.entryType.equals("block")) {
            for (BlockPropertyTag t : CarpetRegistry.VIRTUAL_BLOCK_TAGS) {
                Tag.Builder tBuilder = new Tag.Builder();
                for (Block b : t.values()) {
                    tBuilder.add(Registry.BLOCK.getId(b), Build.ID);
                }
                builders.put(t.id, tBuilder);
            }
        }
    }
}
