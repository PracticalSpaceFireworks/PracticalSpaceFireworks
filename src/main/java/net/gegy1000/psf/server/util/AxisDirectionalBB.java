package net.gegy1000.psf.server.util;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class AxisDirectionalBB extends AxisAlignedBB {
    private final Map<EnumFacing, AxisAlignedBB> aabbMap = Arrays.stream(EnumFacing.VALUES)
            .collect(Maps.toImmutableEnumMap(Function.identity(), facing -> {
                switch (facing) {
                    case DOWN: return new AxisAlignedBB(1 - maxX, minZ, 1 - maxY, 1 - minX, maxZ, 1 - minY);
                    case UP: return new AxisAlignedBB(minX, 1 - maxZ, minY, maxX, 1 - minZ, maxY);
                    case SOUTH: return new AxisAlignedBB(1 - maxX, minY, 1 - maxZ, 1 - minX, maxY, 1 - minZ);
                    case WEST: return new AxisAlignedBB(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX);
                    case EAST: return new AxisAlignedBB(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX);
                }
                return null;
            }));

    private AxisDirectionalBB(double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1, x2, y2, z2);
    }

    public static AxisDirectionalBB of(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AxisDirectionalBB(x1, y1, z1, x2, y2, z2);
    }

    public static AxisDirectionalBB of(Vec3d minVec, Vec3d maxVec) {
        return of(minVec.x, minVec.y, minVec.z, maxVec.x, maxVec.y, maxVec.z);
    }

    public static AxisDirectionalBB of(Vec3i minVec, Vec3i maxVec) {
        return of(minVec.getX(), minVec.getY(), minVec.getZ(), maxVec.getX(), maxVec.getY(), maxVec.getZ());
    }

    public static AxisDirectionalBB of(Vec3i vec) {
        return of(vec.getX(), vec.getY(), vec.getZ(), vec.getX() + 1, vec.getY() + 1, vec.getZ() + 1);
    }

    public static AxisDirectionalBB copyOf(AxisAlignedBB aabb) {
        return of(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    public AxisAlignedBB withFacing(@Nonnull EnumFacing facing) {
        if (facing == EnumFacing.NORTH) return this;
        return aabbMap.getOrDefault(facing, Block.FULL_BLOCK_AABB);
    }
}
