package net.gegy1000.psf.server.entity.world;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.io.File;

import net.gegy1000.psf.api.util.IWorldData;
import net.gegy1000.psf.server.util.ServerEmptyChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@ParametersAreNonnullByDefault
public class DelegatedWorld extends World {
    private final World parent;
    private final IWorldData data;

    public DelegatedWorld(World parent, IWorldData data) {
        super(new SaveHandler(), parent.getWorldInfo(), parent.provider, parent.profiler, parent.isRemote);
        this.parent = parent;
        this.data = data;
    }

    @Nonnull
    public World getParent() {
        return parent;
    }

    @Override
    @Nonnull
    protected IChunkProvider createChunkProvider() {
        return new ChunkProvider();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return new ServerEmptyChunk(this, chunkX, chunkZ);
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return !data.containsBlock(pos);
    }

    @Override
    @Nonnull
    public IBlockState getBlockState(BlockPos pos) {
        if (data.containsBlock(pos)) {
            return data.getBlockState(pos);
        }
        return Blocks.AIR.getDefaultState();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if (data.containsBlock(pos)) {
            TileEntity entity = data.getTileEntity(pos);
            if (entity != null && entity.getWorld() != this) {
                entity.setWorld(this);
            }
            return entity;
        }
        return null;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        if (data.containsBlock(pos)) {
            data.setBlockState(pos, state);
            return true;
        }
        return false;
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity entity) {
        if (data.containsBlock(pos)) {
            data.setTileEntity(pos, entity);
        }
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        if (data.containsBlock(pos)) {
            IBlockState state = data.getBlockState(pos);
            return state.getBlock().isAir(state, this, pos);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        if (data.containsBlock(pos)) {
            return data.getCombinedLight(pos, lightValue);
        }
        return 15 << 20;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (!data.containsBlock(pos)) {
            return _default;
        }
        return getBlockState(pos).isSideSolid(this, pos, side);
    }

    @Override
    @Nonnull
    public Biome getBiome(BlockPos pos) {
        return data.getBiomeServer(pos);
    }

    @Override
    public boolean isValid(BlockPos pos) {
        return data.containsBlock(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        return data.containsBlock(pos);
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        return false;
    }

    private static class ChunkProvider implements IChunkProvider {
        @Nullable
        @Override
        public Chunk getLoadedChunk(int x, int z) {
            return null;
        }

        @Override
        public Chunk provideChunk(int x, int z) {
            return null;
        }

        @Override
        public boolean tick() {
            return false;
        }

        @Override
        public String makeString() {
            return ChunkProvider.class.getSimpleName();
        }

        @Override
        public boolean isChunkGeneratedAt(int x, int z) {
            return false;
        }
    }

    private static class SaveHandler implements ISaveHandler {
        @Nullable
        @Override
        public WorldInfo loadWorldInfo() {
            return null;
        }

        @Override
        public void checkSessionLock() {
        }

        @Override
        public IChunkLoader getChunkLoader(WorldProvider provider) {
            return null;
        }

        @Override
        public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
        }

        @Override
        public void saveWorldInfo(WorldInfo worldInformation) {
        }

        @Override
        public IPlayerFileData getPlayerNBTManager() {
            return null;
        }

        @Override
        public void flush() {
        }

        @Override
        public File getWorldDirectory() {
            return null;
        }

        @Override
        public File getMapFileFromName(String mapName) {
            return null;
        }

        @Override
        public TemplateManager getStructureTemplateManager() {
            return null;
        }
    }
}
