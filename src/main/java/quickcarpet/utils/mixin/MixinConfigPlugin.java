package quickcarpet.utils.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|MixinConfig");
    private boolean multiconnect;

    @Override
    public void onLoad(String mixinPackage) {
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
            case "quickcarpet.mixin.autoCraftingTable.compat.multiconnect.BlockEntityMixin" -> {
                return multiconnect;
            }
            case "quickcarpet.mixin.fabricApi.RegistrySyncManagerMixin" -> {
                if (FabricLoader.getInstance().isModLoaded("fabric-registry-sync-v0")) {
                    LOGGER.info("Applying Fabric API Registry Sync workaround");
                    return true;
                }
                return false;
            }
            case "quickcarpet.mixin.tileTickLimit.compat.lithium.LithiumServerTickSchedulerMixin" -> {
                return FabricLoader.getInstance().isModLoaded("lithium");
            }
        }
        return true;
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
