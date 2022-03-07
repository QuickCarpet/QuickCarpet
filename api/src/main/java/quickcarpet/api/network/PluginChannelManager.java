package quickcarpet.api.network;

import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface PluginChannelManager<T extends PluginChannelHandler> {
    Identifier REGISTER = new Identifier("minecraft:register");
    Identifier UNREGISTER = new Identifier("minecraft:unregister");
    @Deprecated(forRemoval = true)
    Logger LOG = LogManager.getLogger();

    void register(T handler);
    void unregister(T handler);
}
