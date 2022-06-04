package quickcarpet.mixin.structureSpawnOverrides;

import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatureKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

@Mixin(HuskEntity.class)
public class HuskEntityMixin {
    @Redirect(method = "canSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ServerWorldAccess;isSkyVisible(Lnet/minecraft/util/math/BlockPos;)Z"))
    private static boolean quickcarpet$huskSpawningInDesertPyramids$ignoreSky(ServerWorldAccess worldAccess, BlockPos pos) {
        if (worldAccess.isSkyVisible(pos)) {
            return true;
        }
        if (Settings.huskSpawningInDesertPyramids && worldAccess instanceof ServerWorld world) {
            var feature = world.getRegistryManager()
                .get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY)
                .get(ConfiguredStructureFeatureKeys.DESERT_PYRAMID);
            return world.getStructureAccessor().getStructureContaining(pos, feature).hasChildren();
        }
        return false;
    }
}
