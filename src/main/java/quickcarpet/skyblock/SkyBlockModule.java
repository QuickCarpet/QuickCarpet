package quickcarpet.skyblock;

import net.fabricmc.api.ModInitializer;
import quickcarpet.QuickCarpet;
import quickcarpet.module.QuickCarpetModule;

public class SkyBlockModule implements ModInitializer, QuickCarpetModule {
    @Override
    public void onInitialize() {
        QuickCarpet.getInstance().registerModule(this);
    }

    @Override
    public String getName() {
        return "Skyblock";
    }

    @Override
    public String getVersion() {
        return "1.1.0";
    }

    @Override
    public String getId() {
        return "skyblock";
    }

    @Override
    public Class<?> getSettingsClass() {
        return SkyBlockSettings.class;
    }
}
