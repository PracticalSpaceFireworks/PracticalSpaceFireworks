package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SpacecraftBlockAccess implements IBlockAccess {
    private final int[] blockData;
    private final int[] lightData;
    private final Long2ObjectMap<TileEntity> entities;

    private final Biome biome;

    private final BlockPos minPos;
    private final BlockPos maxPos;

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
        return this.entities.get(pos.toLong());
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        int posIndex = getPosIndex(pos, this.minPos, this.maxPos);
        if (posIndex > -1) {
            return this.lightData[posIndex];
        }
        return 15 << 20 | 15 << 4;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.biome;
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

        if (this.biome.getRegistryName() != null) {
            compound.setString("biome", this.biome.getRegistryName().toString());
        }

        compound.setIntArray("block_data", this.blockData);
        compound.setIntArray("light_data", this.lightData);

        NBTTagList entityList = new NBTTagList();
        for (Map.Entry<Long, TileEntity> entry : this.entities.entrySet()) {
            entityList.appendTag(entry.getValue().serializeNBT());
        }

        return compound;
    }

    public static SpacecraftBlockAccess deserialize(NBTTagCompound compound) {
        BlockPos minPos = new BlockPos(compound.getInteger("min_x"), compound.getInteger("min_y"), compound.getInteger("min_z"));
        BlockPos maxPos = new BlockPos(compound.getInteger("max_x"), compound.getInteger("max_y"), compound.getInteger("max_z"));
        Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(compound.getString("biome")));

        int expectedLength = getDataSize(minPos, maxPos);

        int[] blockData = compound.getIntArray("block_data");
        if (blockData.length != expectedLength) {
            PracticalSpaceFireworks.LOGGER.error("Loaded block data array of wrong length");
            blockData = new int[expectedLength];
        }

        int[] lightData = compound.getIntArray("light_data");
        if (lightData.length != expectedLength) {
            PracticalSpaceFireworks.LOGGER.error("Loaded light data array of wrong length");
            lightData = new int[expectedLength];
        }

        Long2ObjectMap<TileEntity> entities = new Long2ObjectOpenHashMap<>();
        NBTTagList entityList = compound.getTagList("entities", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entityList.tagCount(); i++) {
            NBTTagCompound entityTag = entityList.getCompoundTagAt(i);
            TileEntity entity = TileEntity.create(null, entityTag);
            if (entity == null) {
                PracticalSpaceFireworks.LOGGER.warn("Failed to deserialize TE for spacecraft");
                continue;
            }
            entities.put(entity.getPos().toLong(), entity);
        }

        return new SpacecraftBlockAccess(blockData, lightData, entities, biome, minPos, maxPos);
    }

    public void serialize(ByteBuf buffer) {
        buffer.writeLong(this.minPos.toLong());
        buffer.writeLong(this.maxPos.toLong());
        buffer.writeShort(Biome.getIdForBiome(this.biome) & 0xFFFF);

        for (int block : this.blockData) {
            buffer.writeShort(block & 0xFFFF);
        }

        for (int light : this.lightData) {
            buffer.writeInt(light);
        }

        buffer.writeShort(this.entities.size() & 0xFFFF);
        for (Map.Entry<Long, TileEntity> entry : this.entities.entrySet()) {
            ByteBufUtils.writeTag(buffer, entry.getValue().serializeNBT());
        }
    }

    public static SpacecraftBlockAccess deserialize(ByteBuf buffer) {
        BlockPos minPos = BlockPos.fromLong(buffer.readLong());
        BlockPos maxPos = BlockPos.fromLong(buffer.readLong());

        Biome biome = Biome.getBiome(buffer.readUnsignedShort(), Biomes.DEFAULT);

        int[] blockData = new int[getDataSize(minPos, maxPos)];
        for (int i = 0; i < blockData.length; i++) {
            blockData[i] = buffer.readUnsignedShort();
        }

        int[] lightData = new int[getDataSize(minPos, maxPos)];
        for (int i = 0; i < lightData.length; i++) {
            lightData[i] = buffer.readInt();
        }

        int entityCount = buffer.readUnsignedShort();
        Long2ObjectMap<TileEntity> entities = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < entityCount; i++) {
            NBTTagCompound entityTag = ByteBufUtils.readTag(buffer);
            TileEntity entity = TileEntity.create(null, entityTag);
            if (entity == null) {
                PracticalSpaceFireworks.LOGGER.warn("Failed to deserialize TE for spacecraft");
                continue;
            }
            entities.put(entity.getPos().toLong(), entity);
        }

        return new SpacecraftBlockAccess(blockData, lightData, entities, biome, minPos, maxPos);
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
