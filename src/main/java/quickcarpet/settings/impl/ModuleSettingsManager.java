package quickcarpet.settings.impl;

import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.api.settings.Rule;

class ModuleSettingsManager extends SettingsManager implements quickcarpet.api.settings.ModuleSettingsManager {
    public final String prefix;

    protected ModuleSettingsManager(QuickCarpetModule module, Class<?> settingsClass) {
        super(module, settingsClass);
        this.prefix = module.getId() + "/";
    }

    public QuickCarpetModule getModule() {
        return (QuickCarpetModule) source;
    }

    @Override
    public String getRuleName(String fieldName, Rule rule) {
        return this.prefix + super.getRuleName(fieldName, rule);
    }

    @Override
    protected String getTranslationKey(String fieldName, Rule rule, String key) {
        return getModule().getId() + ".rule." + getDefaultRuleName(fieldName, rule) + "." + key;
    }
}
