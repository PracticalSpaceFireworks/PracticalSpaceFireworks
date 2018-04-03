package net.gegy1000.psf.server.entity.spacecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.util.Matrix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Point3d;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpacecraftDeconstructor {
    public static Optional<Result> deconstruct(World world, SpacecraftBlockAccess blockAccess, double posX, double posY, double posZ, Matrix rotationMatrix) {
        Map<BlockPos, IBlockState> blocks = new HashMap<>();
        Map<BlockPos, TileEntity> entities = new HashMap<>();
        for (BlockPos pos : BlockPos.getAllInBoxMutable(blockAccess.getMinPos(), blockAccess.getMaxPos())) {
            IBlockState state = blockAccess.getBlockState(pos);
            if (state.getBlock() != Blocks.AIR) {
                Point3d point = new Point3d(pos.getX(), pos.getY(), pos.getZ());
                rotationMatrix.transform(point);
                BlockPos transformedPos = new BlockPos(point.getX() + posX, point.getY() + posY, point.getZ() + posZ);

                IBlockState worldState = world.getBlockState(transformedPos);
                if (!worldState.getBlock().isReplaceable(world, transformedPos)) {
                    return Optional.empty();
                }

                blocks.put(transformedPos, state);

                TileEntity entity = blockAccess.getTileEntity(pos);
                if (entity != null) {
                    TileEntity copiedEntity = TileEntity.create(world, entity.serializeNBT());
                    if (copiedEntity == null) {
                        PracticalSpaceFireworks.LOGGER.warn("Failed to copy TE when building spacecraft");
                        continue;
                    }
                    copiedEntity.setPos(transformedPos);
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
