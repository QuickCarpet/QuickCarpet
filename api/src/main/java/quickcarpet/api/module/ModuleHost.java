package quickcarpet.api.module;

import quickcarpet.api.QuickCarpetAPI;
import quickcarpet.api.settings.CoreSettingsManager;

import java.util.Set;

public interface ModuleHost {
    void registerModule(QuickCarpetModule module);
    String getVersion();
    /**
     * @since 1.2.0
     */
    CoreSettingsManager getSettingsManager();
    /**
     * @since 1.2.0
     */
    Set<QuickCarpetModule> getModules();

    static ModuleHost getInstance() {
        return QuickCarpetAPI.getInstance();
    }
}
