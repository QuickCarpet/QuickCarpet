package quickcarpet.client;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import quickcarpet.Build;

import java.util.function.Consumer;

public class ClientInit implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Build.ID, new Configs());
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        keybind(Configs.Generic.OPEN_CONFIG_GUI, action -> GuiBase.openGui(new ConfigsGui()));
    }

    private static void keybind(ConfigHotkey config, Consumer<KeyAction> callback) {
        config.getKeybind().setCallback((action, key) -> {
            callback.accept(action);
            return true;
        });
    }
}
