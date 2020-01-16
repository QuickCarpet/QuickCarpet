package quickcarpet.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.MatchesPattern;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Utility class for formatting {@link Text}s
 */
public class Messenger {
    private static final Logger LOG = LogManager.getLogger();

    public static final char ITALIC = 'i';
    public static final char STRIKETHROUGH = 's';
    public static final char UNDERLINE = 'u';
    public static final char BOLD = 'b';
    public static final char OBFUSCATED = 'o';
    public static final char WHITE = 'w';
    public static final char YELLOW = 'y';
    public static final char LIGHT_PURPLE = 'm';
    public static final char RED = 'r';
    public static final char CYAN = 'c';
    public static final char LIME = 'l';
    public static final char BLUE = 't';
    public static final char DARK_GRAY = 'f';
    public static final char GRAY = 'g';
    public static final char GOLD = 'd';
    public static final char DARK_PURPLE = 'p';
    public static final char DARK_RED = 'n';
    public static final char DARK_AQUA = 'q';
    public static final char DARK_GREEN = 'e';
    public static final char DARK_BLUE = 'v';
    public static final char BLACK = 'k';

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

    private static Text applyStyleToTextComponent(Text comp, @MatchesPattern("^[isubowymrcltfgdpnqevk]+$") String styleCode) {
        Style style = comp.getStyle();
        for (int i = 0; i < styleCode.length(); i++) {
            applyStyle(style, styleCode.charAt(i));
        }
        return comp;
    }

    private static void applyStyle(Style style, char styleCode) {
        switch (styleCode) {
            case ITALIC: style.setItalic(true); break;
            case STRIKETHROUGH: style.setStrikethrough(true); break;
            case UNDERLINE: style.setUnderline(true); break;
            case BOLD: style.setBold(true); break;
            case OBFUSCATED: style.setObfuscated(true); break;
            case WHITE: style.setColor(Formatting.WHITE); break;
            case YELLOW: style.setColor(Formatting.YELLOW); break;
            case LIGHT_PURPLE: style.setColor(Formatting.LIGHT_PURPLE); break;
            case RED: style.setColor(Formatting.RED); break;
            case CYAN: style.setColor(Formatting.AQUA); break;
            case LIME: style.setColor(Formatting.GREEN); break;
            case BLUE: style.setColor(Formatting.BLUE); break;
            case DARK_GRAY: style.setColor(Formatting.DARK_GRAY); break;
            case GRAY: style.setColor(Formatting.GRAY); break;
            case GOLD: style.setColor(Formatting.GOLD); break;
            case DARK_PURPLE: style.setColor(Formatting.DARK_PURPLE); break;
            case DARK_RED: style.setColor(Formatting.DARK_RED); break;
            case DARK_AQUA: style.setColor(Formatting.DARK_AQUA); break;
            case DARK_GREEN: style.setColor(Formatting.DARK_GREEN); break;
            case DARK_BLUE: style.setColor(Formatting.DARK_BLUE); break;
            case BLACK: style.setColor(Formatting.BLACK); break;
            default: throw new IllegalArgumentException("Unknown formatting code " + styleCode);
        }
    }

    public static <T extends Text> T style(T text, @MatchesPattern("^[isubowymrcltfgdpnqevk]+$") String style) {
        applyStyleToTextComponent(text, style);
        return text;
    }

    public static <T extends Text> T style(T text, char style) {
        applyStyle(text.getStyle(), style);
        return text;
    }

    public static <T extends Text> T suggestCommand(T text, String command) {
        text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return text;
    }

    public static <T extends Text> T suggestCommand(T text, String command, Text hoverText) {
        Style style = text.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        return text;
    }

    public static <T extends Text> T runCommand(T text, String command) {
        text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return text;
    }

    public static <T extends Text> T runCommand(T text, String command, Text hoverText) {
        Style style = text.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        return text;
    }

    public static <T extends Text> T hoverText(T text, Text hoverText) {
        text.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
        return text;
    }

    public static char getHeatmapColor(double actual, double reference) {
        if (actual > reference) return RED;
        if (actual > 0.8 * reference) return GOLD;
        if (actual > 0.5 * reference) return YELLOW;
        return DARK_GREEN;
    }

    public static char creatureTypeColor(EntityCategory type) {
        switch (type) {
            case MONSTER: return DARK_RED;
            case CREATURE: return DARK_GREEN;
            case AMBIENT: return DARK_GRAY;
            case WATER_CREATURE: return BLUE;
        }
        return WHITE;
    }

