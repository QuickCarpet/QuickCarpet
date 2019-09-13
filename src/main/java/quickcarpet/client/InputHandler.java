package quickcarpet.client;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import quickcarpet.Build;

public class InputHandler implements IKeybindProvider {
    private static final InputHandler INSTANCE = new InputHandler();

    private InputHandler() {}

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (IHotkey hk : Configs.Generic.HOTKEYS) manager.addKeybindToMap(hk.getKeybind());
        for (IHotkey hk : Configs.Generic.TOGGLEABLE) manager.addKeybindToMap(hk.getKeybind());
        for (IHotkey hk : Configs.Rendering.OPTIONS) manager.addKeybindToMap(hk.getKeybind());
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Build.ID, "quickcarpet.hotkeys.category.generic_hotkeys", Configs.Generic.getHotkeys());
        manager.addHotkeysForCategory(Build.ID, "quickcarpet.hotkeys.category.rendering_hotkeys", Configs.Rendering.OPTIONS);
    }
}
