package quickcarpet.api;

import quickcarpet.api.data.PlayerDataKey;
import quickcarpet.api.module.ModuleHost;

public interface QuickCarpetAPI extends ModuleHost {
    <T> PlayerDataKey<T> registerPlayerData(Class<T> type);

    static QuickCarpetAPI getInstance() {
        return ApiUtils.getInstance(Provider.class, QuickCarpetAPI.class);
    }

    interface Provider extends ApiProvider<QuickCarpetAPI> {}
}
