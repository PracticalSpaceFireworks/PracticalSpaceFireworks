package net.gegy1000.psf.server.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.function.UnaryOperator;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class AxisDirectionalBB extends AxisAlignedBB {
    private static final EnumFacing[] DIRECTIONS = EnumFacing.values();
    private static final UnaryOperator<EnumFacing> IDENTITY = dir -> dir;

    private final ImmutableMap<EnumFacing, AxisAlignedBB> boxes = Arrays.stream(DIRECTIONS)
        .collect(Maps.toImmutableEnumMap(IDENTITY, this::computeForDirection));

    public AxisDirectionalBB(AxisAlignedBB aabb) {
        this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    public AxisDirectionalBB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AxisAlignedBB withDirection(EnumFacing dir) {
        return boxes.get(dir);
    }

    private AxisAlignedBB computeForDirection(EnumFacing dir) {
        switch (dir) {
            case DOWN: return new AxisAlignedBB(1 - maxX, minZ, 1 - maxY, 1 - minX, maxZ, 1 - minY);
            case UP: return new AxisAlignedBB(minX, 1 - maxZ, minY, maxX, 1 - minZ, maxY);
            case SOUTH: return new AxisAlignedBB(1 - maxX, minY, 1 - maxZ, 1 - minX, maxY, 1 - minZ);
            case WEST: return new AxisAlignedBB(minZ, minY, 1 - maxX, maxZ, maxY, 1 - minX);
            case EAST: return new AxisAlignedBB(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX);
            default: return this;
        }
    }
}
