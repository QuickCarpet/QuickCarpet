package quickcarpet.utils.mixin.extensions;

import quickcarpet.helper.PlayerActionPack;

public interface ActionPackOwner {
    PlayerActionPack quickcarpet$getActionPack();
    void quickcarpet$setActionPack(PlayerActionPack pack);
}
