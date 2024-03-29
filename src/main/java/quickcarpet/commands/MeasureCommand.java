package quickcarpet.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import quickcarpet.settings.Settings;
import quickcarpet.utils.Constants.MeasureCommand.Keys;

import java.util.Locale;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import static net.minecraft.command.argument.Vec3ArgumentType.getPosArgument;
import static net.minecraft.command.argument.Vec3ArgumentType.vec3;
import static net.minecraft.server.command.CommandManager.argument;
import static quickcarpet.utils.Messenger.*;

public class MeasureCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var measure = CommandManager.literal("measure")
            .requires(s -> s.hasPermissionLevel(Settings.commandMeasure))
            .then(argument("from", vec3())
                .executes(c -> MeasureCommand.fromPosToSource(c.getSource(), getPosArgument(c, "from")))
                .then(argument("to", vec3())
                    .executes(c -> MeasureCommand.fromPosToPos(c.getSource(), getPosArgument(c, "from"), getPosArgument(c, "to")))));
        dispatcher.register(measure);
    }

    private static int fromPosToSource(ServerCommandSource source, PosArgument from) {
        return measure(source, from.toAbsolutePos(source), source.getPosition());
    }

    private static int fromPosToPos(ServerCommandSource source, PosArgument from, PosArgument to) {
        return measure(source, from.toAbsolutePos(source),to.toAbsolutePos(source));
    }

    public static int measure(ServerCommandSource source, Vec3d from, Vec3d to) {
        m(source, t(Keys.TITLE, tp(from, Formatting.BOLD), tp(to, Formatting.BOLD)));
        Vec3d fromCentered = new Vec3d(((int) from.x) + 0.5, (int) from.y, ((int) from.z) + 0.5);
        Vec3d toCentered = new Vec3d(((int) to.x) + 0.5, (int) to.y, ((int) to.z) + 0.5);
        for (Type type : Type.values()) {
            m(source, t(Keys.LINE, type.text(),
                dbl(type.distance.applyAsDouble(from, to), Formatting.AQUA),
                dbl(type.distance.applyAsDouble(fromCentered, toCentered), Formatting.AQUA)));
        }
        return 1;
    }

    private enum Type {
        SPHERICAL(Vec3d::distanceTo),
        MANHATTAN(vec -> Math.abs(vec.x) + Math.abs(vec.y) + Math.abs(vec.z)),
        CYLINDRICAL(vec -> Math.sqrt(vec.x * vec.x + vec.z * vec.z)),
        AREA(vec -> Math.abs(vec.x) * Math.abs(vec.z)),
        VOLUME(vec -> Math.abs(vec.x) * Math.abs(vec.y) * Math.abs(vec.z));

        final ToDoubleBiFunction<Vec3d, Vec3d> distance;

        Type(ToDoubleBiFunction<Vec3d, Vec3d> distance) {
            this.distance = distance;
        }

        Type(ToDoubleFunction<Vec3d> length) {
            this.distance = (from, to) -> length.applyAsDouble(to.subtract(from));
        }

        Text text() {
            return t(Keys.PREFIX + name().toLowerCase(Locale.ROOT));
        }
    }
}
