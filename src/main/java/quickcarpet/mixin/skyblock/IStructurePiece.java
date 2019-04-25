package quickcarpet.mixin.skyblock;

import net.minecraft.structure.StructurePiece;
import net.minecraft.util.BlockMirror;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructurePiece.class)
public interface IStructurePiece {
    @Accessor("mirror")
    BlockMirror getMirror();
    @Invoker
    int invokeApplyXTransform(int x, int z);
    @Invoker
    int invokeApplyYTransform(int y);
    @Invoker
    int invokeApplyZTransform(int x, int z);
}
