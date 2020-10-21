package quickcarpet.module;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.lang.reflect.Method;
import java.util.Optional;

public interface ModuleHost {
    void registerModule(QuickCarpetModule module);
    String getVersion();

    static ModuleHost getInstance() {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer("quickcarpet");
        if (!mod.isPresent()) throw new IllegalStateException("QuickCarpet not found");
        try {
            Class<?> qcClass = Class.forName("quickcarpet.QuickCarpet");
            Method instanceGetter = qcClass.getMethod("getInstance");
            ModuleHost host = (ModuleHost) instanceGetter.invoke(null);
            host.getVersion(); // verify version is after api split
            return host;
        } catch (ClassCastException|LinkageError e) {
            String version = mod.get().getMetadata().getVersion().getFriendlyString();
            throw new IllegalStateException("QuickCarpet version " + version + " does not support API", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("QuickCarpet present, but main class not found", e);
        }
    }
}
