package quickcarpet.api;

import java.util.ServiceLoader;

class ApiUtils {
    static <T, P extends ApiProvider<T>> T getInstance(Class<P> providerType, Class<T> type) {
        P provider = ServiceLoader.load(providerType).iterator().next();
        if (provider == null) {
            throw new IllegalStateException("No implementation of " + type.getSimpleName() + " found");
        }
        return provider.getInstance();
    }
}
