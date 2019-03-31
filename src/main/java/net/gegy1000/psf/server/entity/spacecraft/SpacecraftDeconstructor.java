package net.gegy1000.psf.server.entity.spacecraft;

import javax.vecmath.Point3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IFixedSizeWorldData;
import net.gegy1000.psf.server.util.Matrix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpacecraftDeconstructor {
    public static Optional<Result> deconstruct(World world, IFixedSizeWorldData bodyData, double posX, double posY, double posZ, Matrix rotationMatrix) {
        posY = Math.round(posY);

        Map<BlockPos, IBlockState> blocks = new HashMap<>();
        Map<BlockPos, TileEntity> entities = new HashMap<>();
        for (BlockPos pos : BlockPos.getAllInBoxMutable(bodyData.getMinPos(), bodyData.getMaxPos())) {
            IBlockState state = bodyData.getBlockState(pos);
            if (state.getBlock() != Blocks.AIR) {
                Point3d point = new Point3d(pos.getX(), pos.getY(), pos.getZ());
                rotationMatrix.transform(point);
                BlockPos transformedPos = new BlockPos(point.getX() + posX, point.getY() + posY, point.getZ() + posZ);

                IBlockState worldState = world.getBlockState(transformedPos);
                if (!worldState.getBlock().isReplaceable(world, transformedPos)) {
                    return Optional.empty();
                }

                blocks.put(transformedPos, state);

                TileEntity entity = bodyData.getTileEntity(pos);
                if (entity != null) {
                    NBTTagCompound tag = entity.serializeNBT();
                    // TE must get the proper position during readFromNBT
                    tag.setInteger("x", transformedPos.getX());
                    tag.setInteger("y", transformedPos.getY());
                    tag.setInteger("z", transformedPos.getZ());
                    TileEntity copiedEntity = TileEntity.create(world, tag);
                    if (copiedEntity == null) {
                        PracticalSpaceFireworks.LOGGER.warn("Failed to copy TE when building spacecraft");
                        continue;
                    }
                    entities.put(transformedPos, copiedEntity);
                }
            }
        }

        return Optional.of(new Result(blocks, entities));
    }

    @RequiredArgsConstructor
    public static class Result {
        @Getter
        private final Map<BlockPos, IBlockState> blocks;
        @Getter
        private final Map<BlockPos, TileEntity> entities;
    }
}
