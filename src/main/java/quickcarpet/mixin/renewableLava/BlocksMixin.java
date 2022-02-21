package quickcarpet.mixin.renewableLava;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import quickcarpet.feature.ObsidianBlock;

@Mixin(Blocks.class)
public class BlocksMixin {
    @Dynamic("static initializer")
    @Redirect(method = "<clinit>",
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=obsidian")),
        at = @At(value = "NEW", target = "net/minecraft/block/Block", ordinal = 0)
    )
    private static Block quickcarpet$renewableLava$newObsidian(Block.Settings settings) {
        return new ObsidianBlock(settings);
    }
}
