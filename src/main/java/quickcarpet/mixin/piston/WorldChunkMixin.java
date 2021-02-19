package quickcarpet.mixin.piston;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import quickcarpet.settings.Settings;
import quickcarpet.utils.extensions.ExtendedWorldChunk;

import javax.annotation.Nullable;
import java.util.Map;

import static net.minecraft.world.chunk.WorldChunk.EMPTY_SECTION;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements ExtendedWorldChunk {
    @Shadow @Final private ChunkSection[] sections;
    @Shadow @Final private Map<Heightmap.Type, Heightmap> heightmaps;
    @Shadow private boolean shouldSave;
    @Shadow @Final private World world;
    @Nullable @Shadow public abstract BlockEntity getBlockEntity(BlockPos blockPos_1, WorldChunk.CreationType worldChunk$CreationType_1);
    @Shadow public abstract void addBlockEntity(BlockEntity blockEntity);

    @Shadow protected abstract <T extends BlockEntity> void updateTicker(T blockEntity);

    @Shadow public abstract void removeBlockEntity(BlockPos pos);

    /**
     * Sets the Blockstate and the BlockEntity.
     * Only sets BlockEntity if Block is BlockEntityProvider, but doesn't check if it actually matches (e.g. can assign beacon to chest entity).
     *
     * @author 2No2Name
     */
    @Nullable
    public BlockState setBlockStateWithBlockEntity(BlockPos pos, BlockState newBlockState, BlockEntity newBlockEntity, boolean callListeners) {
        int x = pos.getX() & 15;
        int y = pos.getY();
        int z = pos.getZ() & 15;
        ChunkSection chunkSection = this.sections[y >> 4];
        if (chunkSection == EMPTY_SECTION) {
            if (newBlockState.isAir()) {
                return null;
            }

            chunkSection = new ChunkSection(y >> 4 << 4);
            this.sections[y >> 4] = chunkSection;
        }

        boolean sectionWasEmpty = chunkSection.isEmpty();
        BlockState oldBlockState = chunkSection.setBlockState(x, y & 15, z, newBlockState);
        if (oldBlockState == newBlockState) {
            return null;
        } else {
            Block newBlock = newBlockState.getBlock();
            Block oldBlock = oldBlockState.getBlock();
            this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING).trackUpdate(x, y, z, newBlockState);
            this.heightmaps.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).trackUpdate(x, y, z, newBlockState);
            this.heightmaps.get(Heightmap.Type.OCEAN_FLOOR).trackUpdate(x, y, z, newBlockState);
            this.heightmaps.get(Heightmap.Type.WORLD_SURFACE).trackUpdate(x, y, z, newBlockState);
            boolean sectionIsEmpty = chunkSection.isEmpty();
            if (sectionWasEmpty != sectionIsEmpty) {
                this.world.getChunkManager().getLightingProvider().setSectionStatus(pos, sectionIsEmpty);
            }

            if (!this.world.isClient) {
                //this is a movableBlockEntities special case, if condition wasn't there it would remove the blockentity that was carried for some reason
                if (!(oldBlock instanceof PistonExtensionBlock))
                    oldBlockState.onStateReplaced(this.world, pos, newBlockState, callListeners); //this kills it
            } else if (oldBlock != newBlock && oldBlock instanceof BlockEntityProvider) {
                this.removeBlockEntity(pos);
            }

            if (chunkSection.getBlockState(x, y & 15, z).getBlock() != newBlock) {
                return null;
            } else {
                BlockEntity oldBlockEntity = null;
                if (oldBlockState.hasBlockEntity()) {
                    oldBlockEntity = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);
                    if (oldBlockEntity != null) {
                        oldBlockEntity.setCachedState(newBlockState);
                        this.updateTicker(oldBlockEntity);
                    }
                }

                if (newBlockState.hasBlockEntity()) {
                    if (newBlockEntity == null) {
                        newBlockEntity = ((BlockEntityProvider) newBlock).createBlockEntity(pos, newBlockState);
                    }
                    if (newBlockEntity != oldBlockEntity && newBlockEntity != null) {
                        this.removeBlockEntity(pos);
                        this.addBlockEntity(newBlockEntity);
                    }
                }

                if (!this.world.isClient) {
                    //This can call setblockstate! (e.g. hopper does)
                    newBlockState.onBlockAdded(this.world, pos, oldBlockState, callListeners);
                }

                this.shouldSave = true;
                return oldBlockState;
            }
        }
    }
}
