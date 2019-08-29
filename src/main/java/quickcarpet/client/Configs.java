package quickcarpet.client;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import quickcarpet.Build;

import java.io.File;
import java.util.List;

public class Configs implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = Build.ID + ".json";
    private static final int CONFIG_VERSION = 1;

    public static class Generic {
        public static final ConfigHotkey OPEN_CONFIG_GUI = new ConfigHotkey("openConfigGui", "C,Q",  "The key open the in-game config GUI");
        public static final ConfigBooleanHotkeyed SYNC_LOW_TPS = new ConfigBooleanHotkeyed(ClientSetting.SYNC_LOW_TPS.id, ClientSetting.SYNC_LOW_TPS.defaultValue, "", "Synchronze client and server tickrate if below 20", "Sync low TPS");
        public static final ConfigBooleanHotkeyed SYNC_HIGH_TPS = new ConfigBooleanHotkeyed(ClientSetting.SYNC_HIGH_TPS.id, ClientSetting.SYNC_HIGH_TPS.defaultValue, "", "Synchronze client and server tickrate if above 20", "Sync high TPS");

        public static final List<IConfigBase> OPTIONS = ImmutableList.of(
            SYNC_LOW_TPS,
            SYNC_HIGH_TPS
        );

        public static final List<ConfigHotkey> HOTKEYS = ImmutableList.of(
            OPEN_CONFIG_GUI
        );
    }

    public static class Rendering {
        public static final ConfigBooleanHotkeyed MOVING_BLOCK_CULLING = new ConfigBooleanHotkeyed(ClientSetting.MOVING_BLOCK_CULLING.id, ClientSetting.MOVING_BLOCK_CULLING.defaultValue, "", "Cull the insides of moving transparent blocks", "Moving block culling");

        public static final List<IConfigBase> OPTIONS = ImmutableList.of(
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
        System.out.println(root);
        int version = JsonUtils.getIntegerOrDefault(root, "version", 0);
        if (version > CONFIG_VERSION) return;
        ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
        ConfigUtils.readConfigBase(root, "Rendering", Rendering.OPTIONS);
        ConfigUtils.readHotkeys(root, "GenericHotkeys", Generic.HOTKEYS);
    }

    @Override
    public void save() {
        File configDir = FileUtils.getConfigDirectory();
        if ((!configDir.exists() || !configDir.isDirectory()) && !configDir.mkdirs()) return;
        JsonObject root = new JsonObject();
        root.addProperty("version", CONFIG_VERSION);
        ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
        ConfigUtils.writeConfigBase(root, "Rendering", Rendering.OPTIONS);
        ConfigUtils.writeHotkeys(root, "GenericHotkeys", Generic.HOTKEYS);
        JsonUtils.writeJsonToFile(root, new File(configDir, CONFIG_FILE_NAME));
    }
}
