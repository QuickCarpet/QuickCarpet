package quickcarpet.helper;

import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.QuickCarpetRegistries;

import static quickcarpet.utils.Messenger.hoverText;
import static quickcarpet.utils.Messenger.s;

public interface FluidInfoProvider<T extends Comparable<T>> extends StateInfoProvider<FluidState, T> {
    interface Directional<T extends Comparable<T>> extends FluidInfoProvider<T>, StateInfoProvider.Directional<FluidState, T> {}
    static <T extends Comparable<T>> FluidInfoProvider<T> withFormatter(FluidInfoProvider<T> provider, Messenger.Formatter<T> formatter) {
        class WithFormatter extends StateInfoProvider.WithFormatter<FluidState, T> implements FluidInfoProvider.Directional<T> {
            public WithFormatter(StateInfoProvider<FluidState, T> provider, Messenger.Formatter<T> formatter) {
                super(provider, formatter);
            }
        }
        return new WithFormatter(provider, formatter);
    }

    static <T extends FluidInfoProvider<?>> T register(Identifier id, T provider) {
        return Registry.register(QuickCarpetRegistries.FLUID_INFO_PROVIDER, id, provider);
    }

    static <T extends FluidInfoProvider<?>> T register(String id, T provider) {
        return register(new Identifier("quickcarpet", id), provider);
    }

    FluidInfoProvider<Boolean> SOURCE = register("source", (state, world, pos) -> state.isStill());
    FluidInfoProvider<Boolean> EMPTY = register("empty", (state, world, pos) -> state.isEmpty());
    FluidInfoProvider<Boolean> HAS_RANDOM_TICKS = register("has_random_ticks", (state, world, pos) -> state.hasRandomTicks());
    FluidInfoProvider<Float> HEIGHT = register("height", FluidState::getHeight);
    FluidInfoProvider<Float> OWN_HEIGHT = register("own_height", (state, world, pos) -> state.getHeight());
    FluidInfoProvider<Integer> AMOUNT = register("amount", (state, world, pos) -> state.getLevel());
    FluidInfoProvider<Double> FLOW_SPEED = register("flow_speed", (state, world, pos) -> state.getVelocity(world, pos).length());
    FluidInfoProvider<Double> FLOW_YAW = register("flow_yaw", withFormatter((state, world, pos) -> {
        Vec3d v = state.getVelocity(world, pos);
        return MathHelper.atan2(v.z, v.x) * (180 / Math.PI) - 90;
    }, value -> hoverText(Messenger.format("%.1f°", value), s(value.toString() + "°"))));
    FluidInfoProvider<Float> BLAST_RESISTANCE = register("blast_resistance", (state, world, pos) -> state.getBlastResistance());
}
