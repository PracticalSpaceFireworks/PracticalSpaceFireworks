package net.gegy1000.psf.server.entity.spacecraft;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SpacecraftBlockAccess implements IBlockAccess {
    private final int[] blockData;

    private final BlockPos minPos;
    private final BlockPos maxPos;
    
    private final BlockPos offset;
    private final World world;

    public SpacecraftBlockAccess(BlockPos minPos, BlockPos maxPos, BlockPos offset, World world) {
        this(new int[getDataSize(minPos, maxPos)], minPos, maxPos, offset, world);
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
        return world.getCombinedLight(pos.add(offset), lightValue);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return world.getBiome(pos.add(offset));
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

        compound.setInteger("off_x", this.offset.getX());
        compound.setInteger("off_y", this.offset.getY());
        compound.setInteger("off_z", this.offset.getZ());

        compound.setIntArray("block_data", this.blockData);

        return compound;
    }

    public static SpacecraftBlockAccess deserialize(NBTTagCompound compound, World world) {
        BlockPos minPos = new BlockPos(compound.getInteger("min_x"), compound.getInteger("min_y"), compound.getInteger("min_z"));
        BlockPos maxPos = new BlockPos(compound.getInteger("max_x"), compound.getInteger("max_y"), compound.getInteger("max_z"));
        BlockPos offset = new BlockPos(compound.getInteger("off_x"), compound.getInteger("off_y"), compound.getInteger("off_z"));

        int expectedLength = getDataSize(minPos, maxPos);
        int[] blockData = compound.getIntArray("block_data");
        if (blockData.length != expectedLength) {
            PracticalSpaceFireworks.LOGGER.error("Loaded block data array of wrong length");
            blockData = new int[expectedLength];
        }

        return new SpacecraftBlockAccess(blockData, minPos, maxPos, offset, world);
    }

    public void serialize(ByteBuf buffer) {
        buffer.writeLong(this.minPos.toLong());
        buffer.writeLong(this.maxPos.toLong());
        buffer.writeLong(this.offset.toLong());

        for (int block : this.blockData) {
            buffer.writeShort(block & 0xFFFF);
        }
    }

    public static SpacecraftBlockAccess deserialize(ByteBuf buffer, World world) {
        BlockPos minPos = BlockPos.fromLong(buffer.readLong());
        BlockPos maxPos = BlockPos.fromLong(buffer.readLong());
        BlockPos offset = BlockPos.fromLong(buffer.readLong());

        int[] blockData = new int[getDataSize(minPos, maxPos)];
        for (int i = 0; i < blockData.length; i++) {
            blockData[i] = buffer.readUnsignedShort();
        }

        return new SpacecraftBlockAccess(blockData, minPos, maxPos, offset, world);
    }

    static int getPosIndex(BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        int sizeX = maxPos.getX() - minPos.getX() + 1;
        int sizeY = maxPos.getY() - minPos.getY() + 1;
        int sizeZ = maxPos.getZ() - minPos.getZ() + 1;
        int localX = pos.getX() - minPos.getX();
        int localY = pos.getY() - minPos.getY();
        int localZ = pos.getZ() - minPos.getZ();
        if (localX >= 0 && localY >= 0 && localZ >= 0 && localX < sizeX && localY < sizeY && localZ < sizeZ) {
            return localX + (localZ + localY * sizeZ) * sizeX;
        }
        return -1;
    }

    static int getDataSize(BlockPos minPos, BlockPos maxPos) {
        return (maxPos.getX() - minPos.getX() + 1) * (maxPos.getY() - minPos.getY() + 1) * (maxPos.getZ() - minPos.getZ() + 1);
    }
}