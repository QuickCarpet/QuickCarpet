package quickcarpet.settings;

import quickcarpet.module.QuickCarpetModule;

import java.lang.reflect.Field;

public class ModuleSettingsManager extends SettingsManager {
    public final QuickCarpetModule module;
    public final String prefix;

    protected ModuleSettingsManager(QuickCarpetModule module, Class<?> settingsClass) {
        super(settingsClass);
        this.module = module;
        this.prefix = module.getId() + "/";
    }

    @Override
    public String getRuleName(Field field, Rule rule) {
        return this.prefix + super.getRuleName(field, rule);
    }
}
