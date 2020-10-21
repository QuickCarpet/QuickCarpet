package quickcarpet.settings.impl;

import quickcarpet.module.QuickCarpetModule;
import quickcarpet.settings.Rule;

import java.lang.reflect.Field;

class ModuleSettingsManager extends SettingsManager implements quickcarpet.settings.ModuleSettingsManager {
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

    @Override
    protected String getTranslationKey(Field field, Rule rule, String key) {
        return module.getId() + ".rule." + getDefaultRuleName(field, rule) + "." + key;
    }
}
