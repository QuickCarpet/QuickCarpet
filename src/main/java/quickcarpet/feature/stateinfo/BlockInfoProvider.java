package quickcarpet.feature.stateinfo;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import quickcarpet.utils.Messenger;
import quickcarpet.utils.QuickCarpetRegistries;

public interface BlockInfoProvider<T extends Comparable<T>> extends StateInfoProvider<BlockState, T> {
    interface Directional<T extends Comparable<T>> extends BlockInfoProvider<T>, StateInfoProvider.Directional<BlockState, T> {}
    static <T extends Comparable<T>> BlockInfoProvider<T> withFormatter(BlockInfoProvider<T> provider, Messenger.Formatter<T> formatter) {
        class WithFormatter extends StateInfoProvider.WithFormatter<BlockState, T> implements Directional<T> {
            public WithFormatter(StateInfoProvider<BlockState, T> provider, Messenger.Formatter<T> formatter) {
                super(provider, formatter);
            }
        }
        return new WithFormatter(provider, formatter);
    }

    static <T extends BlockInfoProvider<?>> T register(Identifier id, T provider) {
        return Registry.register(QuickCarpetRegistries.BLOCK_INFO_PROVIDER, id, provider);
    }

    static <T extends BlockInfoProvider<?>> T register(String id, T provider) {
        return register(new Identifier("quickcarpet", id), provider);
    }

    BlockInfoProvider<Boolean> PROPAGATES_SKYLIGHT = register("propagates_skylight", AbstractBlock.AbstractBlockState::isTranslucent);
    BlockInfoProvider<Integer> OPACITY = register("opacity", AbstractBlock.AbstractBlockState::getOpacity);
    BlockInfoProvider<Boolean> LARGE_COLLISION_SHAPE = register("large_collision_shape", (state, world, pos) -> state.exceedsCube());
    BlockInfoProvider<Boolean> USE_SHAPE_FOR_LIGHT_OCCLUSION = register("use_shape_for_light_occlusion", (state, world, pos) -> state.hasSidedTransparency());
    BlockInfoProvider<Integer> LIGHT_EMISSION = register("light_emission", (state, world, pos) -> state.getLuminance());
    BlockInfoProvider<Boolean> AIR = register("air", (state, world, pos) -> state.isAir());
    BlockInfoProvider<Integer> MAP_COLOR = register("map_color", withFormatter(
        (state, world, pos) -> state.getMapColor(world, pos).color,
        color -> Messenger.format("#%06x", color)
    ));
    BlockInfoProvider<Boolean> CONDUCTS_POWER = register("conducts_power", AbstractBlock.AbstractBlockState::isSolidBlock);
    BlockInfoProvider<Boolean> EMITS_POWER = register("emits_power", (state, world, pos) -> state.emitsRedstonePower());
    BlockInfoProvider.Directional<Integer> WEAK_POWER = register("weak_power", (state, world, pos, direction) -> state.getWeakRedstonePower(world, pos, direction.getOpposite()));
    BlockInfoProvider.Directional<Integer> STRONG_POWER = register("strong_power", (state, world, pos, direction) -> state.getStrongRedstonePower(world, pos, direction.getOpposite()));
    BlockInfoProvider<Integer> ANALOG_SIGNAL = register("analog_signal", AbstractBlock.AbstractBlockState::getComparatorOutput);
    BlockInfoProvider<Float> DESTROY_SPEED = register("destroy_speed", AbstractBlock.AbstractBlockState::getHardness);
    BlockInfoProvider<PistonBehavior> PUSH_REACTION = register("push_reaction", (state, world, pos) -> state.getPistonBehavior());
    BlockInfoProvider<Boolean> SIMPLE_FULL_CUBE = register("simple_full_cube", AbstractBlock.AbstractBlockState::isOpaqueFullCube);
    BlockInfoProvider<Boolean> OPAQUE = register("opaque", (state, world, pos) -> state.isOpaque());
    BlockInfoProvider<Double> OFFSET_X = register("offset_x", (state, world, pos) -> state.getModelOffset(world, pos).x);
    BlockInfoProvider<Double> OFFSET_Y = register("offset_y", (state, world, pos) -> state.getModelOffset(world, pos).y);
    BlockInfoProvider<Double> OFFSET_Z = register("offset_z", (state, world, pos) -> state.getModelOffset(world, pos).z);
    BlockInfoProvider<Boolean> SUFFOCATES = register("suffocates", AbstractBlock.AbstractBlockState::shouldSuffocate);
    BlockInfoProvider<Boolean> PATHFINDABLE_LAND = register("pathfindable_land", (state, world, pos) -> state.canPathfindThrough(world, pos, NavigationType.LAND));
    BlockInfoProvider<Boolean> PATHFINDABLE_AIR = register("pathfindable_air", (state, world, pos) -> state.canPathfindThrough(world, pos, NavigationType.AIR));
    BlockInfoProvider<Boolean> PATHFINDABLE_WATER = register("pathfindable_water", (state, world, pos) -> state.canPathfindThrough(world, pos, NavigationType.WATER));
    BlockInfoProvider<Boolean> CAN_SURVIVE = register("can_survive", AbstractBlock.AbstractBlockState::canPlaceAt);
    BlockInfoProvider<Boolean> POST_PROCESS = register("post_process", AbstractBlock.AbstractBlockState::shouldPostProcess);
    BlockInfoProvider<Boolean> RANDOM_TICKS = register("random_ticks", (state, world, pos) -> state.hasRandomTicks());
    BlockInfoProvider.Directional<Boolean> FACE_STURDY = register("face_sturdy", AbstractBlock.AbstractBlockState::isSideSolidFullSquare);
    BlockInfoProvider.Directional<Boolean> CENTER_STURDY = register("center_sturdy", (state, world, pos, direction) -> state.isSideSolid(world, pos, direction, SideShapeType.CENTER));
    BlockInfoProvider.Directional<Boolean> RIGID_STURDY = register("rigid_sturdy", (state, world, pos, direction) -> state.isSideSolid(world, pos, direction, SideShapeType.RIGID));
    BlockInfoProvider<Boolean> FULL_CUBE = register("full_cube", AbstractBlock.AbstractBlockState::isFullCube);
    BlockInfoProvider<Boolean> REQUIRES_CORRECT_TOOL = register("requires_correct_tool", (state, world, pos) -> state.isToolRequired());
    BlockInfoProvider<Boolean> MATERIAL_SOLID = register("material_solid", (state, world, pos) -> state.getMaterial().isSolid());
    BlockInfoProvider<Boolean> MATERIAL_BLOCKS_MOVEMENT = register("material_blocks_movement", (state, world, pos) -> state.getMaterial().blocksMovement());
    BlockInfoProvider<Boolean> MATERIAL_LIQUID = register("material_liquid", (state, world, pos) -> state.getMaterial().isLiquid());
    BlockInfoProvider<Boolean> MATERIAL_FLAMMABLE = register("material_flammable", (state, world, pos) -> state.getMaterial().isBurnable());
    BlockInfoProvider<Boolean> MATERIAL_REPLACEABLE = register("material_replaceable", (state, world, pos) -> state.getMaterial().isReplaceable());
    BlockInfoProvider<PistonBehavior> MATERIAL_PUSH_REACTION = register("material_push_reaction", (state, world, pos) -> state.getMaterial().getPistonBehavior());
    BlockInfoProvider<Integer> MATERIAL_COLOR = register("material_color", withFormatter(
        (state, world, pos) -> state.getMaterial().getColor().color,
        color -> Messenger.format("#%06x", color)
    ));
}
