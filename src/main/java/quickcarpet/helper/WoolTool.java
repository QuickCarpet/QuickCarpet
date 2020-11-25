package quickcarpet.helper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class WoolTool {
    private static final HashMap<MapColor, DyeColor> MATERIAL_TO_DYE_COLOR = new HashMap<>();

    static {
        for (DyeColor color : DyeColor.values()) {
            MATERIAL_TO_DYE_COLOR.put(color.getMapColor(), color);
        }
    }

    public static DyeColor getWoolColorAtPosition(World worldIn, BlockPos pos) {
        BlockState state = worldIn.getBlockState(pos);
        if (state.getMaterial() != Material.WOOL || !Block.isShapeFullCube(state.getCollisionShape(worldIn, pos)))
            return null;
        return MATERIAL_TO_DYE_COLOR.get(state.getMapColor(worldIn, pos));
    }
}
