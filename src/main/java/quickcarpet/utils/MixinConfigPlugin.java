package quickcarpet.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("QuickCarpet|MixinConfig");
    private boolean multiconnect;

    @Override
    public void onLoad(String mixinPackage) {
        multiconnect = QuiltLoader.isModLoaded("multiconnect");
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
            case "quickcarpet.mixin.tileTickLimit.compat.lithium.LithiumServerTickSchedulerMixin" -> {
                return QuiltLoader.isModLoaded("lithium");
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
