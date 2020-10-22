package quickcarpet.api.module;

import quickcarpet.api.ServerEventListener;

import javax.annotation.Nonnull;

public interface QuickCarpetModule extends Comparable<QuickCarpetModule>, ServerEventListener {
    String getName();
    String getVersion();
    String getId();

    @Override
    default int compareTo(@Nonnull QuickCarpetModule o) {
        return getId().compareTo(o.getId());
    }
}
