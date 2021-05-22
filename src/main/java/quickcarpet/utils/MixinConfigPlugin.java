package quickcarpet.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|MixinConfig");
    private boolean incompatibleWorldEdit;
    private boolean multiconnect;

    @Override
    public void onLoad(String mixinPackage) {
        incompatibleWorldEdit = hasIncompatibleWorldEdit();
        multiconnect = FabricLoader.getInstance().isModLoaded("multiconnect");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MixinConfig.MIXIN_PACKAGE + ".")) {
            LOGGER.warn("Foreign mixin {}, disabling", mixinClassName);
            return false;
        }
        if (!MixinConfig.getInstance().isMixinEnabled(mixinClassName)) {
            LOGGER.debug("{} disabled by config", mixinClassName);
            return false;
        }
        switch (mixinClassName) {
            case "quickcarpet.mixin.fillUpdates.compat.worldedit.WorldChunkMixin": {
                if (incompatibleWorldEdit) {
                    LOGGER.info("Applying workaround for WorldEdit 7.2.1 - 7.2.2");
                }
                return incompatibleWorldEdit;
            }
            case "quickcarpet.mixin.fillUpdates.compat.WorldChunkMixin": {
                if (!incompatibleWorldEdit) {
                    LOGGER.debug("Not applying WorldEdit workaround");
                }
                return !incompatibleWorldEdit;
            }
            case "quickcarpet.mixin.autoCraftingTable.compat.multiconnect.BlockEntityMixin": {
                return multiconnect;
            }
            case "quickcarpet.mixin.fabricApi.RegistrySyncManagerMixin": {
                if (FabricLoader.getInstance().isModLoaded("fabric-registry-sync-v0")) {
                    LOGGER.info("Applying Fabric API Registry Sync workaround");
                    return true;
                }
                return false;
            }
            case "quickcarpet.mixin.tileTickLimit.compat.lithium.LithiumServerTickSchedulerMixin": {
                return FabricLoader.getInstance().isModLoaded("lithium");
            }
        }
        return true;
    }

    private boolean hasIncompatibleWorldEdit() {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("worldedit");
        if (!container.isPresent()) return false;
        Version worldEditVersion = container.get().getMetadata().getVersion();
        if (worldEditVersion instanceof SemanticVersion) {
            SemanticVersion semanticVersion = (SemanticVersion) worldEditVersion;
            if (semanticVersion.getVersionComponentCount() < 3) return false;
            int major = semanticVersion.getVersionComponent(0);
            int minor = semanticVersion.getVersionComponent(1);
            int patch = semanticVersion.getVersionComponent(2);
            return major == 7 && minor == 2 && patch > 0 && patch < 3;
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
