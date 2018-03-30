package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public class SpacecraftBlockAccess implements IBlockAccess {
    private final int[] blockData;

    private final BlockPos minPos;
    private final BlockPos maxPos;

    private SpacecraftBlockAccess(int[] blockData, BlockPos minPos, BlockPos maxPos) {
        this.blockData = blockData;

        this.minPos = minPos;
        this.maxPos = maxPos;
    }

    public SpacecraftBlockAccess(BlockPos minPos, BlockPos maxPos) {
        this(new int[getDataSize(minPos, maxPos)], minPos, maxPos);
    }

    public BlockPos getMinPos() {
        return this.minPos;
    }

    public BlockPos getMaxPos() {
        return this.maxPos;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        int posIndex = getPosIndex(pos, this.minPos, this.maxPos);
        if (posIndex > -1) {
            return Block.getStateById(this.blockData[posIndex]);
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState state = this.getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 15728880;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.DEFAULT;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return WorldType.DEFAULT;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        IBlockState state = this.getBlockState(pos);
        return state.isSideSolid(this, pos, side);
    }

    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("min_x", this.minPos.getX());
        compound.setInteger("min_y", this.minPos.getY());
        compound.setInteger("min_z", this.minPos.getZ());

        compound.setInteger("max_x", this.maxPos.getX());
        compound.setInteger("max_y", this.maxPos.getY());
        compound.setInteger("max_z", this.maxPos.getZ());

        compound.setIntArray("block_data", this.blockData);

        return compound;
    }

    public static SpacecraftBlockAccess deserialize(NBTTagCompound compound) {
        BlockPos minPos = new BlockPos(compound.getInteger("min_x"), compound.getInteger("min_y"), compound.getInteger("min_z"));
        BlockPos maxPos = new BlockPos(compound.getInteger("max_x"), compound.getInteger("max_y"), compound.getInteger("max_z"));

        int expectedLength = getDataSize(minPos, maxPos);
        int[] blockData = compound.getIntArray("block_data");
        if (blockData.length != expectedLength) {
            PracticalSpaceFireworks.LOGGER.error("Loaded block data array of wrong length");
            blockData = new int[expectedLength];
        }

        return new SpacecraftBlockAccess(blockData, minPos, maxPos);
    }

    public void serialize(ByteBuf buffer) {
        buffer.writeLong(this.minPos.toLong());
        buffer.writeLong(this.maxPos.toLong());

        for (int block : this.blockData) {
            buffer.writeShort(block & 0xFFFF);
        }
    }

    public static SpacecraftBlockAccess deserialize(ByteBuf buffer) {
        BlockPos minPos = BlockPos.fromLong(buffer.readLong());
        BlockPos maxPos = BlockPos.fromLong(buffer.readLong());

        int[] blockData = new int[getDataSize(minPos, maxPos)];
        for (int i = 0; i < blockData.length; i++) {
            blockData[i] = buffer.readUnsignedShort();
        }

        return new SpacecraftBlockAccess(blockData, minPos, maxPos);
    }

    private static int getPosIndex(BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        return getPosIndex(pos.getX(), pos.getY(), pos.getZ(), minPos, maxPos);
    }

    private static int getPosIndex(int x, int y, int z, BlockPos minPos, BlockPos maxPos) {
        int sizeX = maxPos.getX() - minPos.getX() + 1;
        int sizeY = maxPos.getY() - minPos.getY() + 1;
        int sizeZ = maxPos.getZ() - minPos.getZ() + 1;
        int localX = x - minPos.getX();
        int localY = y - minPos.getY();
        int localZ = z - minPos.getZ();
        if (localX >= 0 && localY >= 0 && localZ >= 0 && localX < sizeX && localY < sizeY && localZ < sizeZ) {
            return localX + (localZ + localY * sizeZ) * sizeX;
        }
        return -1;
    }

    private static int getDataSize(BlockPos minPos, BlockPos maxPos) {
        return (maxPos.getX() - minPos.getX() + 1) * (maxPos.getY() - minPos.getY() + 1) * (maxPos.getZ() - minPos.getZ() + 1);
    }

    public static class Builder {
        private final LongList blockKeys = new LongArrayList();
        private final IntList blockValues = new IntArrayList();

        private int minX, minY, minZ;
        private int maxX, maxY, maxZ;

        public Builder setBlockState(BlockPos pos, IBlockState state) {
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

            this.blockKeys.add(pos.toLong());
            this.blockValues.add(Block.getStateId(state));

            return this;
        }

        public SpacecraftBlockAccess build() {
            BlockPos minPos = new BlockPos(this.minX, this.minY, this.minZ);
            BlockPos maxPos = new BlockPos(this.maxX, this.maxY, this.maxZ);

            int[] blockData = new int[getDataSize(minPos, maxPos)];

            for (int i = 0; i < this.blockKeys.size(); i++) {
                BlockPos key = BlockPos.fromLong(this.blockKeys.getLong(i));
                int state = this.blockValues.getInt(i);

                int posIndex = getPosIndex(key, minPos, maxPos);
                blockData[posIndex] = state;
            }

            return new SpacecraftBlockAccess(blockData, minPos, maxPos);
        }
    }
}
