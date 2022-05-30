package quickcarpet.utils;

import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import quickcarpet.Build;
import quickcarpet.QuickCarpet;
import quickcarpet.settings.Settings;

import static quickcarpet.utils.Messenger.*;

public interface Constants {
    interface SetBlockState {
        int DEFAULT = Block.NOTIFY_ALL;

        int UPDATE_NEIGHBORS = Block.NOTIFY_NEIGHBORS;
        int SEND_TO_CLIENT = Block.NOTIFY_LISTENERS;
        int NO_RERENDER = Block.NO_REDRAW;
        int RERENDER_MAIN_THREAD = Block.REDRAW_ON_MAIN_THREAD;
        int NO_OBSERVER_UPDATE = Block.FORCE_STATE;
        int SKIP_DROPS = Block.SKIP_DROPS;
        int CALL_ON_ADDED_ON_REMOVED = Block.MOVED;
        int SKIP_LIGHTING_UPDATES = Block.SKIP_LIGHTING_UPDATES;
        int NO_FILL_UPDATE = 1024;

        static int modifyFlags(int flags) {
            if (Settings.fillUpdates) return flags;
            return (flags & ~UPDATE_NEIGHBORS) | NO_FILL_UPDATE;
        }
    }

    interface CarpetCommand {
        interface Keys {
            String TITLE_ALL = "command.carpet.title.all";
            String TITLE_STARTUP = "command.carpet.title.startup";
            String TITLE_SEARCH = "command.carpet.title.search";
            String TITLE_CURRENT = "command.carpet.title.current";
            String RULE_INVALID_VALUE = "command.carpet.rule.invalidValue";
            String RULE_VALUE = "command.carpet.rule.value";
            String RULE_DEPRECATED = "command.carpet.rule.deprecated";
            String RULE_DISABLED = "command.carpet.rule.disabled";
            String REFRESH = "command.carpet.refresh";
            String CATEGORIES = "command.carpet.categories";
            String OPTIONS = "command.carpet.options";
            String OPTION_DISABLED = "command.carpet.option.disabled";
            String SWITCH = "command.carpet.switch";
            String LIST = "command.carpet.list";
            String CHANGE_PERMANENTLY = "command.carpet.changePermanently";
            String CHANGE_PERMANENTLY_HOVER = "command.carpet.changePermanently.hover";
            String SET_DEFAULT_SUCCESS = "command.carpet.setDefault.success";
            String REMOVE_DEFAULT_SUCCESS = "command.carpet.removeDefault.success";
            String VERSION = "command.carpet.version";
            String VERSION_BUILT_FOR = "command.carpet.version.builtFor";
            String VERSION_UNCOMMITTED = "command.carpet.version.uncommitted";
            String MODULES = "command.carpet.modules";
            String MODULES_LIST = "command.carpet.modules.list";
            String BROWSE_CATEGORIES = "command.carpet.browseCategories";
            String RULE_CURRENT_VALUE = "carpet.rule.currentValue";
            String RULE_VALUE_DEFAULT = "carpet.rule.value.default";
            String RULE_VALUE_MODIFIED = "carpet.rule.value.modified";
        }
        interface Texts {
            Text TITLE_ALL = t(Keys.TITLE_ALL, Build.NAME);
            Text TITLE_STARTUP = t(Keys.TITLE_STARTUP, Build.NAME);
            Text TITLE_CURRENT = t(Keys.TITLE_CURRENT, Build.NAME);
            Text REFRESH_TOOLTIP = ts(Keys.REFRESH, Formatting.GRAY);
            Text DISABLED_SUFFIX = c(s(" ("), t(Keys.RULE_DISABLED), s(")")).formatted(Formatting.RED);
            Text DISABLED = ts(Keys.RULE_DISABLED, Formatting.GRAY);
            Text OPTIONS_PREFIX = c(t(Keys.OPTIONS), s("[", Formatting.YELLOW));
            Text OPTIONS_SUFFIX = s("]", Formatting.YELLOW);
            Text CATEGORIES = t(Keys.CATEGORIES);
            Text CHANGE_PERMANENTLY = hoverText(c(s("["), t(Keys.CHANGE_PERMANENTLY), s("]")).formatted(Formatting.AQUA), t(Keys.CHANGE_PERMANENTLY_HOVER));
            Text LOCKED = ts(OtherKeys.LOCKED, Formatting.GRAY);
            Text DEFAULT = t(Keys.RULE_VALUE_DEFAULT);
            Text MODIFIED = t(Keys.RULE_VALUE_MODIFIED);
            Text VERSION = ts(Keys.VERSION, Formatting.DARK_GREEN, Build.NAME, QuickCarpet.getFullVersionString());
            Text VERSION_BUILT_FOR = ts(Keys.VERSION_BUILT_FOR, Formatting.YELLOW, Build.MINECRAFT_VERSION);
            Text UNCOMMITTED = ts(Keys.VERSION_UNCOMMITTED, Formatting.RED);
            Text MODULES = ts(Keys.MODULES, Formatting.BOLD);
            Text MODULES_LIST = ts(Keys.MODULES_LIST, Formatting.GRAY);
            Text BROWSE_CATEGORIES = ts(Keys.BROWSE_CATEGORIES, Formatting.BOLD);
        }
    }
    interface CounterCommand {
        interface Texts {
            Text RESET_SUCCESS = t(Keys.RESET_SUCCESS);
        }
        interface Keys {
            String UNKNOWN = "command.counter.unknown";
            String RESET_SUCCESS = "command.counter.reset.success";
            String RESET_ONE_SUCCESS = "command.counter.reset.one.success";
        }
    }
    interface DataTrackerCommand {
        interface Texts {
            Text NO_ENTRIES = ts(Keys.NO_ENTRIES, GRAY_ITALIC);
        }
        interface Keys {
            String NO_ENTRIES = "command.datatracker.no_entries";
            String ENTRY = "command.datatracker.entry";
        }
    }
    interface FixCommand {
        interface Keys {
            String FIXING = "command.fix.fixing";
            String FIXED = "command.fix.fixed";
        }
    }
    interface LogCommand {
        interface Texts {
            Text PLAYER_ONLY = ts(Keys.PLAYER_ONLY, Formatting.RED);
            Text AVAILABLE_OPTIONS = t(Keys.AVAILABLE_OPTIONS);
            Text SUBSCRIBED = ts(Keys.SUBSCRIBED, Formatting.GREEN);
            Text UNSUBSCRIBED_ALL = ts(Keys.UNSUBSCRIBED_ALL, GRAY_ITALIC);
            Text ACTION_SUBSCRIBE = t(Keys.ACTION_SUBSCRIBE);
            Text NO_PLAYER_SPECIFIED = ts(Keys.NO_PLAYER_SPECIFIED, Formatting.RED);
        }
        interface Keys {
            String PLAYER_ONLY = "command.log.playerOnly";
            String AVAILABLE_OPTIONS = "command.log.availableOptions";
            String SUBSCRIBED = "command.log.subscribed";
            String SUBSCRIBED_TO = "command.log.subscribedTo";
            String SUBSCRIBED_TO_PLAYER = "command.log.subscribedTo.player";
            String SUBSCRIBED_TO_OPTION = "command.log.subscribedTo.option";
            String SUBSCRIBED_TO_OPTION_PLAYER = "command.log.subscribedTo.option.player";
            String UNSUBSCRIBED = "command.log.unsubscribed";
            String UNSUBSCRIBED_PLAYER = "command.log.unsubscribed.player";
            String UNSUBSCRIBED_ALL = "command.log.unsubscribed.all";
            String ACTION_SUBSCRIBE = "command.log.action.subscribe";
            String ACTION_SUBSCRIBE_TO = "command.log.action.subscribeTo";
            String ACTION_SUBSCRIBE_TO_OPTION = "command.log.action.subscribeTo.option";
            String ACTION_UNSUBSCRIBE_HOVER = "command.log.action.unsubscribe.hover";
            String NO_PLAYER_SPECIFIED = "command.log.noPlayerSpecified";
            String UNAVAILABLE = "command.log.unavailable";
            String UNKNOWN = "command.log.unknown";
        }
    }
    interface MeasureCommand {
        interface Keys {
            String TITLE = "command.measure.title";
            String LINE = "command.measure.line";
            String PREFIX = "command.measure.";
        }
    }
    interface PingCommand {
        interface Keys {
            String RESULT = "command.ping.result";
        }
    }
    interface PlayerCommand {
        interface Texts {
            Text ONLY_EXISTING = ts(Keys.ONLY_EXISTING, Formatting.RED);
            Text NOT_OPERATOR = ts(Keys.NOT_OPERATOR, Formatting.RED);
            Text NOT_FAKE = ts(Keys.NOT_FAKE, Formatting.RED);
            Text SHADOW_FAKE = ts(Keys.SHADOW_FAKE, Formatting.RED);
        }
        interface Keys {
            String ONLY_EXISTING = "command.player.onlyExisting";
            String NOT_OPERATOR = "command.player.notOperator";
            String NOT_FAKE = "command.player.notFake";
            String ALREADY_ONLINE = "command.player.alreadyOnline";
            String BANNED = "command.player.banned";
            String WHITELISTED = "command.player.whitelisted";
            String DOES_NOT_EXIST = "command.player.doesNotExist";
            String SHADOW_FAKE = "command.player.shadowFake";
        }
    }
    interface SpawnCommand {
        interface Texts {
            Text TRACKING_INACTIVE = ts(Keys.TRACKING_INACTIVE, Formatting.GOLD);
            Text TRACKING_ACTIVE = ts(Keys.TRACKING_ACTIVE, Formatting.GOLD);
            Text TRACKING_STARTED = ts(Keys.TRACKING_STARTED, Formatting.DARK_GREEN);
            Text TRACKING_STOPPED = ts(Keys.TRACKING_STOPPED, Formatting.DARK_GREEN);
            Text LIST_ENTRY_CANT_SPAWN = ts(Keys.LIST_ENTRY_CANT_SPAWN, Formatting.RED);
            Text LIST_ENTRY_COLLIDES = ts(Keys.LIST_ENTRY_COLLIDES, Formatting.RED);
            Text LIST_ENTRY_CHANCE_HOVER = t(Keys.LIST_ENTRY_CHANCE_HOVER);
        }
        interface Keys {
            String TRACKING_INACTIVE = "command.spawn.tracking.inactive";
            String TRACKING_ACTIVE = "command.spawn.tracking.active";
            String TRACKING_STARTED = "command.spawn.tracking.started";
            String TRACKING_STOPPED = "command.spawn.tracking.stopped";
            String TRACKING_TITLE = "command.spawn.tracking.title";
            String TRACKING_CATEGORY = "command.spawn.tracking.category";
            String TRACKING_MOB = "command.spawn.tracking.mob";
            String MOBCAPS_TITLE = "command.spawn.mobcaps.title";
            String MOBCAPS_LINE = "command.spawn.mobcaps.line";
            String LIST_HIGHEST_BLOCK = "command.spawn.list.highestBlock";
            String LIST_LOWEST_BLOCK = "command.spawn.list.lowestBlock";
            String LIST_GROUP = "command.spawn.list.group";
            String LIST_ENTRY = "command.spawn.list.entry";
            String LIST_ENTRY_WEIGHT = "command.spawn.list.entry.weight";
            String LIST_ENTRY_PACK = "command.spawn.list.entry.pack";
            String LIST_ENTRY_CHANCE = "command.spawn.list.entry.chance";
            String LIST_ENTRY_CHANCE_HOVER = "command.spawn.list.entry.chance.hover";
            String LIST_ENTRY_PACK_RANGE = "command.spawn.list.entry.packRange";
            String LIST_ENTRY_CAN_SPAWN = "command.spawn.list.entry.canSpawn";
            String LIST_ENTRY_CANT_SPAWN = "command.spawn.list.entry.cantSpawn";
            String LIST_ENTRY_FITS = "command.spawn.list.entry.fits";
            String LIST_ENTRY_COLLIDES = "command.spawn.list.entry.collides";
        }
    }
    interface StateInfoCommand {
        interface Texts {
            Text BLOCK_STATE = t(Keys.BLOCK_STATE);
            Text FLUID_STATE = t(Keys.FLUID_STATE);
        }
        interface Keys {
            String BLOCK_STATE = "command.stateinfo.block_state";
            String FLUID_STATE = "command.stateinfo.fluid_state";
            String LINE = "command.stateinfo.line";
        }
    }
    interface TickCommand {
        interface Texts {
            Text FREEZE = ts(Keys.FREEZE, GRAY_ITALIC);
            Text UNFREEZE = ts(Keys.UNFREEZE, GRAY_ITALIC);
            Text STATS_LOADAVG = t(Keys.STATS_LOADAVG);
            Text STATS_MINAVGMAX = t(Keys.STATS_MINAVGMAX);
            Text STATS_LAGTICKS = t(Keys.STATS_LAGTICKS);
            Text STATS_PERCENTILES = t(Keys.STATS_PERCENTILES);
            Text WARP_INTERRUPTED = ts(Keys.WARP_INTERRUPTED, GRAY_ITALIC);
            Text WARP_ACTIVE = ts(Keys.WARP_ACTIVE, Formatting.GREEN);
            Text WARP_START = ts(Keys.WARP_START, GRAY_ITALIC);
            Text WARP_STATUS_INACTIVE = ts(Keys.WARP_STATUS_INACTIVE, Formatting.YELLOW);
        }
        interface Keys {
            String CURRENT = "command.tick.current";
            String FREEZE = "command.tick.freeze";
            String UNFREEZE = "command.tick.unfreeze";
            String STATS = "command.tick.stats";
            String STATS_LOADAVG = "command.tick.stats.loadavg";
            String STATS_MINAVGMAX = "command.tick.stats.minavgmax";
            String STATS_LAGTICKS = "command.tick.stats.lagticks";
            String STATS_PERCENTILES = "command.tick.stats.percentiles";
            String WARP_CALLBACK_FAILED = "command.tick.warp.callback.failed";
            String WARP_CALLBACK_FAILED_UNKNOWN = "command.tick.warp.callback.failed.unknown";
            String WARP_COMPLETED = "command.tick.warp.completed";
            String WARP_INTERRUPTED = "command.tick.warp.interrupted";
            String WARP_ACTIVE = "command.tick.warp.active";
            String WARP_START = "command.tick.warp.start";
            String WARP_STATUS_INACTIVE = "command.tick.warp.status.inactive";
            String WARP_STATUS_ACTIVE = "command.tick.warp.status.active";
            String WARP_STATUS_STARTED_BY = "command.tick.warp.status.startedBy";
            String WARP_STATUS_CALLBACK = "command.tick.warp.status.callback";
        }
    }
    interface WaypointCommand {
        interface Texts {
            Text LIST_INVALID_PAGE = t(Keys.LIST_INVALID_PAGE);
            Text LIST_NONE = ts(Keys.LIST_NONE, Formatting.GOLD);
            Text LIST_HEADER_ALL = t(Keys.LIST_HEADER_ALL);
            Text LIST_PAGE_PREVIOUS = t(Keys.LIST_PAGE_PREVIOUS);
            Text LIST_PAGE_NEXT = t(Keys.LIST_PAGE_NEXT);
        }
        interface Keys {
            String ERROR_EXISTS = "command.waypoint.error.exists";
            String ERROR_NOT_FOUND = "command.waypoint.error.notFound";
            String ADDED = "command.waypoint.added";
            String LIST_INVALID_PAGE = "command.waypoint.list.invalidPage";
            String LIST_NONE = "command.waypoint.list.none";
            String LIST_HEADER_ALL = "command.waypoint.list.header.all";
            String LIST_HEADER_DIMENSION = "command.waypoint.list.header.dimension";
            String LIST_HEADER_CREATOR = "command.waypoint.list.header.creator";
            String LIST_PAGE = "command.waypoint.list.page";
            String LIST_PAGE_PREVIOUS = "command.waypoint.list.page.previous";
            String LIST_PAGE_NEXT = "command.waypoint.list.page.next";
            String LIST_ENTRY = "command.waypoint.list.entry";
            String LIST_ENTRY_CREATOR = "command.waypoint.list.entry.creator";
            String REMOVE_NOT_ALLOWED = "command.waypoint.remove.notAllowed";
            String REMOVE_SUCCESS = "command.waypoint.remove.success";
        }
    }
    interface Counter {
        interface Texts {
            Text NONE = ts(Keys.NONE, Formatting.GOLD);
            Text REAL_TIME = t(Keys.REAL_TIME);
            Text ACTION_RESET = ts(Keys.ACTION_RESET, Formatting.GRAY);
        }
        interface Keys {
            String NONE = "counter.none";
            String NONE_COLOR = "counter.none.color";
            String NONE_COLOR_TIMED = "counter.none.color.timed";
            String REAL_TIME = "counter.realTime";
            String FORMAT = "counter.format";
            String FORMAT_ITEM = "counter.format.item";
            String ACTION_RESET = "counter.action.reset";
        }
    }
    interface Profiler {
        interface Texts {
            Text TOP_10_COUNTS = t(Keys.TOP_10_COUNTS);
            Text TOP_10_GROSSING = t(Keys.TOP_10_GROSSING);
        }
        interface Keys {
            String SECTION_FORMAT = "carpet.profiler.section.format";
            String SECTION_PREFIX = "carpet.profiler.section.";
            String SECTION_FORMAT_SUFFIX = ".format";
            String TITLE = "carpet.profiler.title";
            String TOP_10_COUNTS = "carpet.profiler.top_10_counts";
            String TOP_10_GROSSING = "carpet.profiler.top_10_grossing";
            String ENTITY_LINE = "carpet.profiler.entity.line";
        }
    }
    interface Validator {
        interface Texts {
            TranslatableText TNT_ANGLE = t(Keys.TNT_ANGLE);
            TranslatableText VIEW_DISTANCE_INTEGRATED = t(Keys.VIEW_DISTANCE_INTEGRATED);
        }
        interface Keys {
            String RANGE = "carpet.validator.range";
            String TNT_ANGLE = "carpet.validator.tntAngle";
            String VIEW_DISTANCE_INTEGRATED = "carpet.validator.viewDistance.integrated";
        }
    }
    interface Client {
        interface Keys {
            String TITLE_CONFIGS = "quickcarpet.gui.title.configs";
            String HOTKEYS_CATEGORY_GENERIC_HOTKEYS = "quickcarpet.hotkeys.category.generic_hotkeys";
            String HOTKEYS_CATEGORY_RENDERING_HOTKEYS = "quickcarpet.hotkeys.category.rendering_hotkeys";
            String BUTTON_CONFIG_GUI_GENERIC = "quickcarpet.gui.button.config_gui.generic";
            String BUTTON_CONFIG_GUI_RENDERING = "quickcarpet.gui.button.config_gui.rendering";
            String BUTTON_CONFIG_GUI_GENERIC_HOTKEYS = "quickcarpet.gui.button.config_gui.generic_hotkeys";
            String BUTTON_CONFIG_GUI_RENDERING_HOTKEYS = "quickcarpet.gui.button.config_gui.rendering_hotkeys";
        }
    }
    interface OtherKeys {
        String LOCKED = "carpet.locked";
        String GC_LOGGER_UNAVAILABLE = "logger.gc.unavailable";
        String TILE_TICK_LIMIT_REACHED = "logger.tileTickLimit.message";
        String WEATHER_LOG_TYPE_PREFIX = "logger.weather.";
        String WEATHER_LOG_ACTIVE_SUFFIX = ".active";
        String WEATHER_LOG_INACTIVE_SUFFIX = ".inactive";
        String STATE_INFO_PROVIDER_2 = "state_info_provider.2";
        String KILLS_LOG_1 = "logger.kills.1";
        String KILLS_LOG_SWEEPING_1 = "logger.kills.sweeping.1";
        String KILLS_LOG_SWEEPING_N = "logger.kills.sweeping.n";
        String DAMAGE_LOG_REGISTER = "logger.damage.register";
        String DAMAGE_LOG_REGISTER_ATTACKER = "logger.damage.registerAttacker";
        String DAMAGE_LOG_REDUCE_0 = "logger.damage.reduce.0";
        String DAMAGE_LOG_REDUCE = "logger.damage.reduce";
        String DAMAGE_LOG_INCREASE = "logger.damage.increase";
        String DAMAGE_LOG_REASON_PREFIX = "logger.damage.reason.";
        String DAMAGE_LOG_FINAL = "logger.damage.final";
    }
}
