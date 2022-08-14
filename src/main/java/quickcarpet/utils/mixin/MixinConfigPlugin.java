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

    @Override
    public void onLoad(String mixinPackage) {
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
        String relative = mixinClassName.substring(MixinConfig.MIXIN_PACKAGE.length() + 1);
        String[] parts = relative.split("\\.");
        if (parts.length > 2 && parts[1].equals("compat")) {
            boolean apply = FabricLoader.getInstance().isModLoaded(parts[2]);
            if (apply) {
                LOGGER.info("Detected {}, loading {}", parts[2], relative);
            }
            return apply;
        }
        switch (mixinClassName) {
            case "quickcarpet.mixin.fabricApi.RegistrySyncManagerMixin" -> {
                if (FabricLoader.getInstance().isModLoaded("fabric-registry-sync-v0")) {
                    LOGGER.info("Applying Fabric API Registry Sync workaround");
                    return true;
                }
                return false;
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
