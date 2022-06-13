package quickcarpet.utils.mixin.extensions;

import quickcarpet.feature.player.PlayerActionPack;

public interface ActionPackOwner {
    PlayerActionPack quickcarpet$getActionPack();
    void quickcarpet$setActionPack(PlayerActionPack pack);
}
