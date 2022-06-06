package quickcarpet.api.network;

import net.minecraft.util.Identifier;

public interface PluginChannelManager<T extends PluginChannelHandler> {
    Identifier REGISTER = new Identifier("minecraft:register");
    Identifier UNREGISTER = new Identifier("minecraft:unregister");

    void register(T handler);
    void unregister(T handler);
}
