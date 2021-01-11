package quickcarpet.utils;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.MatchesPattern;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Utility class for formatting {@link Text}s
 */
public class Messenger {
    private static final Logger LOG = LogManager.getLogger();

    public static final Formatter<Number> NUMBER = value -> s(value.toString());
    public static final Formatter<Number> FLOAT = value -> dbl(value.doubleValue());
    public static final Formatter<Boolean> BOOLEAN = value -> s(Boolean.toString(value), value ? Formatting.GREEN : Formatting.RED);
    public static final Formatting[] GRAY_ITALIC = new Formatting[]{Formatting.GRAY, Formatting.ITALIC};

    public static <T extends MutableText> T style(T text, Formatting style) {
        text.setStyle(text.getStyle().withFormatting(style));
        return text;
    }

    public static <T extends MutableText> T style(T text, Formatting... style) {
        text.setStyle(text.getStyle().withFormatting(style));
        return text;
    }

    public static <T extends MutableText> T suggestCommand(T text, String command) {
        text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));
        return text;
    }

    public static <T extends MutableText> T suggestCommand(T text, String command, Text hoverText) {
        Style style = text.getStyle()
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        text.setStyle(style);
        return text;
    }

    public static <T extends MutableText> T runCommand(T text, String command) {
        text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
        return text;
    }

    public static <T extends MutableText> T runCommand(T text, String command, Text hoverText) {
        Style style = text.getStyle()
            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        text.setStyle(style);
        return text;
    }

    public static <T extends Text> T hoverText(T text, Text hoverText) {
        text.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
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
        if (prop instanceof DirectionProperty) style(valueText, GOLD);
        else if (prop instanceof BooleanProperty) style(valueText, (Boolean) value ? LIME : RED);
        else if (prop instanceof IntProperty) style(valueText, LIME);
        return name.append(valueText);
    }

    public static MutableText format(Direction side) {
        return new TranslatableText("side." + side.asString());
    }

    public static Formatting creatureTypeColor(SpawnGroup type) {
        switch (type) {
            case MONSTER: return Formatting.DARK_RED;
            case CREATURE: return Formatting.DARK_GREEN;
            case AMBIENT: return Formatting.DARK_GRAY;
            case WATER_CREATURE: return Formatting.BLUE;
            case WATER_AMBIENT: return Formatting.DARK_AQUA;
        }
        return Formatting.WHITE;
    }

    public static MutableText tp(Vec3d pos, Formatting... style) {
        return tp(pos.x, pos.y, pos.z, style);
    }

    public static MutableText tp(Waypoint waypoint, Formatting... style) {
        return runCommand(
                s(String.format(Locale.ROOT, "[ %.1f, %.1f, %.1f ]", waypoint.position.x, waypoint.position.y, waypoint.position.z), style),
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

    public static void m(ServerCommandSource source, MutableText message) {
        send(source, message, false);
    }

    public static void m(ServerCommandSource source, Object... fields) {
        m(source, c(fields));
    }

    /**
     * Formats multiple texts to a single text
     * @param components Texts
     * @return Formatted multi-component text
     */
    public static MutableText c(@Nonnull MutableText... components) {
        MutableText message = components.length > 0 ? components[0] : new LiteralText("");
        for (int i = 1; i < components.length; i++) {
            message.append(components[i]);
        }
        return message;
    }

    public static MutableText c(@Nonnull Iterable<MutableText> components) {
        Iterator<MutableText> it = components.iterator();
        MutableText message = it.hasNext() ? it.next() : new LiteralText("");
        while (it.hasNext()) {
            message.append(it.next());
        }
        return message;
    }

    //simple text

    public static MutableText s(@Nonnull String text) {
        return new LiteralText(text);
    }

    public static MutableText s(@Nonnull String text, Formatting style) {
        return style(s(text), style);
    }

    public static MutableText s(@Nonnull String text, Formatting... style) {
        return style(s(text), style);
    }

    public static TranslatableText t(@Nonnull String key, Object... args) {
        for (int i = 0; i < args.length; i++) if (args[i] instanceof Formattable) args[i] = ((Formattable) args[i]).format();
        return new TranslatableText(key, args);
    }

    public static TranslatableText ts(@Nonnull String key, Formatting style, Object... args) {
        return style(t(key, args), style);
    }

    public static TranslatableText ts(@Nonnull String key, Formatting[] style, Object... args) {
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

    public static void send(ServerCommandSource source, Collection<MutableText> lines) {
        send(source, lines, false);
    }

    public static void send(ServerCommandSource source, Collection<MutableText> lines, boolean sendToOps) {
        lines.forEach(line -> send(source, line, sendToOps));
    }

    public static void send(ServerCommandSource source, MutableText line, boolean sendToOps) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayerEntity) line = Translations.translate(line, (ServerPlayerEntity) entity);
        source.sendFeedback(line, sendToOps);
    }


    public static void broadcast(MinecraftServer server, String message) {
        broadcast(server, new LiteralText(message));
    }

    public static void broadcast(MinecraftServer server, MutableText message) {
        if (server == null) {
            LOG.error("Message not delivered: " + message.getString());
            return;
        }
        server.sendSystemMessage(message, Util.NIL_UUID);
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
    }

    /*
     * Deprecated methods, taking String, char or PlayerEntity arguments
     */

    @Deprecated public static final char ITALIC = 'i';
    @Deprecated public static final char STRIKETHROUGH = 's';
    @Deprecated public static final char UNDERLINE = 'u';
    @Deprecated public static final char BOLD = 'b';
    @Deprecated public static final char OBFUSCATED = 'o';
    @Deprecated public static final char WHITE = 'w';
    @Deprecated public static final char YELLOW = 'y';
    @Deprecated public static final char LIGHT_PURPLE = 'm';
    @Deprecated public static final char RED = 'r';
    @Deprecated public static final char CYAN = 'c';
    @Deprecated public static final char LIME = 'l';
    @Deprecated public static final char BLUE = 't';
    @Deprecated public static final char DARK_GRAY = 'f';
    @Deprecated public static final char GRAY = 'g';
    @Deprecated public static final char GOLD = 'd';
    @Deprecated public static final char DARK_PURPLE = 'p';
    @Deprecated public static final char DARK_RED = 'n';
    @Deprecated public static final char DARK_AQUA = 'q';
    @Deprecated public static final char DARK_GREEN = 'e';
    @Deprecated public static final char DARK_BLUE = 'v';
    @Deprecated public static final char BLACK = 'k';

    /*
     messsage: "desc me ssa ge"
     desc contains:
     i = italic
     s = strikethrough
     u = underline
     b = bold
     o = obfuscated

     w = white
     y = yellow
     m = magenta (light purple)
     r = red
     c = cyan (aqua)
     l = lime (green)
     t = light blue (blue)
     f = dark gray
     g = gray
     d = gold
     p = dark purple (purple)
     n = dark red (brown)
     q = dark aqua
     e = dark green
     v = dark blue (navy)
     k = black

     / = action added to the previous component
     */

    @Deprecated
    private static MutableText applyStyleToTextComponent(MutableText comp, @MatchesPattern("^[isubowymrcltfgdpnqevk]+$") String styleCode) {
        Style style = comp.getStyle();
        for (int i = 0; i < styleCode.length(); i++) {
            style = applyStyle(style, styleCode.charAt(i));
        }
        comp.setStyle(style);
        return comp;
    }

    @Deprecated
    private static Style applyStyle(Style style, char styleCode) {
        switch (styleCode) {
            case ITALIC: return style.withFormatting(Formatting.ITALIC);
            case STRIKETHROUGH: return style.withFormatting(Formatting.STRIKETHROUGH);
            case UNDERLINE: return style.withFormatting(Formatting.UNDERLINE);
            case BOLD: return style.withFormatting(Formatting.BOLD);
            case OBFUSCATED: return style.withFormatting(Formatting.OBFUSCATED);
            case WHITE: return style.withColor(Formatting.WHITE);
            case YELLOW: return style.withColor(Formatting.YELLOW);
            case LIGHT_PURPLE: return style.withColor(Formatting.LIGHT_PURPLE);
            case RED: return style.withColor(Formatting.RED);
            case CYAN: return style.withColor(Formatting.AQUA);
            case LIME: return style.withColor(Formatting.GREEN);
            case BLUE: return style.withColor(Formatting.BLUE);
            case DARK_GRAY: return style.withColor(Formatting.DARK_GRAY);
            case GRAY: return style.withColor(Formatting.GRAY);
            case GOLD: return style.withColor(Formatting.GOLD);
            case DARK_PURPLE: return style.withColor(Formatting.DARK_PURPLE);
            case DARK_RED: return style.withColor(Formatting.DARK_RED);
            case DARK_AQUA: return style.withColor(Formatting.DARK_AQUA);
            case DARK_GREEN: return style.withColor(Formatting.DARK_GREEN);
            case DARK_BLUE: return style.withColor(Formatting.DARK_BLUE);
            case BLACK: return style.withColor(Formatting.BLACK);
            default: throw new IllegalArgumentException("Unknown formatting code " + styleCode);
        }
    }

    @Deprecated
    public static <T extends MutableText> T style(T text, @MatchesPattern("^[isubowymrcltfgdpnqevk]+$") String style) {
        applyStyleToTextComponent(text, style);
        return text;
    }

    @Deprecated
    public static <T extends MutableText> T style(T text, char style) {
        text.setStyle(applyStyle(text.getStyle(), style));
        return text;
    }

    @Deprecated
    public static MutableText tp(String desc, Vec3d pos) {
        return tp(desc, pos.x, pos.y, pos.z);
    }

    @Deprecated
    public static MutableText tp(String desc, Waypoint waypoint) {
        return tp(desc, waypoint.position);  //TODO: tp to waypoint
    }

    @Deprecated
    public static MutableText tp(String desc, BlockPos pos) {
        return tp(desc, pos.getX(), pos.getY(), pos.getZ());
    }

    @Deprecated
    public static MutableText tp(String desc, double x, double y, double z) {
        return tp(desc, (float) x, (float) y, (float) z);
    }

    @Deprecated
    public static MutableText tp(String desc, float x, float y, float z) {
        return getCoordsTextComponent(desc, x, y, z, false);
    }

    @Deprecated
    public static MutableText tp(String desc, int x, int y, int z) {
        return getCoordsTextComponent(desc, x, y, z, true);
    }

    @Deprecated
    private static MutableText getCoordsTextComponent(String style, float x, float y, float z, boolean isInt) {
        if (isInt) return runCommand(s(String.format("[ %d, %d, %d ]", (int) x, (int) y, (int) z), style), String.format("/tp %d %d %d", (int) x, (int) y, (int) z));
        return runCommand(s(String.format("[ %.1f, %.1f, %.1f ]", x, y, z), style), String.format("/tp %.3f %.3f %.3f", x, y, z));
    }

    @Deprecated
    public static MutableText dbl(String style, double value) {
        return hoverText(style(format("%.1f", value), style), s(String.valueOf(value)));
    }

    @Deprecated
    public static MutableText dbls(String style, double... doubles) {
        return s(DoubleStream.of(doubles).mapToObj(d -> String.format("%.1f", d)).collect(Collectors.joining(", ", "[ ", " ]")));
    }

    @Deprecated
    public static void m(PlayerEntity player, MutableText message) {
        m(player.getCommandSource(), message);
    }

    @Deprecated
    public static void m(PlayerEntity player, Object... fields) {
        m(player, c(fields));
    }

    /**
     * Formats a text or string to a text
     * @param field Text or formatting string
     * @return Formatted text
     * @deprecated Use {@link #s(String)} or {@link #s(String, String)}
     */
    @Deprecated
    public static MutableText c(@Nonnull Object field) {
        if (field instanceof MutableText) return (MutableText) field;
        if (field instanceof String) return formatComponent((String) field, null);
        throw new IllegalArgumentException("Expected text or string");
    }

    /**
     * Formats multiple texts or strings to a single text
     * @param fields Texts or formatting strings
     * @return Formatted multi-component text
     * @deprecated Use {@link #c(MutableText...)} instead
     */
    @Deprecated
    public static MutableText c(@Nonnull Object... fields) {
        MutableText message = new LiteralText("");
        MutableText previousText = null;
        for (Object field : fields) {
            if (field instanceof Text) {
                message.append((MutableText) field);
                previousText = (MutableText) field;
            } else if (field instanceof Formattable) {
                MutableText t = ((Formattable) field).format();
                message.append(t);
                previousText = t;
            } else if (field instanceof String) {
                MutableText comp = formatComponent((String) field, previousText);
                if (comp != previousText) message.append(comp);
                previousText = comp;
            } else throw new IllegalArgumentException("Expected text or string");
        }
        return message;
    }

    @Deprecated
    private static MutableText formatComponent(String message, MutableText previous) {
        if (message.equalsIgnoreCase("")) {
            return new LiteralText("");
        }
        String[] parts = message.split("\\s", 2);
        String desc = parts[0];
        String str = "";
        if (parts.length > 1) str = parts[1];
        if (desc.charAt(0) == '/') { // deprecated
            if (previous != null) {
                previous.setStyle(previous.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message)));
            }
            return previous;
        }
        if (desc.charAt(0) == '?') {
            if (previous != null) {
                previous.setStyle(previous.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message.substring(1))));
            }
            return previous;
        }
        if (desc.charAt(0) == '!') {
            if (previous != null) {
                previous.setStyle(previous.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, message.substring(1))));
            }
            return previous;
        }
        if (desc.charAt(0) == '^') {
            if (previous != null) {
                previous.setStyle(previous.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, c(message.substring(1)))));
            }
            return previous;
        }
        MutableText txt = new LiteralText(str);
        return applyStyleToTextComponent(txt, desc);
    }

    @Deprecated
    public static MutableText s(@Nonnull String text, @Nonnull @MatchesPattern("^[isubowymrcltfgdpnqevk]+$") String style) {
        return style(s(text), style);
    }

    @Deprecated
    public static MutableText s(@Nonnull String text, char style) {
        return style(s(text), style);
    }

    @Deprecated
    public static TranslatableText ts(@Nonnull String key, char style, Object... args) {
        return style(t(key, args), style);
    }

    @Deprecated
    public static TranslatableText ts(@Nonnull String key, @Nonnull String style, Object... args) {
        return style(t(key, args), style);
    }

    @Deprecated
    public static MutableText formats(@Nonnull String format, @Nonnull String style, Object... args) {
        return s(String.format(format, args), style);
    }

    @Deprecated
    public static MutableText formats(@Nonnull String format, char style, Object... args) {
        return s(String.format(format, args), style);
    }

    @Deprecated
    public static void send(PlayerEntity player, Collection<MutableText> lines) {
        lines.forEach(line -> send(player, line));
    }

    @Deprecated
    public static void send(PlayerEntity player, MutableText line) {
        if (player instanceof ServerPlayerEntity) line = Translations.translate(line, (ServerPlayerEntity) player);
        player.sendMessage(line, false);
    }
}
