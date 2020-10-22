package quickcarpet.api;

import quickcarpet.api.network.server.ServerPluginChannelManager;

public interface QuickCarpetServerAPI {
    ServerPluginChannelManager getPluginChannelManager();

    static QuickCarpetServerAPI getInstance() {
        return ApiUtils.getInstance("quickcarpet.QuickCarpetServer", QuickCarpetServerAPI.class);
    }
}
