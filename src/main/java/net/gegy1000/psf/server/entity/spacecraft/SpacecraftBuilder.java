package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

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

    public SpacecraftBlockAccess buildBlockAccess() {
        BlockPos minPos = new BlockPos(this.minX, this.minY, this.minZ);
        BlockPos maxPos = new BlockPos(this.maxX, this.maxY, this.maxZ);

        int[] blockData = new int[SpacecraftBlockAccess.getDataSize(minPos, maxPos)];

        for (int i = 0; i < this.blockKeys.size(); i++) {
            BlockPos pos = BlockPos.fromLong(this.blockKeys.getLong(i));
            int state = this.blockValues.getInt(i);

            blockData[SpacecraftBlockAccess.getPosIndex(pos, minPos, maxPos)] = state;
        }

        return new SpacecraftBlockAccess(blockData, minPos, maxPos);
    }

    public SpacecraftMetadata buildMetadata() {
        ImmutableList.Builder<SpacecraftMetadata.Thruster> thrusters = ImmutableList.builder();

        for (int i = 0; i < this.blockKeys.size(); i++) {
            BlockPos pos = BlockPos.fromLong(this.blockKeys.getLong(i));
            IBlockState state = Block.getStateById(this.blockValues.getInt(i));

            // TODO: Thrusters can be defined in a better way
            if (state.getBlock() instanceof BlockFence) {
                thrusters.add(new SpacecraftMetadata.Thruster(pos));
            }
        }

        return new SpacecraftMetadata(thrusters.build());
    }
}
