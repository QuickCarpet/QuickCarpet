package quickcarpet.network.impl;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import quickcarpet.api.network.server.ServerPluginChannelHandler;
import quickcarpet.api.network.server.ServerPluginChannelManager;

public class ToggleableChannelHandler implements ServerPluginChannelHandler {
    private final ServerPluginChannelManager channelManager;
    public final ServerPluginChannelHandler baseHandler;
    private boolean enabled;

    public ToggleableChannelHandler(ServerPluginChannelManager channelManager, ServerPluginChannelHandler baseHandler) {
        this(channelManager, baseHandler, true);
    }

    public ToggleableChannelHandler(ServerPluginChannelManager channelManager, ServerPluginChannelHandler baseHandler, boolean enabled) {
        this.channelManager = channelManager;
        this.baseHandler = baseHandler;
        this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            if (enabled) {
                channelManager.register(this);
            } else {
                channelManager.unregister(this);
            }
            this.enabled = enabled;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Identifier[] getChannels() {
        return baseHandler.getChannels();
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {
        if (enabled) baseHandler.onCustomPayload(packet, player);
    }

    @Override
    public boolean register(Identifier channel, ServerPlayerEntity player) {
        return enabled && baseHandler.register(channel, player);
    }

    @Override
    public void unregister(Identifier channel, ServerPlayerEntity player) {
        baseHandler.unregister(channel, player);
    }
}
