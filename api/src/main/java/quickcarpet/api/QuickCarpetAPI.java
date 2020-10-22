package quickcarpet.api;

import quickcarpet.api.module.ModuleHost;

public interface QuickCarpetAPI extends ModuleHost {
    static QuickCarpetAPI getInstance() {
        return ApiUtils.getInstance("quickcarpet.QuickCarpet", QuickCarpetAPI.class);
    }
}
