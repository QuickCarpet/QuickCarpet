package quickcarpet.api.module;

import quickcarpet.api.QuickCarpetAPI;

public interface ModuleHost {
    void registerModule(QuickCarpetModule module);
    String getVersion();

    static ModuleHost getInstance() {
        return QuickCarpetAPI.getInstance();
    }
}
