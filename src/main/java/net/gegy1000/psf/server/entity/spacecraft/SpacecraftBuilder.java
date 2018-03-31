package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.modules.ModuleThruster;
import net.gegy1000.psf.server.util.MaterialMass;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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
                this.setTileEntity(localPos, entity);
            }
        }
    }

    public SpacecraftBlockAccess buildBlockAccess(Entity e) {
        World world = e.getEntityWorld();
        BlockPos origin = e.getPosition();

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
            pos = origin.add(pos);
            lightData[SpacecraftBlockAccess.getPosIndex(pos, minPos, maxPos)] = world.getCombinedLight(pos, 0);
        }

        Biome biome = world.getBiome(origin);

        return new SpacecraftBlockAccess(world, blockData, lightData, this.entities, biome, minPos, maxPos);
    }

    public LauncherMetadata buildMetadata() {
        double mass = 0.0;
        ImmutableList.Builder<LauncherMetadata.Thruster> thrusters = ImmutableList.builder();

        for (int i = 0; i < this.blockKeys.size(); i++) {
            long posKey = this.blockKeys.getLong(i);
            BlockPos pos = BlockPos.fromLong(posKey);
            IBlockState state = Block.getStateById(this.blockValues.getInt(i));

            mass += MaterialMass.getMass(state);

            TileEntity entity = this.entities.get(posKey);
            if (entity instanceof TileModule) {
                IModule module = ((TileModule) entity).getModule();
                if (module instanceof ModuleThruster) {
                    ModuleThruster.ThrusterTier tier = ((ModuleThruster) module).getTier();
                    thrusters.add(new LauncherMetadata.Thruster(pos, tier.getThrust()));
                }
            }
        }

        return new LauncherMetadata(thrusters.build(), mass);
    }
}
