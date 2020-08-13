package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
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

public class FixCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> distance = literal("fix")
            .requires(s -> s.hasPermissionLevel(Settings.commandFix))
            .then(literal("chunk")).then(argument("pos", columnPos())
                .executes(c -> fixChunk(c.getSource(), getColumnPos(c, "pos"))));
        dispatcher.register(distance);
    }

    private static int fixChunk(ServerCommandSource source, ColumnPos pos) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        BlockView chunk = world.getExistingChunk(pos.x >> 4, pos.z >> 4);
        if (!(chunk instanceof WorldChunk)) throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
        WorldChunk worldChunk = (WorldChunk) chunk;
        Heightmap.populateHeightmaps(worldChunk, EnumSet.allOf(Heightmap.Type.class));
        ((ServerLightingProvider) world.getLightingProvider()).light(worldChunk, worldChunk.isLightOn()).join();
        return 1;
    }
}
