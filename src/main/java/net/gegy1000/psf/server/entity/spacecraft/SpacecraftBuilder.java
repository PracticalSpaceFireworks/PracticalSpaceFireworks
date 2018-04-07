package net.gegy1000.psf.server.entity.spacecraft;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.controller.CraftGraph;
import net.gegy1000.psf.server.util.PointUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

public class SpacecraftBuilder {
    private final LongList blockKeys = new LongArrayList();
    private final IntList blockValues = new IntArrayList();
    private final Long2ObjectMap<TileEntity> entities = new Long2ObjectOpenHashMap<>();

    private BlockPos minPos = BlockPos.ORIGIN;
    private BlockPos maxPos = BlockPos.ORIGIN;

    public void setBlockState(BlockPos pos, IBlockState state) {
        this.updateBounds(pos);

        this.blockKeys.add(pos.toLong());
        this.blockValues.add(Block.getStateId(state));
    }

    public void setTileEntity(BlockPos pos, TileEntity entity) {
        this.updateBounds(pos);

        this.entities.put(pos.toLong(), entity);
    }

    private void updateBounds(BlockPos pos) {
        this.minPos = PointUtils.min(pos, this.minPos);
        this.maxPos = PointUtils.max(pos, this.maxPos);
    }

    public void copyFrom(World world, BlockPos origin, @Nullable CraftGraph positions) {
        if (positions == null) {
            return;
        }
        List<BlockPos> locations = Lists.newArrayList(positions.getPositions());
        for (BlockPos pos : locations) {
            BlockPos localPos = pos.subtract(origin);
            IBlockState state = world.getBlockState(pos);
            this.setBlockState(localPos, world.getBlockState(pos));

            TileEntity entity = world.getTileEntity(pos);
            if (entity != null) {
                TileEntity copiedEntity = TileEntity.create(world, entity.serializeNBT());
                if (copiedEntity == null) {
                    PracticalSpaceFireworks.LOGGER.warn("Failed to copy TE when building spacecraft");
                    continue;
                }
                copiedEntity.setPos(localPos);
                this.setTileEntity(localPos, copiedEntity);
            }
        }
    }

    public SpacecraftWorldHandler buildWorldHandler(BlockPos origin, World world) {
        int[] blockData = new int[SpacecraftWorldHandler.getDataSize(minPos, maxPos)];
        for (int i = 0; i < this.blockKeys.size(); i++) {
            BlockPos pos = BlockPos.fromLong(this.blockKeys.getLong(i));
            int state = this.blockValues.getInt(i);

            blockData[SpacecraftWorldHandler.getPosIndex(pos, minPos, maxPos)] = state;
        }

        int[] lightData = new int[SpacecraftWorldHandler.getDataSize(minPos, maxPos)];
        for (BlockPos pos : BlockPos.getAllInBoxMutable(minPos, maxPos)) {
            lightData[SpacecraftWorldHandler.getPosIndex(pos, minPos, maxPos)] = getCombinedLight(world, origin.add(pos));
        }

        Biome biome = world.getBiome(origin);

        return new SpacecraftWorldHandler(blockData, lightData, this.entities, biome, minPos, maxPos);
    }

    private int getCombinedLight(World world, BlockPos pos) {
        int skyLight = this.getLightFromNeighborsFor(world, EnumSkyBlock.SKY, pos);
        int blockLight = this.getLightFromNeighborsFor(world, EnumSkyBlock.BLOCK, pos);
        return skyLight << 20 | blockLight << 4;
    }

    private int getLightFromNeighborsFor(World world, EnumSkyBlock type, BlockPos pos) {
        if (!world.provider.hasSkyLight() && type == EnumSkyBlock.SKY) {
            return 0;
        } else {
            if (pos.getY() < 0) {
                pos = new BlockPos(pos.getX(), 0, pos.getZ());
            }
            if (!world.isValid(pos)) {
                return type.defaultLightValue;
            } else if (!world.isBlockLoaded(pos)) {
                return type.defaultLightValue;
            } else if (world.getBlockState(pos).useNeighborBrightness()) {
                int lightUp = world.getLightFor(type, pos.up());
                int lightEast = world.getLightFor(type, pos.east());
                int lightWest = world.getLightFor(type, pos.west());
                int lightSouth = world.getLightFor(type, pos.south());
                int lightNorth = world.getLightFor(type, pos.north());
                return Math.max(lightUp, Math.max(lightEast, Math.max(lightWest, Math.max(lightSouth, lightNorth))));
            } else {
                return world.getChunkFromBlockCoords(pos).getLightFor(type, pos);
            }
        }
    }
}
