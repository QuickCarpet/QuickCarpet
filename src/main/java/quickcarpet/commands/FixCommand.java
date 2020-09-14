package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import quickcarpet.settings.Settings;

import java.util.EnumSet;

import static net.minecraft.command.argument.ColumnPosArgumentType.columnPos;
import static net.minecraft.command.argument.ColumnPosArgumentType.getColumnPos;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.m;
import static quickcarpet.utils.Messenger.t;

public class FixCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> fix = literal("fix")
            .requires(s -> s.hasPermissionLevel(Settings.commandFix))
            .then(literal("chunk").then(argument("block_pos", columnPos())
                .executes(c -> fixChunk(c.getSource(), getColumnPos(c, "block_pos")))));
        dispatcher.register(fix);
    }

    private static int fixChunk(ServerCommandSource source, ColumnPos pos) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        ChunkPos cPos = new ChunkPos(pos.x >> 4, pos.z >> 4);
        BlockView chunk = world.getExistingChunk(cPos.x, cPos.z);
        if (!(chunk instanceof WorldChunk)) throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
        WorldChunk worldChunk = (WorldChunk) chunk;
        m(source, t("command.fix.fixing", cPos.x, cPos.z));
        Heightmap.populateHeightmaps(worldChunk, EnumSet.allOf(Heightmap.Type.class));
        ((ServerLightingProvider) world.getLightingProvider()).light(worldChunk, false).thenRun(() -> {
            m(source, t("command.fix.fixed", cPos.x, cPos.z));
        });
        return 1;
    }
}
