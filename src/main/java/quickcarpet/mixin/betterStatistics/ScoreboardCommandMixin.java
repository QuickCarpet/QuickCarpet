package quickcarpet.mixin.betterStatistics;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.QuickCarpetServer;
import quickcarpet.settings.Settings;
import quickcarpet.utils.StatHelper;

import java.util.function.Predicate;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(method = "executeAddObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getNullableObjective(Ljava/lang/String;)Lnet/minecraft/scoreboard/ScoreboardObjective;"))
    private static ScoreboardObjective redirectNewlyCreatedObjective(Scoreboard scoreboard, String name) {
        ScoreboardObjective objective = scoreboard.getNullableObjective(name);
        if (objective != null && Settings.betterStatistics) {
            StatHelper.initialize(scoreboard, QuickCarpetServer.getMinecraftServer(), objective);
        }
        return objective;
    }

    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"))
    private static ArgumentBuilder<?, ?> changeRequires(LiteralArgumentBuilder<ServerCommandSource> builder, Predicate<ServerCommandSource> requirement) {
        return builder.requires(s -> Settings.commandScoreboardPublic || requirement.test(s));
    }

    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;"))
    private static LiteralArgumentBuilder<ServerCommandSource> addRequirements(String literal) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal(literal);
        switch (literal) {
            case "players": case "teams":
            case "remove": case "modify": return builder.requires(s -> s.hasPermissionLevel(2));
            default: return builder;
        }
    }
}
