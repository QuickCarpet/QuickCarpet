package quickcarpet.skyblock.mixin;

import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.TheNetherDimension;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import quickcarpet.skyblock.SkyBlockUtils;

@Mixin(TheNetherDimension.class)
public abstract class TheNetherDimensionMixin extends Dimension {

    public TheNetherDimensionMixin(World world, DimensionType type) {
        super(world, type);
    }

    @Inject(method = "createChunkGenerator()Lnet/minecraft/world/gen/chunk/ChunkGenerator;", at = @At("HEAD"), cancellable = true)
    private void createSkyBlockGenerator(CallbackInfoReturnable<ChunkGenerator<? extends ChunkGeneratorConfig>> cir) {
        LevelGeneratorType type = this.world.getLevelProperties().getGeneratorType();
        if (type == SkyBlockUtils.LEVEL_GENERATOR_TYPE) {
            cir.setReturnValue(SkyBlockUtils.createNetherChunkGenerator(this.world));
            cir.cancel();
        }
    }
}
