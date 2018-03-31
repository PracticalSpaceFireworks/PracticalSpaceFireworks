package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.gegy1000.psf.server.block.PSFBlockRegistry;
import net.gegy1000.psf.server.util.MaterialMass;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class SpacecraftBuilder {
    private final LongList blockKeys = new LongArrayList();
    private final IntList blockValues = new IntArrayList();

    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    public void setBlockState(BlockPos pos, IBlockState state) {
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
    }

    public SpacecraftBlockAccess buildBlockAccess(Entity e) {
        BlockPos minPos = new BlockPos(this.minX, this.minY, this.minZ);
        BlockPos maxPos = new BlockPos(this.maxX, this.maxY, this.maxZ);

        int[] blockData = new int[SpacecraftBlockAccess.getDataSize(minPos, maxPos)];
        for (int i = 0; i < this.blockKeys.size(); i++) {
            BlockPos pos = BlockPos.fromLong(this.blockKeys.getLong(i));
            int state = this.blockValues.getInt(i);

            blockData[SpacecraftBlockAccess.getPosIndex(pos, minPos, maxPos)] = state;
        }

        World world = e.getEntityWorld();
        BlockPos origin = e.getPosition();

        Biome biome = world.getBiome(origin);

        int[] lightData = new int[SpacecraftBlockAccess.getDataSize(minPos, maxPos)];
        for (BlockPos pos : BlockPos.getAllInBoxMutable(minPos, maxPos)) {
            pos = origin.add(pos);
            lightData[SpacecraftBlockAccess.getPosIndex(pos, minPos, maxPos)] = world.getCombinedLight(pos, 0);
        }

        return new SpacecraftBlockAccess(blockData, lightData, biome, minPos, maxPos);
    }

    public SpacecraftMetadata buildMetadata() {
        double mass = 0.0;
        ImmutableList.Builder<SpacecraftMetadata.Thruster> thrusters = ImmutableList.builder();

        for (int i = 0; i < this.blockKeys.size(); i++) {
            BlockPos pos = BlockPos.fromLong(this.blockKeys.getLong(i));
            IBlockState state = Block.getStateById(this.blockValues.getInt(i));

            mass += MaterialMass.getMass(state);

            // TODO: Thrusters can be defined in a better way
            if (state.getBlock() == PSFBlockRegistry.thruster) {
                thrusters.add(new SpacecraftMetadata.Thruster(pos, 845000.0));
            }
        }

        return new SpacecraftMetadata(thrusters.build(), mass);
    }
}
