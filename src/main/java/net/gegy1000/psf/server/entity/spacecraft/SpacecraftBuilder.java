package net.gegy1000.psf.server.entity.spacecraft;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Set;

public class SpacecraftBuilder {
    private final LongList blockKeys = new LongArrayList();
    private final IntList blockValues = new IntArrayList();
    private final Long2ObjectMap<TileEntity> entities = new Long2ObjectOpenHashMap<>();

    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

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
        if (pos.getX() < this.minX) {
            this.minX = pos.getX();
        }
        if (pos.getX() > this.maxX) {
            this.maxX = pos.getX();
        }

        if (pos.getY() < this.minY) {
            this.minY = pos.getY();
        }
        if (pos.getY() > this.maxY) {
            this.maxY = pos.getY();
        }

        if (pos.getZ() < this.minZ) {
            this.minZ = pos.getZ();
        }
        if (pos.getZ() > this.maxZ) {
            this.maxZ = pos.getZ();
        }
    }

    public void copyFrom(World world, BlockPos origin, Set<BlockPos> positions) {
        for (BlockPos pos : positions) {
            BlockPos localPos = pos.subtract(origin);
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

    public SpacecraftBlockAccess buildBlockAccess(BlockPos origin, World world) {
        BlockPos minPos = new BlockPos(this.minX, this.minY, this.minZ);
        BlockPos maxPos = new BlockPos(this.maxX, this.maxY, this.maxZ);

        int[] blockData = new int[SpacecraftBlockAccess.getDataSize(minPos, maxPos)];
        for (int i = 0; i < this.blockKeys.size(); i++) {
            BlockPos pos = BlockPos.fromLong(this.blockKeys.getLong(i));
            int state = this.blockValues.getInt(i);

            blockData[SpacecraftBlockAccess.getPosIndex(pos, minPos, maxPos)] = state;
        }

        int[] lightData = new int[SpacecraftBlockAccess.getDataSize(minPos, maxPos)];
        for (BlockPos pos : BlockPos.getAllInBoxMutable(minPos, maxPos)) {
            lightData[SpacecraftBlockAccess.getPosIndex(pos, minPos, maxPos)] = getCombinedLight(world, origin.add(pos));
        }

        Biome biome = world.getBiome(origin);

        return new SpacecraftBlockAccess(blockData, lightData, this.entities, biome, minPos, maxPos);
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
