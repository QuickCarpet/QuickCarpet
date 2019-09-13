package quickcarpet.client;

import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.resource.language.I18n;
import quickcarpet.Build;

import java.util.Collections;
import java.util.List;

public class ConfigsGui extends GuiConfigsBase {
    private static ConfigGuiTab selectedTab = ConfigGuiTab.GENERIC;

    public ConfigsGui() {
        super(10, 50, Build.ID, null, "quickcarpet.gui.title.configs");
    }

    @Override
    public void init() {
        super.init();
        this.clearOptions();
        int x = 10;
        int y = 26;
        for (ConfigGuiTab tab : ConfigGuiTab.values()) {
            x += this.createButton(x, y, -1, tab) + 4;
        }
    }

    private int createButton(int x, int y, int width, ConfigGuiTab tab) {
        String label = tab.getDisplayName();
        if (width < 0) width = this.textRenderer.getStringWidth(label) + 10;

        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label);
        button.setEnabled(tab != selectedTab);
        this.addButton(button, (control, mouseButton) -> {
            selectedTab = tab;
            this.reCreateListWidget();
            this.getListWidget().resetScrollbarPosition();
            this.init();
        });

        return width;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        return ConfigOptionWrapper.createFor(getConfigsForTab(selectedTab));
    }

    @SuppressWarnings("unchecked")
    private static List<IConfigBase> getConfigsForTab(ConfigGuiTab tab) {
        switch (selectedTab) {
            case GENERIC: return (List) ConfigUtils.createConfigWrapperForType(ConfigType.BOOLEAN, Configs.Generic.TOGGLEABLE);
            case GENERIC_HOTKEYS: return (List) Configs.Generic.getHotkeys();
            case RENDERING: return (List) ConfigUtils.createConfigWrapperForType(ConfigType.BOOLEAN, Configs.Rendering.OPTIONS);
            case RENDERING_HOTKEYS: return (List) ConfigUtils.createConfigWrapperForType(ConfigType.HOTKEY, Configs.Rendering.OPTIONS);
        }
        return Collections.emptyList();
    }

    public enum ConfigGuiTab {
        GENERIC("quickcarpet.gui.button.config_gui.generic"),
        GENERIC_HOTKEYS("quickcarpet.gui.button.config_gui.generic_hotkeys"),
        RENDERING("quickcarpet.gui.button.config_gui.rendering"),
        RENDERING_HOTKEYS("quickcarpet.gui.button.config_gui.rendering_hotkeys");

        private final String translationKey;

        ConfigGuiTab(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return I18n.translate(this.translationKey);
        }
    }
}
