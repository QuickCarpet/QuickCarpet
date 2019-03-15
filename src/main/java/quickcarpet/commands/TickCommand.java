package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import quickcarpet.QuickCarpetSettings;
import quickcarpet.helper.TickSpeed;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static net.minecraft.server.command.ServerCommandManager.argument;
import static net.minecraft.server.command.ServerCommandManager.literal;

public class TickCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = literal("tick").
                requires((player) -> QuickCarpetSettings.getBool("commandTick")).
                then(literal("rate").
                        executes((c) -> TickSpeed.sendUsage(c.getSource())).
                        then(argument("rate", floatArg(0.1F, 500.0F)).
                                suggests( (c, b) -> CommandSource.suggestMatching(new String[]{"20.0"},b)).
                        executes((c) -> TickSpeed.setRate(c.getSource(), getFloat(c, "rate"))))).
                then(literal("warp")
                              .executes(context -> TickSpeed.sendUsage(context.getSource()))
                              .then(argument("ticks", IntegerArgumentType.integer(0)).
                                      suggests( (c, b) -> CommandSource.suggestMatching(new String[]{"3600","72000"},b)).
                                      executes(context -> TickSpeed.setWarp(context.getSource(), IntegerArgumentType.getInteger(context, "ticks")))));
        /*
                then(literal("freeze").executes( (c)-> toggleFreeze(c.getSource()))).
                then(literal("step").
                        executes((c) -> step(1)).
                        then(argument("ticks", integer(1,72000)).
                                suggests( (c, b) -> CommandSource.suggestMatching(new String[]{"20"},b)).
                                executes((c) -> step(getInteger(c,"ticks"))))).
                then(literal("superHot").executes( (c)-> toggleSuperHot(c.getSource()))).
                then(literal("health").
                        executes( (c) -> healthReport(c.getSource(), 100)).
                        then(argument("ticks", integer(20,24000)).
                                executes( (c) -> healthReport(c.getSource(), getInteger(c, "ticks"))))).
                then(literal("entities").
                        executes((c) -> healthEntities(c.getSource(), 100)).
                        then(argument("ticks", integer(20,24000)).
                                executes((c) -> healthEntities(c.getSource(), getInteger(c, "ticks")))));
         */
        dispatcher.register(literalargumentbuilder);
    }
}
