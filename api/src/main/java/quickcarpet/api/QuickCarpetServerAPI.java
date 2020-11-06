package quickcarpet.api;

import quickcarpet.api.network.server.ServerPluginChannelManager;

public interface QuickCarpetServerAPI {
    ServerPluginChannelManager getPluginChannelManager();

    static QuickCarpetServerAPI getInstance() {
        return ApiUtils.getInstance(Provider.class, QuickCarpetServerAPI.class);
    }

    interface Provider extends ApiProvider<QuickCarpetServerAPI> {}
}
