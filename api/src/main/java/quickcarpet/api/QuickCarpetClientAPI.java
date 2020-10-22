package quickcarpet.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import quickcarpet.api.network.client.ClientPluginChannelManager;

@Environment(EnvType.CLIENT)
public interface QuickCarpetClientAPI {
    ClientPluginChannelManager getPluginChannelManager();

    static QuickCarpetClientAPI getInstance() {
        return ApiUtils.getInstance("quickcarpet.QuickCarpetClient", QuickCarpetClientAPI.class);
    }
}
