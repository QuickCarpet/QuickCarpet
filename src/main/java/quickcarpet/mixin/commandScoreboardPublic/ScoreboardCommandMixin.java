package quickcarpet.mixin.commandScoreboardPublic;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;

import java.util.function.Predicate;

@Mixin(ScoreboardCommand.class)
public class ScoreboardCommandMixin {
    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;", remap = false))
    private static ArgumentBuilder<?, ?> changeRequires(LiteralArgumentBuilder<ServerCommandSource> builder, Predicate<ServerCommandSource> requirement) {
        return builder.requires(s -> Settings.commandScoreboardPublic || requirement.test(s));
    }

    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;"))
    private static LiteralArgumentBuilder<ServerCommandSource> addRequirements(String literal) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal(literal);
        return switch (literal) {
            case "players", "teams", "remove", "modify" -> builder.requires(s -> s.hasPermissionLevel(2));
            default -> builder;
        };
    }
}
