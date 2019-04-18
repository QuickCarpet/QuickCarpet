package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TextComponent;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import quickcarpet.QuickCarpetSettings;
import quickcarpet.mixin.IServerChunkManager;
import quickcarpet.utils.Messenger;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("spawn").
                requires((player) -> QuickCarpetSettings.getBool("commandSpawn")).
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
        ServerWorld world = source.getMinecraftServer().getWorld(dimension);
        Object2IntMap<EntityCategory> mobs = world.getMobCountsByCategory();
        List<TextComponent> lst = new ArrayList<>();
        lst.add(Messenger.s(String.format("Mobcaps for %s:", Registry.DIMENSION.getId(dimension))));
        int chunks = ((IServerChunkManager) world.method_14178()).getTicketManager().getLevelCount();
        for (EntityCategory category : EntityCategory.values()) {
            int cur = mobs.getOrDefault(category, 0);
            int max = chunks * category.getSpawnCap() / (17 * 17);
            lst.add(Messenger.c(String.format("w   %s: ", category),
                (cur+max==0)?"g -/-":String.format("%s %d/%d", (cur >= max)?"r":((cur >= 8*max/10)?"y":"l") ,cur, max)
            ));
        }
        Messenger.send(source, lst);
        return 1;
    }
}
