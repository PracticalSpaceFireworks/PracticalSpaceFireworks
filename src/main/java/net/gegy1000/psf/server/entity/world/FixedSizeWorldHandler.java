package net.gegy1000.psf.server.entity.world;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedSizeWorldHandler implements DelegatedWorld.Handler {
    protected int[] blockData;
    protected int[] lightData;
    protected Long2ObjectMap<TileEntity> entities;

    protected Biome biome;

    @Getter
    protected BlockPos minPos;
    @Getter
    protected BlockPos maxPos;

    protected DelegatedWorld parent = null;

    protected FixedSizeWorldHandler(int[] blockData, int[] lightData, Long2ObjectMap<TileEntity> entities, Biome biome, BlockPos minPos, BlockPos maxPos) {
        this.blockData = blockData;
        this.lightData = lightData;
        this.entities = entities;
        this.biome = biome;
        this.minPos = minPos;
        this.maxPos = maxPos;
    }

    @Override
    public void setParent(DelegatedWorld parent) {
        this.parent = parent;
        entities.values().forEach(te -> te.setWorld(parent));
    }

    @Override
    public void setBlockState(BlockPos pos, IBlockState state) {
        int posIndex = getPosIndex(pos, this.minPos, this.maxPos);
        if (posIndex > -1) {
            int stateId = Block.getStateId(state);
            if (blockData[posIndex] != stateId) {
                blockData[posIndex] = stateId;
                // TODO: Mark dirty for render update
            }
        }
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity entity) {
        entities.put(pos.toLong(), entity);
    }

    @Nonnull
    @Override
    public IBlockState getBlockState(BlockPos pos) {
        int posIndex = getPosIndex(pos, this.minPos, this.maxPos);
        if (posIndex > -1) {
            return Block.getStateById(this.blockData[posIndex]);
        }
        return Blocks.AIR.getDefaultState();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return entities.get(pos.toLong());
    }

    @Override
    public int getLight(BlockPos pos) {
        int posIndex = getPosIndex(pos, this.minPos, this.maxPos);
        if (posIndex > -1) {
            return lightData[posIndex];
        }
        return 15 << 20;
    }

    @Nonnull
    @Override
    public Biome getBiome(BlockPos pos) {
        return biome;
    }

    @Override
    public boolean containsBlock(BlockPos pos) {
        return pos.getX() >= minPos.getX() && pos.getY() >= minPos.getY() && pos.getZ() >= minPos.getZ()
                && pos.getX() <= maxPos.getX() && pos.getY() <= maxPos.getY() && pos.getZ() <= maxPos.getZ();
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

        compound.setTag("entities", entityList);

        return compound;
    }

    public void deserialize(NBTTagCompound compound) {
        minPos = new BlockPos(compound.getInteger("min_x"), compound.getInteger("min_y"), compound.getInteger("min_z"));
        maxPos = new BlockPos(compound.getInteger("max_x"), compound.getInteger("max_y"), compound.getInteger("max_z"));
        biome = Biome.REGISTRY.getObject(new ResourceLocation(compound.getString("biome")));
        if (biome == null) {
            PracticalSpaceFireworks.LOGGER.warn("Failed to load biome with id {}", compound.getString("biome"));
            biome = Biomes.DEFAULT;
        }

        int expectedLength = getDataSize(minPos, maxPos);

        blockData = compound.getIntArray("block_data");
        if (blockData.length != expectedLength) {
            PracticalSpaceFireworks.LOGGER.error("Loaded block data array of wrong length");
            blockData = new int[expectedLength];
        }

        lightData = compound.getIntArray("light_data");
        if (lightData.length != expectedLength) {
            PracticalSpaceFireworks.LOGGER.error("Loaded light data array of wrong length");
            lightData = new int[expectedLength];
        }

        entities = new Long2ObjectOpenHashMap<>();
        NBTTagList entityList = compound.getTagList("entities", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entityList.tagCount(); i++) {
            NBTTagCompound entityTag = entityList.getCompoundTagAt(i);
            TileEntity entity = TileEntity.create(parent, entityTag);
            if (entity == null) {
                PracticalSpaceFireworks.LOGGER.warn("Failed to deserialize TE for spacecraft");
                continue;
            }
            entities.put(entity.getPos().toLong(), entity);
        }
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

    public void deserialize(ByteBuf buffer) {
        minPos = BlockPos.fromLong(buffer.readLong());
        maxPos = BlockPos.fromLong(buffer.readLong());

        biome = Biome.getBiome(buffer.readUnsignedShort(), Biomes.DEFAULT);

        blockData = new int[getDataSize(minPos, maxPos)];
        for (int i = 0; i < blockData.length; i++) {
            blockData[i] = buffer.readUnsignedShort();
        }

        lightData = new int[getDataSize(minPos, maxPos)];
        for (int i = 0; i < lightData.length; i++) {
            lightData[i] = buffer.readInt();
        }

        int entityCount = buffer.readUnsignedShort();
        entities = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < entityCount; i++) {
            NBTTagCompound entityTag = ByteBufUtils.readTag(buffer);
            TileEntity entity = TileEntity.create(parent, entityTag);
            if (entity == null) {
                PracticalSpaceFireworks.LOGGER.warn("Failed to deserialize TE");
                continue;
            }
            entities.put(entity.getPos().toLong(), entity);
        }
    }

    public static int getPosIndex(BlockPos pos, BlockPos minPos, BlockPos maxPos) {
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

    public static int getDataSize(BlockPos minPos, BlockPos maxPos) {
        return (maxPos.getX() - minPos.getX() + 1) * (maxPos.getY() - minPos.getY() + 1) * (maxPos.getZ() - minPos.getZ() + 1);
    }
}
