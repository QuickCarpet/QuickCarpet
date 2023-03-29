package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.Pair;
import quickcarpet.mixin.accessor.DataTrackerAccessor;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.DataTrackerCommand.Keys;
import quickcarpet.utils.DataTrackerUtils;

import java.util.Collection;
import java.util.Locale;

import static net.minecraft.command.argument.EntityArgumentType.entity;
import static net.minecraft.command.argument.EntityArgumentType.getEntity;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Constants.DataTrackerCommand.Texts.NO_ENTRIES;
import static quickcarpet.utils.Messenger.*;

public class DataTrackerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var tracker = literal("datatracker")
            .requires(s -> s.hasPermissionLevel(Settings.commandDataTracker))
            .then(argument("entity", entity())
                .then(literal("list").executes(DataTrackerCommand::list)));

        dispatcher.register(tracker);
    }

    private static int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Entity target = getEntity(ctx, "entity");
        DataTracker tracker = target.getDataTracker();
        Collection<DataTracker.Entry<?>> entries = ((DataTrackerAccessor) tracker).getEntries().values();
        if (entries == null || entries.isEmpty()) {
            m(ctx.getSource(), NO_ENTRIES);
            return 0;
        }
        Int2ObjectMap<Pair<String, DataTrackerUtils.KnownType>> knownProps = DataTrackerUtils.collectKnownProperties(target.getClass());
        for (DataTracker.Entry<?> e : entries) {
            m(ctx.getSource(), format(e, knownProps));
        }
        return entries.size();
    }

    private static <T> Text format(DataTracker.Entry<T> entry, Int2ObjectMap<Pair<String, DataTrackerUtils.KnownType>> knownProps) {
        TrackedData<?> data = entry.getData();
        DataTrackerUtils.KnownType type = DataTrackerUtils.KnownType.get(data.getType());
        Pair<String, DataTrackerUtils.KnownType> known = knownProps.get(data.getId());
        String name = known.getRight() == type ? known.getLeft() : "unknown (" + data.getId() + ")";
        @SuppressWarnings("unchecked")
        Formatter<T> formatter = (Formatter<T>) type.formatter;

        return t(Keys.ENTRY, name, type.name().toLowerCase(Locale.ROOT), formatter.format(entry.get()));
    }
}
