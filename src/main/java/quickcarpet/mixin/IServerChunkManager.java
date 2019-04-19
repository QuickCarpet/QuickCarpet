package quickcarpet.mixin;

import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkManager.class)
public interface IServerChunkManager {
    @Accessor("ticketManager")
    ChunkTicketManager getTicketManager();
}
