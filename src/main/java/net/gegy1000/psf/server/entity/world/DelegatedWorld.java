package net.gegy1000.psf.server.entity.world;

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
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;

@ParametersAreNonnullByDefault
public class DelegatedWorld extends World {
    private final World parent;
    private final Handler handler;

    public DelegatedWorld(World parent, Handler handler) {
        super(new SaveHandler(), parent.getWorldInfo(), parent.provider, parent.profiler, parent.isRemote);
        this.parent = parent;
        this.handler = handler;
        this.handler.setParent(this);
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
        return new EmptyChunk(this, chunkX, chunkZ);
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return !handler.containsBlock(pos);
    }

    @Override
    @Nonnull
    public IBlockState getBlockState(BlockPos pos) {
        if (handler.containsBlock(pos)) {
            return handler.getBlockState(pos);
        }
        return Blocks.AIR.getDefaultState();
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        if (handler.containsBlock(pos)) {
            return handler.getTileEntity(pos);
        }
        return null;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        if (handler.containsBlock(pos)) {
            handler.setBlockState(pos, state);
            return true;
        }
        return false;
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity entity) {
        if (handler.containsBlock(pos)) {
            handler.setTileEntity(pos, entity);
        }
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        if (handler.containsBlock(pos)) {
            IBlockState state = handler.getBlockState(pos);
            return state.getBlock().isAir(state, this, pos);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        if (handler.containsBlock(pos)) {
            return handler.getLight(pos);
        }
        return 15 << 20;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (!handler.containsBlock(pos)) {
            return _default;
        }
        return getBlockState(pos).isSideSolid(this, pos, side);
    }

    @Override
    @Nonnull
    public Biome getBiome(BlockPos pos) {
        return handler.getBiome(pos);
    }

    @Override
    public boolean isValid(BlockPos pos) {
        return handler.containsBlock(pos);
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        return handler.containsBlock(pos);
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        return false;
    }

    public interface Handler {
        void setParent(DelegatedWorld world);

        void setBlockState(BlockPos pos, IBlockState state);

        void setTileEntity(BlockPos pos, @Nullable TileEntity entity);

        @Nonnull
        IBlockState getBlockState(BlockPos pos);

        @Nullable
        TileEntity getTileEntity(BlockPos pos);

        int getLight(BlockPos pos);

        @Nonnull
        Biome getBiome(BlockPos pos);

        boolean containsBlock(BlockPos pos);
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
