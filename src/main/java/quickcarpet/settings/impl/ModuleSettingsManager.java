package quickcarpet.settings.impl;

import quickcarpet.api.module.QuickCarpetModule;
import quickcarpet.api.settings.Rule;

class ModuleSettingsManager extends SettingsManager implements quickcarpet.api.settings.ModuleSettingsManager {
    public final QuickCarpetModule module;
    public final String prefix;

    protected ModuleSettingsManager(QuickCarpetModule module, Class<?> settingsClass) {
        super(settingsClass);
        this.module = module;
        this.prefix = module.getId() + "/";
    }

    @Override
    public String getRuleName(String fieldName, Rule rule) {
        return this.prefix + super.getRuleName(fieldName, rule);
    }

    @Override
    protected String getTranslationKey(String fieldName, Rule rule, String key) {
        return module.getId() + ".rule." + getDefaultRuleName(fieldName, rule) + "." + key;
    }

    @Override
    public void parse() {
        super.parse();
        module.addRules(this);
    }
}
