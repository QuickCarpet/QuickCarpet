package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TextComponent;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.helper.Mobcaps;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Messenger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("spawn").
                requires((player) -> Settings.commandSpawn).
                then(literal("mobcaps").
                    executes(c -> sendMobcaps(c.getSource(), null)).
                    then(argument("dimension", DimensionArgumentType.create()).
                        executes(c -> sendMobcaps(c.getSource(), c.getArgument("dimension", DimensionType.class)))
                    )
                );
        dispatcher.register(builder);
    }

    private static int sendMobcaps(ServerCommandSource source, DimensionType dimension) {
        if (dimension == null) dimension = source.getWorld().getDimension().getType();
        Map<EntityCategory, Pair<Integer, Integer>> mobcaps = Mobcaps.getMobcaps(dimension);
        List<TextComponent> lst = new ArrayList<>();
        lst.add(Messenger.s(String.format("Mobcaps for %s:", Registry.DIMENSION.getId(dimension))));
        for (Map.Entry<EntityCategory, Pair<Integer, Integer>> e : mobcaps.entrySet()) {
            EntityCategory category = e.getKey();
            Pair<Integer, Integer> pair = e.getValue();
            int cur = pair.getLeft();
            int max = pair.getRight();
            lst.add(Messenger.c(String.format("w   %s: ", category),
                (cur+max==0)?"g -/-":String.format("%s %d/%d", (cur >= max)?"r":((cur >= 8*max/10)?"y":"l") ,cur, max)
            ));
        }
        Messenger.send(source, lst);
        return 1;
    }
}
