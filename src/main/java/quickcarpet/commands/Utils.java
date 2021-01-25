package quickcarpet.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import quickcarpet.helper.StateInfoProvider;
import quickcarpet.utils.Translations;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.command.argument.BlockPosArgumentType.getLoadedBlockPos;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.literal;
import static quickcarpet.utils.Messenger.*;

public class Utils {
    private static final List<Formatting> ARG_COLORS = ImmutableList.of(Formatting.AQUA, Formatting.YELLOW, Formatting.GREEN, Formatting.LIGHT_PURPLE, Formatting.GOLD);

    interface ArgumentGetter<T> {
        T get(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException;
    }

    static <T> T getOrNull(CommandContext<ServerCommandSource> context, String argument, Class<T> type) {
        try {
            return context.getArgument(argument, type);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("No such argument")) return null;
            throw e;
        }
    }
    static <T> T getOrNull(CommandContext<ServerCommandSource> context, String argument, ArgumentGetter<T> getter) throws CommandSyntaxException {
        try {
            return getter.get(context, argument);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("No such argument")) return null;
            throw e;
        }
    }

    static <T> T getOrDefault(CommandContext<ServerCommandSource> context, String argument, T defaultValue) {
        T value = getOrNull(context, argument, (Class<T>) defaultValue.getClass());
        return value == null ? defaultValue : value;
    }

    static <T> T getOrDefault(CommandContext<ServerCommandSource> context, String argument, ArgumentGetter<T> getter, T defaultValue) throws CommandSyntaxException {
        T value = getOrNull(context, argument, getter);
        return value == null ? defaultValue : value;
    }

    static RequiredArgumentBuilder<ServerCommandSource, Identifier> identifierArgument(String name, Registry<?> registry) {
        return CommandManager.argument(name, identifier()).suggests((ctx, builder) -> CommandSource.suggestIdentifiers(registry.getIds(), builder));
    }

    static <T> T getIdentifierArgumentValue(CommandContext<ServerCommandSource> ctx, String name, Registry<T> registry, Function<Identifier, CommandSyntaxException> exceptionCreator) throws CommandSyntaxException {
        Identifier id = getIdentifier(ctx, name);
        return registry.getOrEmpty(id).orElseThrow(() -> exceptionCreator.apply(id));
    }

    static int getReturnValue(Comparable<?> value) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof Boolean) return ((Boolean) value) ? 1 : 0;
        if (value instanceof Enum<?>) return ((Enum<?>) value).ordinal();
        return 0;
    }

    interface StateInfoExecuteSingle {
        int executeDirection(CommandContext<ServerCommandSource> ctx, Direction direction) throws CommandSyntaxException;
    }

    static <T extends ArgumentBuilder<ServerCommandSource, T>> T makeStateInfoCommand(T root, Registry<? extends StateInfoProvider<?, ?>> registry, Command<ServerCommandSource> command, StateInfoExecuteSingle single) {
        return root.then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
            .executes(command)
            .then(identifierArgument("provider", registry)
                    .executes(ctx -> single.executeDirection(ctx, null))
                    .then(literal("up").executes(ctx -> single.executeDirection(ctx, Direction.UP)))
                    .then(literal("down").executes(ctx -> single.executeDirection(ctx, Direction.DOWN)))
                    .then(literal("north").executes(ctx -> single.executeDirection(ctx, Direction.NORTH)))
                    .then(literal("south").executes(ctx -> single.executeDirection(ctx, Direction.SOUTH)))
                    .then(literal("west").executes(ctx -> single.executeDirection(ctx, Direction.WEST)))
                    .then(literal("east").executes(ctx -> single.executeDirection(ctx, Direction.EAST)))
            ));
    }

    static <S extends State<?, S>, P extends StateInfoProvider<S, ?>> int executeStateInfo(ServerCommandSource source, BlockPos pos, S state, Registry<P> providers) {
        for (Map.Entry<RegistryKey<P>, P> e : providers.getEntries()) {
            MutableText value = e.getValue().getAndFormat(state, source.getWorld(), pos);
            m(source, c(Translations.translate(e.getKey()), s(": "), value));
        }
        return 0;
    }

    static <S extends State<?, S>, P extends StateInfoProvider<S, ?>> int executeStateInfo(CommandContext<ServerCommandSource> ctx, Direction direction, Registry<P> providers, BiFunction<BlockView, BlockPos, S> stateGetter, Function<Identifier, CommandSyntaxException> exceptionCreator) throws CommandSyntaxException {
        return executeStateInfo(ctx.getSource(),
                getLoadedBlockPos(ctx, "pos"),
                (StateInfoProvider<S, ?>) getIdentifierArgumentValue(ctx, "provider", providers, exceptionCreator),
                direction,
                stateGetter
        );
    }

    static <S extends State<?, S>, P extends StateInfoProvider<S, T>, T extends Comparable<T>> int executeStateInfo(ServerCommandSource source, BlockPos pos, P provider, Direction direction, BiFunction<BlockView, BlockPos, S> stateGetter) {
        ServerWorld world = source.getWorld();
        S state = stateGetter.apply(world, pos);
        T value = (direction != null && provider instanceof StateInfoProvider.Directional)
                ? ((StateInfoProvider.Directional<S, T>) provider).get(state, world, pos, direction)
                : provider.get(state, world, pos);
        m(source, provider.format(value));
        return getReturnValue(value);
    }

    static <E extends Enum<E>> List<String> getOptions(Class<E> type) {
        return Arrays.stream(type.getEnumConstants())
                .map(e -> e.name().toLowerCase(Locale.ROOT))
                .collect(ImmutableList.toImmutableList());
    }

    static <E extends Enum<E>> RequiredArgumentBuilder<ServerCommandSource, String> argument(String name, Class<E> type) {
        Iterable<String> options = getOptions(type);
        return CommandManager.argument(name, StringArgumentType.string())
                .suggests((c, b) -> CommandSource.suggestMatching(options, b));
    }

    static <E extends Enum<E>> E getArgument(CommandContext<ServerCommandSource> context, String argument, Class<E> type) throws CommandSyntaxException {
        try {
            return Enum.valueOf(type, StringArgumentType.getString(context, argument).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(String.join(", ", getOptions(type)));
        }
    }

    public static <T extends CommandSource> MutableText highlight(ParseResults<T> parse, String original, int firstCharacterIndex) {
        List<MutableText> list = new ArrayList<>();
        int i = 0;
        int argIndex = -1;
        CommandContextBuilder<T> commandContextBuilder = parse.getContext().getLastChild();

        for (ParsedArgument<T, ?> arg : commandContextBuilder.getArguments().values()) {
            ++argIndex;
            if (argIndex >= ARG_COLORS.size()) {
                argIndex = 0;
            }

            int k = Math.max(arg.getRange().getStart() - firstCharacterIndex, 0);
            if (k >= original.length()) {
                break;
            }

            int l = Math.min(arg.getRange().getEnd() - firstCharacterIndex, original.length());
            if (l > 0) {
                list.add(s(original.substring(i, k), Formatting.GRAY));
                list.add(s(original.substring(k, l), ARG_COLORS.get(argIndex)));
                i = l;
            }
        }

        if (parse.getReader().canRead()) {
            int m = Math.max(parse.getReader().getCursor() - firstCharacterIndex, 0);
            if (m < original.length()) {
                int n = Math.min(m + parse.getReader().getRemainingLength(), original.length());
                list.add(s(original.substring(i, m), Formatting.GRAY));
                list.add(s(original.substring(m, n), Formatting.RED));
                i = n;
            }
        }

        list.add(s(original.substring(i), Formatting.GRAY));
        return c(list);
    }
}
