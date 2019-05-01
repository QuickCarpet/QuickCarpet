package quickcarpet;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuickCarpetSettings {

    public static final String carpetVersion = "v1.7.1";
    public static final Logger LOG = LogManager.getLogger();
    public static final CarpetSettingEntry FalseEntry = CarpetSettingEntry.create("void", "all", "Error").choices("None", "");
    public static final String[] default_tags = {"tnt", "fix", "survival", "creative", "experimental", "optimizations", "feature", "commands"}; //tab completion only
    private static final Map<String, CarpetSettingEntry> settings_store;
    public static boolean locked = false;
    public static boolean skipGenerationChecks = false;
    // Static store
    public static boolean b_hopperCounters = false;

    static {
        settings_store = new HashMap<>();
        set_defaults();
    }

    private static CarpetSettingEntry rule(String s1, String s2, String s3) {
        return CarpetSettingEntry.create(s1, s2, s3);
    }

    private static void set_defaults() {
        CarpetSettingEntry[] RuleList = new CarpetSettingEntry[]{
                rule("commandTick", "commands", "Enables /tick command to control game speed")
                        .isACommand(),
                rule("commandPing", "commands", "Enables /ping for players to get their ping")
                        .isACommand(),
                rule("commandCarpetFill", "creative", "Enables /carpetfill command")
                        .extraInfo("This is an replica of /fill command for fillUpdates and fillLimits")
                        .isACommand(),
                rule("commandCarpetClone", "creative", "Enables /carpetclone command")
                        .extraInfo("This is an replica of /clone command for fillUpdates and fillLimits")
                        .isACommand(),
                rule("commandCarpetSetBlock", "creative", "Enables /carpetclone command")
                        .extraInfo("This is an replica of /setblock command for fillUpdates")
                        .isACommand(),
                rule("fillUpdates", "creative", "fill/clone/setblock and structure blocks cause block updates").defaultTrue(),
                rule("fillLimit", "creative", "Customizable fill/clone volume limit")
                        .choices("32768", "32768 250000 1000000")
                        .setNotStrict(),
                rule("hopperCounters", "commands creative survival", "hoppers pointing to wool will count items passing through them")
                        .extraInfo("Enables /counter command, and actions while placing red and green carpets on wool blocks",
                                "Use /counter <color?> reset to reset the counter, and /counter <color?> to query",
                                "In survival, place green carpet on same color wool to query, red to reset the counters",
                                "Counters are global and shared between players, 16 channels available",
                                "Items counted are destroyed, count up to one stack per tick per hopper")
                        .isACommand().boolAccelerate().defaultFalse(),
                rule("commandPlayer", "commands", "Enables /player command to control/spawn players").isACommand(),
                rule("commandLog",    "commands", "Enables /log command to monitor events in the game via chat and overlays").isACommand(),
                rule("commandSpawn",  "commands", "Enables /spawn command for spawn tracking").isACommand(),
                rule("explosionNoBlockDamage", "tnt", "Explosions won't destroy blocks"),
        };
        for (CarpetSettingEntry rule : RuleList) {
            settings_store.put(rule.getName(), rule);
        }

    }

    private static void notifyPlayersCommandsChanged() {
        if (QuickCarpet.minecraft_server == null) {
            return;
        }
        for (ServerPlayerEntity entityplayermp : QuickCarpet.minecraft_server.getPlayerManager().getPlayerList()) {
            QuickCarpet.minecraft_server.getCommandManager().sendCommandTree(entityplayermp);
        }
    }

    public static void apply_settings_from_conf(MinecraftServer server) {
        Map<String, String> conf = read_conf(server);
        boolean is_locked = locked;
        locked = false;
        if (is_locked) {
            LOG.info("[CM]: Carpet Mod is locked by the administrator");
        }
        for (String key : conf.keySet()) {
            set(key, conf.get(key));
            LOG.info("[CM]: loaded setting " + key + " as " + conf.get(key) + " from carpet.conf");
        }
        locked = is_locked;
    }

    private static void disable_commands_by_default() {
        for (CarpetSettingEntry entry : settings_store.values()) {
            if (entry.getName().startsWith("command")) {
                entry.defaultFalse();
            }
        }
    }

    private static Map<String, String> read_conf(MinecraftServer server) {
        try {
            File settings_file = server.getLevelStorage().resolveFile(server.getLevelName(), "carpet.conf");
            BufferedReader b = new BufferedReader(new FileReader(settings_file));
            String line = "";
            Map<String, String> result = new HashMap<String, String>();
            while ((line = b.readLine()) != null) {
                line = line.replaceAll("\\r|\\n", "");
                if ("locked".equalsIgnoreCase(line)) {
                    disable_commands_by_default();
                    locked = true;
                }
                String[] fields = line.split("\\s+", 2);
                if (fields.length > 1) {
                    if (get(fields[0]) == FalseEntry) {
                        LOG.error("[CM]: Setting " + fields[0] + " is not a valid - ignoring...");
                        continue;
                    }
                    if (!(Arrays.asList(get(fields[0]).getOptions()).contains(fields[1])) && get(fields[0]).isStrict()) {
                        LOG.error("[CM]: The value of " + fields[1] + " for " + fields[0] + " is not valid - ignoring...");
                        continue;
                    }
                    result.put(fields[0], fields[1]);
                }
            }
            b.close();
            return result;
        } catch (FileNotFoundException e) {
            return new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }

    private static void write_conf(MinecraftServer server, Map<String, String> values) {
        if (locked) return;
        try {
            File settings_file = server.getLevelStorage().resolveFile(server.getLevelName(), "carpet.conf");
            FileWriter fw = new FileWriter(settings_file);
            for (String key : values.keySet()) {
                fw.write(key + " " + values.get(key) + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("[CM]: failed write the carpet.conf");
        }
    }

    // stores different defaults in the file
    public static boolean setDefaultRule(MinecraftServer server, String setting_name, String string_value) {
        if (locked) return false;
        if (settings_store.containsKey(setting_name)) {
            Map<String, String> conf = read_conf(server);
            conf.put(setting_name, string_value);
            write_conf(server, conf);
            set(setting_name, string_value);
            return true;
        }
        return false;
    }

    // removes overrides of the default values in the file
    public static boolean removeDefaultRule(MinecraftServer server, String setting_name) {
        if (locked) return false;
        if (settings_store.containsKey(setting_name)) {
            Map<String, String> conf = read_conf(server);
            conf.remove(setting_name);
            write_conf(server, conf);
            set(setting_name, get(setting_name).getDefault());
            return true;
        }
        return false;
    }

    //changes setting temporarily
    public static boolean set(String setting_name, String string_value) {
        CarpetSettingEntry en = get(setting_name);
        if (en != FalseEntry) {
            en.set(string_value);
            //reload_stat(setting_name);
            //CarpetClientRuleChanger.updateCarpetClientsRule(setting_name, string_value);
            return true;
        }
        return false;
    }

    // used as CarpetSettings.get("pushLimit").integer to get the int value of push limit
    public static CarpetSettingEntry get(String setting_name) {
        if (!settings_store.containsKey(setting_name)) {
            return FalseEntry;
        }
        return settings_store.get(setting_name);
    }

    public static int getInt(String setting_name) {
        return get(setting_name).getIntegerValue();
    }

    public static boolean getBool(String setting_name) {
        return get(setting_name).getBoolValue();
    }

    public static String getString(String setting_name) {
        return get(setting_name).getStringValue();
    }

    public static float getFloat(String setting_name) {
        return get(setting_name).getFloatValue();
    }

    public static CarpetSettingEntry[] findAll(String tag) {
        ArrayList<CarpetSettingEntry> res = new ArrayList<CarpetSettingEntry>();
        for (String rule : settings_store.keySet().stream().sorted().collect(Collectors.toList())) {
            if (tag == null || settings_store.get(rule).matches(tag)) {
                res.add(settings_store.get(rule));
            }
        }
        return res.toArray(new CarpetSettingEntry[0]);
    }

    public static CarpetSettingEntry[] find_nondefault(MinecraftServer server) {
        ArrayList<CarpetSettingEntry> res = new ArrayList<CarpetSettingEntry>();
        Map<String, String> defaults = read_conf(server);
        for (String rule : settings_store.keySet().stream().sorted().collect(Collectors.toList())) {
            if (!settings_store.get(rule).isDefault() || defaults.containsKey(rule)) {
                res.add(settings_store.get(rule));
            }
        }
        return res.toArray(new CarpetSettingEntry[0]);
    }

    public static CarpetSettingEntry[] findStartupOverrides(MinecraftServer server) {
        ArrayList<CarpetSettingEntry> res = new ArrayList<CarpetSettingEntry>();
        if (locked) return res.toArray(new CarpetSettingEntry[0]);
        Map<String, String> defaults = read_conf(server);
        for (String rule : settings_store.keySet().stream().sorted().collect(Collectors.toList())) {
            if (defaults.containsKey(rule)) {
                res.add(settings_store.get(rule));
            }
        }
        return res.toArray(new CarpetSettingEntry[0]);
    }

    public static String[] toStringArray(CarpetSettingEntry[] entry_array) {
        return Stream.of(entry_array).map(CarpetSettingEntry::getName).toArray(String[]::new);
    }

    public static ArrayList<CarpetSettingEntry> getAllCarpetSettings() {
        ArrayList<CarpetSettingEntry> res = new ArrayList<CarpetSettingEntry>();
        for (String rule : settings_store.keySet().stream().sorted().collect(Collectors.toList())) {
            res.add(settings_store.get(rule));
        }

        return res;
    }

    public static CarpetSettingEntry getCarpetSetting(String rule) {
        return settings_store.get(rule);
    }

    public static void resetToVanilla() {
        for (String rule : settings_store.keySet()) {
            get(rule).reset();
            //reload_stat(rule);
        }
    }

    public static void resetToUserDefaults(MinecraftServer server) {
        resetToVanilla();
        apply_settings_from_conf(server);
    }

    public static void resetToCreative() {
        resetToBugFixes();
        set("fillLimit", "500000");
        set("fillUpdates", "false");
        set("portalCreativeDelay", "true");
        set("portalCaching", "true");
        set("flippinCactus", "true");
        set("hopperCounters", "true");
        set("antiCheatSpeed", "true");

    }

    public static void resetToSurvival() {
        resetToBugFixes();
        set("ctrlQCraftingFix", "true");
        set("persistentParrots", "true");
        set("stackableEmptyShulkerBoxes", "true");
        set("flippinCactus", "true");
        set("hopperCounters", "true");
        set("carpets", "true");
        set("missingTools", "true");
        set("portalCaching", "true");
        set("miningGhostBlocksFix", "true");
    }

    public static void resetToBugFixes() {
        resetToVanilla();
        set("portalSuffocationFix", "true");
        set("pistonGhostBlocksFix", "serverOnly");
        set("portalTeleportationFix", "true");
        set("entityDuplicationFix", "true");
        set("inconsistentRedstoneTorchesFix", "true");
        set("llamaOverfeedingFix", "true");
        set("invisibilityFix", "true");
        set("potionsDespawnFix", "true");
        set("liquidsNotRandom", "true");
        set("mobsDontControlMinecarts", "true");
        set("breedingMountingDisabled", "true");
        set("growingUpWallJump", "true");
        set("reloadSuffocationFix", "true");
        set("watchdogFix", "true");
        set("unloadedEntityFix", "true");
        set("hopperDuplicationFix", "true");
        set("calmNetherFires", "true");
    }

    public static class CarpetSettingEntry {
        private String rule;
        private String string;
        private int integer;
        private boolean bool;
        private float flt;
        private String[] options;
        private String[] tags;
        private String toast;
        private String[] extra_info;
        private String default_string_value;
        private boolean isFloat;
        private boolean strict;
        private List<Consumer<String>> validators;

        private CarpetSettingEntry(String rule_name, String tags_string, String toast_string) {
            set("false");
            rule = rule_name;
            default_string_value = string;
            tags = tags_string.split("\\s+"); // never empty
            toast = toast_string;
            options = "true false".split("\\s+");
            isFloat = false;
            extra_info = null;
            strict = true;
            validators = null;
        }

        //factory
        public static CarpetSettingEntry create(String rule_name, String tags, String toast) {
            return new CarpetSettingEntry(rule_name, tags, toast);
        }

        public CarpetSettingEntry defaultTrue() {
            set("true");
            default_string_value = string;
            options = "true false".split("\\s+");
            return this;
        }

        public CarpetSettingEntry validate(Consumer<String> method) {
            if (validators == null) {
                validators = new ArrayList<>();
            }
            validators.add(method);
            return this;
        }

        public CarpetSettingEntry boolAccelerate() {
            Consumer<String> validator = (name) -> {
                try {
                    Field f = QuickCarpetSettings.class.getDeclaredField("b_" + name);
                    f.setBoolean(null, QuickCarpetSettings.getBool(name));
                } catch (IllegalAccessException e) {
                    QuickCarpetSettings.LOG.error("[CM Error] rule " + name + " has wrong access to boolean accelerator");
                } catch (NoSuchFieldException e) {
                    QuickCarpetSettings.LOG.error("[CM Error] rule " + name + " doesn't have a boolean accelerator");
                }
            };
            return validate(validator);
        }

        public CarpetSettingEntry numAccelerate() {
            Consumer<String> validator = (name) -> {
                try {
                    Field f = QuickCarpetSettings.class.getDeclaredField("n_" + name);
                    if (QuickCarpetSettings.get(name).isFloat) {
                        f.setDouble(null, (double) QuickCarpetSettings.getFloat(name));
                    } else {
                        f.setInt(null, QuickCarpetSettings.getInt(name));
                    }
                } catch (IllegalAccessException e) {
                    QuickCarpetSettings.LOG.error("[CM Error] rule " + name + " wrong type of numerical accelerator");
                } catch (NoSuchFieldException e) {
                    QuickCarpetSettings.LOG.error("[CM Error] rule " + name + " doesn't have a numerical accelerator");
                }
            };
            return validate(validator);
        }


        public CarpetSettingEntry isACommand() {
            return this.defaultTrue().validate((s) -> notifyPlayersCommandsChanged());
        }

        public CarpetSettingEntry defaultFalse() {
            set("false");
            default_string_value = string;
            options = "true false".split("\\s+");
            return this;
        }

        public CarpetSettingEntry choices(String defaults, String options_string) {
            set(defaults);
            default_string_value = string;
            options = options_string.split("\\s+");
            return this;
        }

        public CarpetSettingEntry extraInfo(String... extra_info_string) {
            extra_info = extra_info_string;
            return this;
        }

        public CarpetSettingEntry setFloat() {
            isFloat = true;
            strict = false;
            return this;
        }

        public CarpetSettingEntry setNotStrict() {
            strict = false;
            return this;
        }

        private void set(String unparsed) {
            string = unparsed;
            try {
                integer = Integer.parseInt(unparsed);
            } catch (NumberFormatException e) {
                integer = 0;
            }
            try {
                flt = Float.parseFloat(unparsed);
            } catch (NumberFormatException e) {
                flt = 0.0F;
            }
            bool = (integer > 0) ? true : Boolean.parseBoolean(unparsed);
            if (validators != null) {
                validators.forEach((r) -> r.accept(this.getName()));
            }
        }

        //accessors
        public boolean isDefault() {
            return string.equals(default_string_value);
        }

        public String getDefault() {
            return default_string_value;
        }

        public String toString() {
            return rule + ": " + string;
        }

        public String getToast() {
            return toast;
        }

        public String[] getInfo() {
            return extra_info == null ? new String[0] : extra_info;
        }

        public String[] getOptions() {
            return options;
        }

        public String[] getTags() {
            return tags;
        }

        public String getName() {
            return rule;
        }

        public String getStringValue() {
            return string;
        }

        public boolean getBoolValue() {
            return bool;
        }

        public int getIntegerValue() {
            return integer;
        }

        public float getFloatValue() {
            return flt;
        }

        public boolean getIsFloat() {
            return isFloat;
        }

        //actual stuff
        public void reset() {
            set(default_string_value);
        }

        public boolean matches(String tag) {
            tag = tag.toLowerCase();
            if (rule.toLowerCase().contains(tag)) {
                return true;
            }
            for (String t : tags) {
                if (tag.equalsIgnoreCase(t)) {
                    return true;
                }
            }
            return false;
        }

        public String getNextValue() {
            int i;
            for (i = 0; i < options.length; i++) {
                if (options[i].equals(string)) {
                    break;
                }
            }
            i++;
            return options[i % options.length];
        }

        public boolean isStrict() {
            return strict;
        }
    }
}
