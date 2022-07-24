package quickcarpet.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import quickcarpet.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Configs implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = Build.ID + ".json";
    private static final int CONFIG_VERSION = 1;

    private static ConfigHotkey hotkey(String id, String defaultStorageString) {
        return new ConfigHotkey(id, defaultStorageString, "quickcarpet.gui.comment." + id);
    }

    private static ConfigBooleanHotkeyed booleanHotkeyed(ClientSetting<Boolean> clientSetting, String defaultHotkey) {
        return new ConfigBooleanHotkeyed(clientSetting.id, clientSetting.defaultValue, defaultHotkey, "quickcarpet.gui.comment." + clientSetting.id, "quickcarpet.gui.pretty." + clientSetting.id);
    }

    public static class Generic {
        public static final ConfigHotkey OPEN_CONFIG_GUI = hotkey("openConfigGui", "C,Q");
        public static final ConfigBooleanHotkeyed SYNC_LOW_TPS = booleanHotkeyed(ClientSetting.SYNC_LOW_TPS, "");
        public static final ConfigBooleanHotkeyed SYNC_HIGH_TPS = booleanHotkeyed(ClientSetting.SYNC_HIGH_TPS, "");
        public static final ConfigBooleanHotkeyed CREATIVE_NO_CLIP = booleanHotkeyed(ClientSetting.CREATIVE_NO_CLIP, "");
        public static final ConfigBooleanHotkeyed CREATIVE_NO_CLIP_OVERRIDE = booleanHotkeyed(ClientSetting.CREATIVE_NO_CLIP_OVERRIDE, "");
        public static final ConfigBooleanHotkeyed SOUND_ENGINE_FIX = booleanHotkeyed(ClientSetting.SOUND_ENGINE_FIX, "");
        public static final ConfigBooleanHotkeyed REMOVE_NBT_SIZE_LIMIT = booleanHotkeyed(ClientSetting.REMOVE_NBT_SIZE_LIMIT, "");

        public static final List<IConfigBase> OPTIONS = List.of(
            SYNC_LOW_TPS,
            SYNC_HIGH_TPS,
            CREATIVE_NO_CLIP,
            CREATIVE_NO_CLIP_OVERRIDE
        );

        public static final List<IHotkey> HOTKEYS = List.of(
            OPEN_CONFIG_GUI
        );

        public static final List<IHotkeyTogglable> TOGGLEABLE = List.of(
            SYNC_LOW_TPS,
            SYNC_HIGH_TPS,
            CREATIVE_NO_CLIP,
            CREATIVE_NO_CLIP_OVERRIDE,
            SOUND_ENGINE_FIX
        );

        public static List<IHotkey> getHotkeys() {
            List<IHotkey> list = new ArrayList<>();
            list.addAll(Generic.HOTKEYS);
            list.addAll(ConfigUtils.createConfigWrapperForType(ConfigType.HOTKEY, Generic.TOGGLEABLE));
            return list;
        }
    }

    public static class Rendering {
        public static final ConfigBooleanHotkeyed MOVING_BLOCK_CULLING = booleanHotkeyed(ClientSetting.MOVING_BLOCK_CULLING, "");

        public static final List<IHotkeyTogglable> OPTIONS = List.of(
            MOVING_BLOCK_CULLING
        );
    }

    @Override
    public void load() {
        File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);
        if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) return;
        JsonElement data = JsonUtils.parseJsonFile(configFile);
        if (data == null || !data.isJsonObject()) return;
        JsonObject root = data.getAsJsonObject();
        int version = JsonUtils.getIntegerOrDefault(root, "version", 0);
        if (version > CONFIG_VERSION) return;
        ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
        ConfigUtils.readHotkeyToggleOptions(root, "GenericHotkeys", "GenericToggles", Generic.TOGGLEABLE);
        ConfigUtils.readHotkeyToggleOptions(root, "RenderingHotkeys", "Rendering", Rendering.OPTIONS);
    }

    @Override
    public void save() {
        File configDir = FileUtils.getConfigDirectory();
        if ((!configDir.exists() || !configDir.isDirectory()) && !configDir.mkdirs()) return;
        JsonObject root = new JsonObject();
        root.addProperty("version", CONFIG_VERSION);
        ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
        ConfigUtils.writeHotkeyToggleOptions(root, "GenericHotkeys", "GenericToggles", Generic.TOGGLEABLE);
        ConfigUtils.writeHotkeyToggleOptions(root, "RenderingHotkeys", "Rendering", Rendering.OPTIONS);
        JsonUtils.writeJsonToFile(root, new File(configDir, CONFIG_FILE_NAME));
    }
}
