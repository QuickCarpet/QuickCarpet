package quickcarpet;

import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import quickcarpet.api.QuickCarpetClientAPI;
import quickcarpet.api.settings.ParsedRule;
import quickcarpet.client.ClientInit;
import quickcarpet.client.ClientPluginChannelManager;
import quickcarpet.client.ClientPubSubListener;
import quickcarpet.client.ClientRulesChannel;
import quickcarpet.helper.TickSpeed;
import quickcarpet.settings.Settings;

@Environment(EnvType.CLIENT)
public class QuickCarpetClient implements QuickCarpetClientAPI {
    private static QuickCarpetClient instance = new QuickCarpetClient();
    private final MinecraftClient minecraftClient;
    private final ClientRulesChannel rulesChannel;
    private final ClientPubSubListener pubSubListener;
    public TickSpeed tickSpeed = new TickSpeed(null);

    public QuickCarpetClient() {
        instance = this;
        minecraftClient = MinecraftClient.getInstance();
        ClientPluginChannelManager.INSTANCE.register(rulesChannel = new ClientRulesChannel());
        ClientPluginChannelManager.INSTANCE.register(pubSubListener = new ClientPubSubListener());
        try {
            new MaLiLibInitializer().run();
        } catch (LinkageError e) {
            //if (FabricLoader.getInstance().isModLoaded("malilib")) {
                e.printStackTrace();
            //}
        }
    }

    @Override
    public quickcarpet.api.network.client.ClientPluginChannelManager getPluginChannelManager() {
        return ClientPluginChannelManager.INSTANCE;
    }

    public static QuickCarpetClient getInstance() {
        //noinspection ResultOfMethodCallIgnored
        QuickCarpet.getInstance();
        return instance;
    }

    // separate class to avoid loading malilib classes outside the try-catch
    private static class MaLiLibInitializer implements Runnable {
        @Override
        public void run() {
            InitializationHandler.getInstance().registerInitializationHandler(new ClientInit());
        }
    }

    public boolean isSingleplayer() {
        return minecraftClient.isInSingleplayer();
    }

    public void onJoinServer() {
        tickSpeed = new TickSpeed(null);
        ClientPluginChannelManager.INSTANCE.sendRegisterPacket(minecraftClient.getNetworkHandler());
        pubSubListener.subscribe(
            "minecraft.performance.tps",
            "carpet.tick-rate.tps-goal",
            "carpet.tick-rate.paused",
            "carpet.tick-rate.step"
        );
        if (!isSingleplayer()) {
            for (ParsedRule<?> rule : Settings.MANAGER.getRules()) rule.resetToDefault(false);
        }
    }

    public void onLeaveServer() {
        tickSpeed = new TickSpeed(null);
    }

    public void tick() {
        tickSpeed.tick();
    }

    public static class Provider implements QuickCarpetClientAPI.Provider {
        @Override
        public QuickCarpetClientAPI getInstance() {
            return QuickCarpetClient.getInstance();
        }
    }
}