    private static Text formatComponent(String message, Text previous) {
        if (message.equalsIgnoreCase("")) {
            return new LiteralText("");
        }
        String[] parts = message.split("\\s", 2);
        String desc = parts[0];
        String str = "";
        if (parts.length > 1) str = parts[1];
        if (desc.charAt(0) == '/') { // deprecated
            if (previous != null) previous.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message));
            return previous;
        }
        if (desc.charAt(0) == '?') {
            if (previous != null) previous.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, message.substring(1)));
            return previous;
        }
        if (desc.charAt(0) == '!') {
            if (previous != null) previous.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, message.substring(1)));
            return previous;
        }
        if (desc.charAt(0) == '^') {
            if (previous != null) previous.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, c(message.substring(1))));
            return previous;
        }
        Text txt = new LiteralText(str);
        return applyStyleToTextComponent(txt, desc);
    }

    public static Text tp(String desc, Vec3d pos) {
        return tp(desc, pos.x, pos.y, pos.z);
    }

    public static Text tp(String desc, Waypoint waypoint) {
        return tp(desc, waypoint.position);  //TODO: tp to waypoint
    }

    public static Text tp(String desc, BlockPos pos) {
        return tp(desc, pos.getX(), pos.getY(), pos.getZ());
    }

    public static Text tp(String desc, double x, double y, double z) {
        return tp(desc, (float) x, (float) y, (float) z);
    }

    public static Text tp(String desc, float x, float y, float z) {
        return getCoordsTextComponent(desc, x, y, z, false);
    }

    public static Text tp(String desc, int x, int y, int z) {
        return getCoordsTextComponent(desc, (float) x, (float) y, (float) z, true);
    }

    /// to be continued
    public static Text dbl(String style, double value) {
        return hoverText(format("%.1f", value), s(String.valueOf(value)));
    }

    public static Text dbls(String style, double... doubles) {
        return s(DoubleStream.of(doubles).mapToObj(d -> String.format("%.1f", d)).collect(Collectors.joining(", ", "[ ", " ]")));
    }

    public static Text dblf(double... doubles) {
        return s(DoubleStream.of(doubles).mapToObj(Double::toString).collect(Collectors.joining(", ", "[ ", " ]")));
    }

    public static Text dblt(double... doubles) {
        Text text = s("[ ");
        boolean first = true;
        for (double d : doubles) {
            if (first) first = false;
            else text.append(", ");
            text.append(suggestCommand(format("%.1f", d), Double.toString(d), s(Double.toString(d))));
        }
        text.append(" ]");
        return text;
    }

    private static Text getCoordsTextComponent(String style, float x, float y, float z, boolean isInt) {
        if (isInt) return runCommand(s(String.format("[ %d, %d, %d ]", (int) x, (int) y, (int) z), style), String.format("/tp %d %d %d", (int) x, (int) y, (int) z));
        return runCommand(s(String.format("[ %.1f, %.1f, %.1f ]", x, y, z), style), String.format("/tp %.3f %.3f %.3f", x, y, z));
    }

    public static void m(ServerCommandSource source, Text message) {
        send(source, message, false);
    }

    public static void m(ServerCommandSource source, Object... fields) {
        m(source, c(fields));
    }

    public static void m(PlayerEntity player, Text message) {
        send(player, message);
    }

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
    public static Text c(@Nonnull Object field) {
        if (field instanceof Text) return (Text) field;
        if (field instanceof String) return formatComponent((String) field, null);
        throw new IllegalArgumentException("Expected text or string");
    }

    /**
     * Formats multiple texts or strings to a single text
     * @param fields Texts or formatting strings
     * @return Formatted multi-component text
     */
    public static Text c(@Nonnull Object... fields) {
        Text message = new LiteralText("");
        Text previousText = null;
        for (Object field : fields) {
            if (field instanceof Text) {
                message.append((Text) field);
                previousText = (Text) field;
            } else if (field instanceof Formattable) {
                Text t = ((Formattable) field).format();
                message.append(t);
                previousText = t;
            } else if (field instanceof String) {
                Text comp = formatComponent((String) field, previousText);
                if (comp != previousText) message.append(comp);
                previousText = comp;
            } else throw new IllegalArgumentException("Expected text or string");
        }
        return message;
    }

    //simple text

    public static Text s(@Nonnull String text) {
        return new LiteralText(text);
    }

    public static Text s(@Nonnull String text, @Nonnull @MatchesPattern("^[isubowymrcltfgdpnqevk]+$") String style) {
        return style(s(text), style);
    }

    public static Text s(@Nonnull String text, char style) {
        return style(s(text), style);
    }

    public static TranslatableText t(@Nonnull String key, Object... args) {
        for (int i = 0; i < args.length; i++) if (args[i] instanceof Formattable) args[i] = ((Formattable) args[i]).format();
        return new TranslatableText(key, args);
    }

    public static TranslatableText ts(@Nonnull String key, char style, Object... args) {
        return style(t(key, args), style);
    }

    public static TranslatableText ts(@Nonnull String key, @Nonnull String style, Object... args) {
        return style(t(key, args), style);
    }

    public static Text format(@Nonnull String format, Object... args) {
        return s(String.format(format, args));
    }

    public static Text formats(@Nonnull String format, @Nonnull String style, Object... args) {
        return s(String.format(format, args), style);
    }

    public static Text formats(@Nonnull String format, char style, Object... args) {
        return s(String.format(format, args), style);
    }

    public static void send(PlayerEntity player, Collection<Text> lines) {
        lines.forEach(line -> send(player, line));
    }

    public static void send(PlayerEntity player, Text line) {
        if (player instanceof ServerPlayerEntity) line = Translations.translate(line, (ServerPlayerEntity) player);
        player.sendMessage(line);
    }

    public static void send(ServerCommandSource source, Collection<Text> lines) {
        send(source, lines, false);
    }

    public static void send(ServerCommandSource source, Collection<Text> lines, boolean sendToOps) {
        lines.forEach(line -> send(source, line, sendToOps));
    }

    public static void send(ServerCommandSource source, Text line, boolean sendToOps) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayerEntity) line = Translations.translate(line, (ServerPlayerEntity) entity);
        source.sendFeedback(line, sendToOps);
    }


    public static void broadcast(MinecraftServer server, String message) {
        broadcast(server, new LiteralText(message));
    }

    public static void broadcast(MinecraftServer server, Text message) {
        if (server == null) {
            LOG.error("Message not delivered: " + message.getString());
            return;
        }
        server.sendMessage(message);
        for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
            send(player, message);
        }
    }

    public interface Formattable {
        Text format();
    }
}
