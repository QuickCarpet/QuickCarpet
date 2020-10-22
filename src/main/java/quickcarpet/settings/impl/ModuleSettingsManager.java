package quickcarpet.settings.impl;

import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.api.settings.Rule;

import java.lang.reflect.Field;

class ModuleSettingsManager extends SettingsManager implements quickcarpet.api.settings.ModuleSettingsManager {
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
