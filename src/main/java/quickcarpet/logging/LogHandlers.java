package quickcarpet.logging;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogHandlers {
    public static final BiMap<String, LogHandler.LogHandlerCreator> CREATORS = HashBiMap.create();
    private static final Map<LogHandler, LogHandler.LogHandlerCreator> CREATOR_FOR_HANDLER = new HashMap<>();

    static {
        registerCreator("chat", extraArgs -> LogHandler.CHAT);
        registerCreator("hud", extraArgs -> LogHandler.HUD);
        registerCreator("command", new LogHandler.LogHandlerCreator() {
            @Override
            public LogHandler create(String... extraArgs) {
                return new CommandLogHandler(extraArgs);
            }

            @Override
            public boolean usesExtraArgs() {
                return true;
            }
        });
        registerCreator("action_bar", extraArgs -> LogHandler.ACTION_BAR);
    }

    private static void registerCreator(String name, LogHandler.LogHandlerCreator creator) {
        CREATORS.put(name, creator);
        if (!creator.usesExtraArgs()) {
            CREATOR_FOR_HANDLER.put(creator.create(), creator);
        }
    }

    public static LogHandler createHandler(String name, String... extraArgs) {
        return CREATORS.get(name).create(extraArgs);
    }

    public static LogHandler.LogHandlerCreator getCreator(LogHandler handler) {
        LogHandler.LogHandlerCreator creator = CREATOR_FOR_HANDLER.get(handler);
        if (creator != null) return creator;
        return handler.getCreator();
    }

    public static String getCreatorName(LogHandler.LogHandlerCreator creator) {
        return CREATORS.inverse().get(creator);
    }

    public static List<String> getHandlerNames() {
        return CREATORS.keySet().stream().sorted().collect(Collectors.toList());
    }
}
