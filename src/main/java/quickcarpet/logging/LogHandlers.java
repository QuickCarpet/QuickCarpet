package quickcarpet.logging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogHandlers {
    public static final Map<String, LogHandler.LogHandlerCreator> CREATORS = new HashMap<>();

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
    }

    public static LogHandler createHandler(String name, String... extraArgs) {
        return CREATORS.get(name).create(extraArgs);
    }

    public static List<String> getHandlerNames() {
        return CREATORS.keySet().stream().sorted().collect(Collectors.toList());
    }
}
