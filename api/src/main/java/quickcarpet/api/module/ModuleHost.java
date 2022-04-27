package quickcarpet.api.module;

import quickcarpet.api.QuickCarpetAPI;
import quickcarpet.api.settings.CoreSettingsManager;

public interface ModuleHost {
    void registerModule(QuickCarpetModule module);
    String getVersion();
    /**
     * @since 1.2.0
     */
    CoreSettingsManager getSettingsManager();

    static ModuleHost getInstance() {
        return QuickCarpetAPI.getInstance();
    }
}
