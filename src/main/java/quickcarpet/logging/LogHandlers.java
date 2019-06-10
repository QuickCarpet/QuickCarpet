package quickcarpet.logging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogHandlers {
    private static final Map<String, LogHandler.LogHandlerCreator> CREATORS = new HashMap<>();

    static {
        registerCreator("chat", extraArgs -> LogHandler.CHAT);
        registerCreator("hud", extraArgs -> LogHandler.HUD);
        // registerCreator("command", CommandLogHandler::new);
    }

    private static void registerCreator(String name, LogHandler.LogHandlerCreator creator) {
        CREATORS.put(name, creator);
    }

    static LogHandler createHandler(String name, String... extraArgs) {
        return CREATORS.get(name).create(extraArgs);
    }

    static List<String> getHandlerNames() {
        return CREATORS.keySet().stream().sorted().collect(Collectors.toList());
    }
}
