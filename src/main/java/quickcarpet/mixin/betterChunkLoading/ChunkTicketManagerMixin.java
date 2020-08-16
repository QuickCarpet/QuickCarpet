package quickcarpet.mixin.betterChunkLoading;

import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quickcarpet.QuickCarpet;
import quickcarpet.settings.Settings;

// Inject after Lithium
@Mixin(value = ChunkTicketManager.class, priority = 1100)
public class ChunkTicketManagerMixin {
    @Inject(method = "purge", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ChunkTicketManager;ticketsByPosition:Lit/unimi/dsi/fastutil/longs/Long2ObjectOpenHashMap;"), cancellable = true)
    private void purgeOnAutosave(CallbackInfo ci) {
        if (Settings.betterChunkLoading && QuickCarpet.minecraft_server.getTicks() % 900 != 0) ci.cancel();
    }
}
