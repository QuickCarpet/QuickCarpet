package quickcarpet.mixin.betterStatistics;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ScoreboardCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.QuickCarpet;
import quickcarpet.settings.Settings;
import quickcarpet.utils.StatHelper;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(method = "executeAddObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getNullableObjective(Ljava/lang/String;)Lnet/minecraft/scoreboard/ScoreboardObjective;"))
    private static ScoreboardObjective redirectNewlyCreatedObjective(Scoreboard scoreboard, String name) {
        ScoreboardObjective objective = scoreboard.getNullableObjective(name);
        if (objective != null && Settings.betterStatistics) {
            StatHelper.initialize(scoreboard, QuickCarpet.minecraft_server, objective);
        }
        return objective;
    }
}
