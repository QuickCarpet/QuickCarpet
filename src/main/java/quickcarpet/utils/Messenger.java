package quickcarpet.utils;

import com.mojang.logging.LogUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.State;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerData;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Utility class for formatting {@link Text}s
 */
public class Messenger {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Formatting[] GRAY_ITALIC = new Formatting[]{Formatting.GRAY, Formatting.ITALIC};

    public static MutableText style(MutableText text, Formatting style) {
        text.setStyle(text.getStyle().withFormatting(style));
        return text;
    }

    public static MutableText style(MutableText text, Formatting... style) {
        text.setStyle(text.getStyle().withFormatting(style));
        return text;
    }

    public static MutableText suggestCommand(MutableText text, String command) {
        text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));
        return text;
    }

    public static MutableText suggestCommand(MutableText text, String command, Text hoverText) {
        Style style = text.getStyle()
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        text.setStyle(style);
        return text;
    }

    public static MutableText runCommand(MutableText text, String command) {
        text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
        return text;
    }

    public static MutableText runCommand(MutableText text, String command, Text hoverText) {
        Style style = text.getStyle()
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        text.setStyle(style);
        return text;
    }

    public static MutableText hoverText(MutableText text, Text hoverText) {
        Style style = text.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        text.setStyle(style);
        return text;
    }

    public static Formatting getHeatmapColor(double actual, double reference) {
        if (actual > reference) return Formatting.RED;
        if (actual > 0.8 * reference) return Formatting.GOLD;
        if (actual > 0.5 * reference) return Formatting.YELLOW;
        return Formatting.DARK_GREEN;
    }

    public static MutableText format(BlockState state) {
        return format(Registry.BLOCK, BlockState::getBlock, state);
    }

    public static MutableText format(FluidState state) {
        return format(Registry.FLUID, FluidState::getFluid, state);
    }

    private static <O, S extends State<O, S>> MutableText format(Registry<O> ownerRegistry, Function<S, O> ownerGetter, S state) {
        MutableText text = s(ownerRegistry.getId(ownerGetter.apply(state)).toString());
        Collection<Property<?>> properties = state.getProperties();
        if (properties.isEmpty()) return text;
        text.append("[");
        boolean first = true;
        for (Property<?> prop : properties) {
            if (!first) text.append(", ");
            first = false;
            text.append(format(state, prop));
        }
        return text.append("]");
    }

    public static <T extends Comparable<T>> MutableText format(State<?, ?> state, Property<T> prop) {
        return format(prop, state.get(prop));
    }

    public static <T extends Comparable<T>> MutableText format(Property<T> prop, T value) {
        MutableText name = s(prop.getName() + "=");
        MutableText valueText = s(prop.name(value));
        if (prop instanceof DirectionProperty) style(valueText, Formatting.GOLD);
        else if (prop instanceof BooleanProperty) style(valueText, (Boolean) value ? Formatting.GREEN : Formatting.RED);
        else if (prop instanceof IntProperty) style(valueText, Formatting.GREEN);
        return name.append(valueText);
    }

    public static MutableText format(Direction side) {
        return t("side." + side.asString());
    }

    public static <T> MutableText format(Registry<T> registry, T value) {
        return format(registry.getId(value));
    }

    public static MutableText format(Identifier id) {
        if (id == null || !"minecraft".equals(id.getNamespace())) return s(String.valueOf(id));
        return c(s("minecraft:", Formatting.GRAY), s(id.getPath(), Formatting.WHITE));
    }

    public static MutableText format(EntityType<?> entity) {
        return format(Registry.ENTITY_TYPE.getId(entity));
    }

    public static MutableText tp(Vec3d pos, Formatting... style) {
        return tp(pos.x, pos.y, pos.z, style);
    }

    public static MutableText tp(Waypoint waypoint, Formatting... style) {
        return runCommand(
                s(String.format(Locale.ROOT, "[ %.1f, %.1f, %.1f ]", waypoint.position().x, waypoint.position().y, waypoint.position().z), style),
                String.format(Locale.ROOT, "/tp waypoint %s", waypoint.getFullName()));
    }

    public static MutableText tp(BlockPos pos, Formatting... style) {
        return tp(pos.getX(), pos.getY(), pos.getZ(), style);
    }

    public static MutableText tp(double x, double y, double z, Formatting... style) {
        return runCommand(
            s(String.format(Locale.ROOT, "[ %.1f, %.1f, %.1f ]", x, y, z), style),
            String.format(Locale.ROOT, "/tp %.3f %.3f %.3f", x, y, z)
        );
    }

    public static MutableText tp(int x, int y, int z, Formatting... style) {
        return runCommand(
                s(String.format(Locale.ROOT, "[ %d, %d, %d ]", x, y, z), style),
                String.format(Locale.ROOT, "/tp %d %d %d", x, y, z)
        );
    }

    public static MutableText dbl(double value) {
        return hoverText(format("%.1f", value), s(String.valueOf(value)));
    }

    public static MutableText dbl(double value, Formatting... style) {
        return hoverText(style(format("%.1f", value), style), s(String.valueOf(value)));
    }

    public static MutableText dblf(double... doubles) {
        return s(DoubleStream.of(doubles).mapToObj(Double::toString).collect(Collectors.joining(", ", "[ ", " ]")));
    }

    public static MutableText dblt(double... doubles) {
        MutableText text = s("[ ");
        boolean first = true;
        for (double d : doubles) {
            if (first) first = false;
            else text.append(", ");
            text.append(suggestCommand(format("%.1f", d), Double.toString(d), s(Double.toString(d))));
        }
        text.append(" ]");
        return text;
    }

    public static void m(ServerCommandSource source, Text message) {
        send(source, message, false);
    }

    public static void m(ServerCommandSource source, Text... fields) {
        m(source, c(fields));
    }

    /**
     * Formats multiple texts to a single text
     * @param components Texts
     * @return Formatted multi-component text
     */
    public static MutableText c(@Nonnull Text... components) {
        MutableText message = s("");
        for (Text t : components) message.append(t);
        return message;
    }

    public static MutableText c(@Nonnull Iterable<MutableText> components) {
        Iterator<MutableText> it = components.iterator();
        MutableText message = it.hasNext() ? it.next() : s("");
        while (it.hasNext()) {
            message.append(it.next());
        }
        return message;
    }

    public static <T> MutableText join(Iterable<T> items, Function<T, Text> mapper, Text delimiter) {
        return join(null, items, mapper, delimiter, null);
    }

    public static <T> MutableText join(@Nullable Text prefix, Iterable<T> items, Function<T, Text> mapper, Text delimiter, @Nullable Text suffix) {
        MutableText message = prefix == null ? s("") : prefix.copy();
        boolean first = true;
        for (T item : items) {
            if (!first) message.append(delimiter);
            message.append(mapper.apply(item));
            first = false;
        }
        if (suffix != null) message.append(suffix);
        return message;
    }

    //simple text

    public static MutableText s(@Nonnull String text) {
        return MutableText.of(new LiteralTextContent(text));
    }

    public static MutableText s(@Nonnull String text, Formatting style) {
        return style(s(text), style);
    }

    public static MutableText s(@Nonnull String text, Formatting... style) {
        return style(s(text), style);
    }

    public static MutableText t(@Nonnull String key, Object... args) {
        for (int i = 0; i < args.length; i++) if (args[i] instanceof Formattable) args[i] = ((Formattable) args[i]).format();
        return MutableText.of(new TranslatableTextContent(key, args));
    }

    public static MutableText ts(@Nonnull String key, Formatting style, Object... args) {
        return style(t(key, args), style);
    }

    public static MutableText ts(@Nonnull String key, Formatting[] style, Object... args) {
        return style(t(key, args), style);
    }

    public static MutableText format(@Nonnull String format, Object... args) {
        return s(String.format(Locale.ROOT, format, args));
    }

    public static MutableText formats(@Nonnull String format, Formatting style, Object... args) {
        return s(String.format(Locale.ROOT, format, args), style);
    }

    public static MutableText formats(@Nonnull String format, Formatting[] style, Object... args) {
        return s(String.format(Locale.ROOT, format, args), style);
    }

    public static void send(ServerCommandSource source, Collection<? extends Text> lines) {
        send(source, lines, false);
    }

    public static void send(ServerCommandSource source, Collection<? extends Text> lines, boolean sendToOps) {
        lines.forEach(line -> send(source, line, sendToOps));
    }

    public static void send(ServerCommandSource source, Text line, boolean sendToOps) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayerEntity) line = Translations.translate(line, (ServerPlayerEntity) entity);
        source.sendFeedback(line, sendToOps);
    }


    public static void broadcast(MinecraftServer server, String message) {
        broadcast(server, s(message));
    }

    public static void broadcast(MinecraftServer server, Text message) {
        if (server == null) {
            LOGGER.error("Message not delivered: " + message.getString());
            return;
        }
        server.sendMessage(message);
        for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
            send(player.getCommandSource(), message, false);
        }
    }

    public static MutableText join(Text joiner, Text... elements) {
        if (elements.length == 0) return s("");
        MutableText text = elements[0].copy();
        for (int i = 1; i < elements.length; i++) {
            text.append(joiner.copy());
            text.append(elements[i].copy());
        }
        return text;
    }

    public static MutableText join(Text joiner, Collection<? extends Text> elements) {
        return join(joiner, elements.toArray(new Text[0]));
    }

    public static Collector<Text, MutableText, MutableText> joining(Text joiner) {
        MutableText empty = s("");
        return Collector.of(() -> empty,
            (a, b) -> {
                if (!a.getSiblings().isEmpty()) a.append(joiner.copy());
                a.append(b);
            },
            (a, b) -> a.getSiblings().isEmpty() ? b : a.append(joiner.copy()).append(b.copy()),
            Function.identity()
        );
    }

    public interface Formattable {
        MutableText format();
    }

    public interface Formatter<T> {
        MutableText format(T value);

        Formatter<Number> NUMBER = value -> s(value.toString());
        Formatter<Number> FLOAT = value -> dbl(value.doubleValue());
        Formatter<Boolean> BOOLEAN = value -> s(Boolean.toString(value), value ? Formatting.GREEN : Formatting.RED);
        Formatter<String> STRING = Messenger::s;
        Formatter<Text> TEXT = Text::copy;
        Formatter<ItemStack> ITEM_STACK = s -> c(s(s.getCount() + " "), s.toHoverableText().copy());
        Formatter<EulerAngle> ROTATION = r -> s(r.getYaw() + "° yaw, " + r.getPitch() + "° pitch, " + r.getRoll() + "° roll");
        Formatter<BlockPos> BLOCK_POS = Messenger::tp;
        Formatter<? extends Enum<?>> ENUM = e -> s(e.toString().toLowerCase(Locale.ROOT));
        Formatter<?> OBJECT = o -> s(String.valueOf(o));
        Formatter<BlockState> BLOCK_STATE = Messenger::format;
        Formatter<FluidState> FLUID_STATE = Messenger::format;
        Formatter<NbtCompound> COMPOUND_TAG = t -> NbtHelper.toPrettyPrintedText(t).copy();
        Formatter<ParticleEffect> PARTICLE = p -> s(String.valueOf(Registry.PARTICLE_TYPE.getId(p.getType())));
        Formatter<VillagerData> VILLAGER_DATA = d -> c(
            s("type="), Messenger.format(Registry.VILLAGER_TYPE, d.getType()),
            s(", profession="), Messenger.format(Registry.VILLAGER_PROFESSION, d.getProfession()),
            s(", level=" + d.getLevel())
        );
        Formatter<OptionalInt> OPTIONAL_INT = o -> s(o.isPresent() ? Integer.toString(o.getAsInt()) : "empty");

        static <T> Formatter<Optional<T>> optional(Formatter<T> formatter) {
            return value -> {
                if (value.isPresent()) return formatter.format(value.get());
                return s("empty", Formatting.GRAY, Formatting.ITALIC);
            };
        }
    }
}
