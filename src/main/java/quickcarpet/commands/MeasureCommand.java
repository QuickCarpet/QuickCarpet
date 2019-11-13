package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import quickcarpet.settings.Settings;

import java.util.Locale;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import static net.minecraft.command.arguments.Vec3ArgumentType.getPosArgument;
import static net.minecraft.command.arguments.Vec3ArgumentType.vec3;
import static net.minecraft.server.command.CommandManager.argument;
import static quickcarpet.utils.Messenger.*;

public class MeasureCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> distance = CommandManager.literal("measure")
            .requires(s -> s.hasPermissionLevel(Settings.commandMeasure))
            .then(argument("from", vec3())
                .executes(c -> MeasureCommand.fromPosToSource(c.getSource(), getPosArgument(c, "from")))
                .then(argument("to", vec3())
                    .executes(c -> MeasureCommand.fromPosToPos(c.getSource(), getPosArgument(c, "from"), getPosArgument(c, "to")))));
        dispatcher.register(distance);
    }

    private static int fromPosToSource(ServerCommandSource source, PosArgument from) {
        return measure(source, from.toAbsolutePos(source), source.getPosition());
    }

    private static int fromPosToPos(ServerCommandSource source, PosArgument from, PosArgument to) {
        return measure(source, from.toAbsolutePos(source),to.toAbsolutePos(source));
    }

    private static int measure(ServerCommandSource source, Vec3d from, Vec3d to) {
        m(source, t("command.measure.title", tp("b", from), tp("b", to)));
        Vec3d fromCentered = new Vec3d(((int) from.x) + 0.5, (int) from.y, ((int) from.z) + 0.5);
        Vec3d toCentered = new Vec3d(((int) to.x) + 0.5, (int) to.y, ((int) to.z) + 0.5);
        for (Type type : Type.values()) {
            m(source, t("command.measure.line", type.text(),
                dbl("c", type.distance.applyAsDouble(from, to)),
                dbl("c", type.distance.applyAsDouble(fromCentered, toCentered))));
        }
        return 1;
    }

    private enum Type {
        SPHERICAL(Vec3d::distanceTo),
        MANHATTAN(vec -> Math.abs(vec.x) + Math.abs(vec.y) + Math.abs(vec.z)),
        CYLINDRICAL(vec -> Math.sqrt(vec.x * vec.x + vec.z * vec.z)),
        AREA(vec -> Math.abs(vec.x) * Math.abs(vec.z)),
        VOLUME(vec -> Math.abs(vec.x) * Math.abs(vec.y) * Math.abs(vec.z));

        ToDoubleBiFunction<Vec3d, Vec3d> distance;

        Type(ToDoubleBiFunction<Vec3d, Vec3d> distance) {
            this.distance = distance;
        }

        Type(ToDoubleFunction<Vec3d> length) {
            this.distance = (from, to) -> length.applyAsDouble(to.subtract(from));
        }

        Text text() {
            return t("command.measure." + name().toLowerCase(Locale.ROOT));
        }
    }
}
