package quickcarpet.api.settings;

import quickcarpet.api.module.QuickCarpetModule;

import java.io.OutputStream;

public interface CoreSettingsManager extends SettingsManager {
    ModuleSettingsManager getModuleSettings(QuickCarpetModule m);
    boolean isLocked();
    void save();
    void dump(OutputStream out);
}
