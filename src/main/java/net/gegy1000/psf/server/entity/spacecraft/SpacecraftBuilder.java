package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.Lists;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.List;

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

    public SpacecraftBuilder copyFrom(World world, BlockPos origin, @Nullable CraftGraph positions) {
        if (positions == null) {
            return this;
        }
        List<BlockPos> locations = Lists.newArrayList(positions.getPositions());
        for (BlockPos pos : locations) {
            BlockPos localPos = pos.subtract(origin);
            IBlockState state = world.getBlockState(pos);
            this.setBlockState(localPos, state);

            TileEntity entity = world.getTileEntity(pos);
            if (entity != null) {
                NBTTagCompound tag = entity.serializeNBT();
                tag.setInteger("x", localPos.getX());
                tag.setInteger("y", localPos.getY());
                tag.setInteger("z", localPos.getZ());
                TileEntity copiedEntity = TileEntity.create(world, tag);
                if (copiedEntity == null) {
                    PracticalSpaceFireworks.LOGGER.warn("Failed to copy TE when building spacecraft");
                    continue;
                }
                this.setTileEntity(localPos, copiedEntity);
            }
        }
        return this;
    }

    public SpacecraftWorldHandler buildWorldHandler(BlockPos origin, World world) {
        int[] blockData = new int[SpacecraftWorldHandler.getDataSize(minPos, maxPos)];
        for (int i = 0; i < this.blockKeys.size(); i++) {
            BlockPos pos = BlockPos.fromLong(this.blockKeys.getLong(i));
            int state = this.blockValues.getInt(i);

            blockData[SpacecraftWorldHandler.getPosIndex(pos, minPos, maxPos)] = state;
        }
        MutableBlockPos mutablePos = new MutableBlockPos(origin);
        int[] lightData = new int[SpacecraftWorldHandler.getDataSize(minPos, maxPos)];
        for (BlockPos pos : BlockPos.getAllInBoxMutable(minPos, maxPos)) {
            mutablePos.setPos(origin.getX() + pos.getX(), origin.getY() + pos.getY(), origin.getZ() + pos.getZ());
            int index = SpacecraftWorldHandler.getPosIndex(pos, minPos, maxPos);
            lightData[index] = getCombinedLight(world, mutablePos);
            mutablePos.setPos(origin);
        }

        Biome biome = world.getBiome(origin);

        return new SpacecraftWorldHandler(blockData, lightData, this.entities, biome, minPos, maxPos);
    }

    private int getCombinedLight(World world, MutableBlockPos pos) {
        int skyLight = getLightFromNeighborsFor(world, EnumSkyBlock.SKY, pos);
        int blockLight = getLightFromNeighborsFor(world, EnumSkyBlock.BLOCK, pos);
        return skyLight << 20 | blockLight << 4;
    }

    private int getLightFromNeighborsFor(World world, EnumSkyBlock type, MutableBlockPos pos) {
        if (world.provider.hasSkyLight() || type != EnumSkyBlock.SKY) {
            pos.setY(Math.max(0, pos.getY()));
            if (world.isValid(pos) && world.isBlockLoaded(pos)) {
                if (world.getBlockState(pos).useNeighborBrightness()) {
                    int u = getLightForSide(world, type, pos, EnumFacing.UP);
                    int n = getLightForSide(world, type, pos, EnumFacing.NORTH);
                    int s = getLightForSide(world, type, pos, EnumFacing.SOUTH);
                    int w = getLightForSide(world, type, pos, EnumFacing.WEST);
                    int e = getLightForSide(world, type, pos, EnumFacing.EAST);
                    return Math.max(u, Math.max(e, Math.max(w, Math.max(s, n))));
                }
                Chunk chunk = world.getChunkFromBlockCoords(pos);
                return chunk.getLightFor(type, pos);
            } else return type.defaultLightValue;
        }
        return 0;
    }

    private int getLightForSide(World world, EnumSkyBlock type, MutableBlockPos pos, EnumFacing side) {
        pos.move(side);
        boolean invalidY = pos.getY() < 0;
        int light;
        if (invalidY) pos.setY(0);
        if (world.isValid(pos) && world.isBlockLoaded(pos)) {
            Chunk chunk = world.getChunkFromBlockCoords(pos);
            light = chunk.getLightFor(type, pos);
        } else light = type.defaultLightValue;
        if (!invalidY) pos.move(side.getOpposite());
        return light;
    }
}
