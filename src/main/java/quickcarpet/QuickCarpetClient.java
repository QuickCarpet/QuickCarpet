package quickcarpet;

import net.minecraft.client.MinecraftClient;
import quickcarpet.client.ClientPluginChannelManager;
import quickcarpet.client.ClientPubSubListener;
import quickcarpet.client.ClientRulesChannel;
import quickcarpet.helper.TickSpeed;
import quickcarpet.settings.ParsedRule;
import quickcarpet.settings.Settings;

public class QuickCarpetClient {
    private final MinecraftClient minecraftClient;
    private final ClientRulesChannel rulesChannel;
    private final ClientPubSubListener pubSubListener;

    public QuickCarpetClient() {
        minecraftClient = MinecraftClient.getInstance();
        ClientPluginChannelManager.INSTANCE.register(rulesChannel = new ClientRulesChannel());
        ClientPluginChannelManager.INSTANCE.register(pubSubListener = new ClientPubSubListener());
    }

    public boolean isSingleplayer() {
        return minecraftClient.isInSingleplayer();
    }

    public void onJoinServer() {
        if (!isSingleplayer()) {
            TickSpeed.reset();
            for (ParsedRule<?> rule : Settings.MANAGER.getRules()) rule.resetToDefault(false);
            ClientPluginChannelManager.INSTANCE.sendRegisterPacket(minecraftClient.getNetworkHandler());
            pubSubListener.subscribe("minecraft.performance.tps", "carpet.tick-rate.tps-goal");
        }
    }
}
