package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// todo matrix based computation for collision boxes? optimize them, pls
// todo placement orientation (getStateForPlacement)
// todo wrench orientation (withRotation/withMirror/rotateBlock)

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BlockStrutOrientable extends BlockStrutAbstract {
    private static final PropertyEnum<Orientation> ORIENTATION =
        PropertyEnum.create("orientation", Orientation.class, Orientation.ALL);

    public BlockStrutOrientable(String name) {
        super(name);
        setDefaultState(getDefaultState().withProperty(ORIENTATION, Orientation.SOUTH_WEST));
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
        return state.getValue(ORIENTATION).contains(side) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> boxes, @Nullable Entity entityIn, boolean isActualState) {
        for (val box : getCollisionBoxes(state.getValue(ORIENTATION), ForgeModContainer.fullBoundingBoxLadders)) {
            addCollisionBoxToList(pos, entityBox, boxes, box);
        }
    }

    protected abstract ImmutableSet<AxisAlignedBB> getCollisionBoxes(Orientation orientation, boolean full);

    @Override
    @Nullable
    @Deprecated
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        val hits = new HashSet<RayTraceResult>();
        val a = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        val b = end.subtract(pos.getX(), pos.getY(), pos.getZ());
        for (val box : getCollisionBoxes(state.getValue(ORIENTATION), true)) {
            @Nullable val hit = box.calculateIntercept(a, b);
            if (hit != null) {
                val vec = hit.hitVec.add(pos.getX(), pos.getY(), pos.getZ());
                hits.add(new RayTraceResult(vec, hit.sideHit, pos));
            }
        }
        RayTraceResult nearest = null;
        var sqrDis = 0.0;
        for (val hit : hits) {
            val newSqrDis = hit.hitVec.squareDistanceTo(end);
            if (newSqrDis > sqrDis) {
                nearest = hit;
                sqrDis = newSqrDis;
            }
        }
        return nearest;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION, ORIENTATION);
    }

    @Override // fixme
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        if (side.getAxis().isVertical()) {
            return getDefaultState().withProperty(ORIENTATION, Orientation.from(placer.getHorizontalFacing(), side.getOpposite()));
        }
        val offset = side.getAxis() == Axis.X ? hitZ : hitX;
        EnumFacing secondary;
        if (side.getAxisDirection() == AxisDirection.NEGATIVE) {
            secondary = (side.getAxis() == Axis.Z ? offset < 0.5 : offset >= 0.5) ? side.rotateY() : side.rotateYCCW();
        } else {
            secondary = (side.getAxis() == Axis.Z ? offset >= 0.5 : offset < 0.5) ? side.rotateY() : side.rotateYCCW();
        }
        return getDefaultState().withProperty(ORIENTATION, Orientation.from(side.getOpposite(), secondary.getOpposite()));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ORIENTATION).ordinal();
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ORIENTATION, Orientation.valueOf(meta));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Orientation implements IStringSerializable {
        NORTH_DOWN(EnumFacing.NORTH, EnumFacing.DOWN, Rotation.CLOCKWISE_90),
        NORTH_UP(EnumFacing.NORTH, EnumFacing.UP, Rotation.CLOCKWISE_90),
        NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST, Rotation.CLOCKWISE_90),
        NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST, Rotation.CLOCKWISE_180),
        SOUTH_DOWN(EnumFacing.SOUTH, EnumFacing.DOWN, Rotation.COUNTERCLOCKWISE_90),
        SOUTH_UP(EnumFacing.SOUTH, EnumFacing.UP, Rotation.COUNTERCLOCKWISE_90),
        SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST, Rotation.NONE),
        SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST, Rotation.COUNTERCLOCKWISE_90),
        WEST_DOWN(EnumFacing.WEST, EnumFacing.DOWN, Rotation.NONE),
        WEST_UP(EnumFacing.WEST, EnumFacing.UP, Rotation.NONE),
        EAST_DOWN(EnumFacing.EAST, EnumFacing.DOWN, Rotation.CLOCKWISE_180),
        EAST_UP(EnumFacing.EAST, EnumFacing.UP, Rotation.CLOCKWISE_180);

        private static final Orientation[] VALUES = values();

        private static final ImmutableSet<Orientation> ALL =
            Sets.immutableEnumSet(EnumSet.allOf(Orientation.class));

        private final EnumFacing primary;
        private final EnumFacing secondary;
        private final Rotation rotation;

        public static Orientation valueOf(int ordinal) {
            return VALUES[ordinal % VALUES.length];
        }

        public static Orientation from(EnumFacing primary, EnumFacing secondary) {
            for (val orientation : ALL) {
                if (primary == orientation.getPrimary() && secondary == orientation.getSecondary()) {
                    return orientation;
                }
                if (secondary == orientation.getPrimary() && primary == orientation.getSecondary()) {
                    return orientation;
                }
            }
            throw new IllegalArgumentException("[" + primary + ", " + secondary + "]");
        }

        public final boolean contains(EnumFacing facing) {
            return facing == getPrimary() || facing == getSecondary();
        }

        @Override
        public final String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final class Slope extends BlockStrutOrientable {
        private static final Map<Orientation, ImmutableSet<AxisAlignedBB>> FULL;
        private static final Map<Orientation, ImmutableSet<AxisAlignedBB>> INSET;

        static {
            val full = ImmutableMap.<Orientation, ImmutableSet<AxisAlignedBB>>builder();
            val inset = ImmutableMap.<Orientation, ImmutableSet<AxisAlignedBB>>builder();

            for (val orientation : Orientation.ALL) {
                val boxes = ImmutableSet.<AxisAlignedBB>builder();
                for (var i = 0; i <= 63; ++i) {
                    if (i < 4) continue;
                    if (i > 59) continue;
                    val rot = rotateBox(orientation, new AxisAlignedBB(
                        (i / 4.0) / 16.0, 0.0625, (i / 4.0) / 16.0, (i / 4.0 + 0.25) / 16.0, 0.9375, 0.9375)
                    );
                    val minY = orientation.getSecondary() != EnumFacing.UP ? 0.0 : rot.minY;
                    val maxY = orientation.getSecondary() != EnumFacing.DOWN ? 1.0 : rot.maxY;
                    boxes.add(new AxisAlignedBB(rot.minX, minY, rot.minZ, rot.maxX, maxY, rot.maxZ));
                }
                inset.put(orientation, boxes.build());
            }

            for (val orientation : Orientation.ALL) {
                val boxes = ImmutableSet.<AxisAlignedBB>builder();
                for (int i = 0; i <= 63; ++i) {
                    boxes.add(rotateBox(orientation, new AxisAlignedBB(
                        (i / 4.0) / 16.0, 0.0, (i / 4.0) / 16.0, (i / 4.0 + 0.25) / 16.0, 1.0, 1.0))
                    );
                }
                full.put(orientation, boxes.build());
            }

            FULL = full.build();
            INSET = inset.build();
        }

        public Slope(String name) {
            super(name);
        }

        // fixme
        private static AxisAlignedBB rotateBox(Orientation orientation, AxisAlignedBB box) {
            if (orientation.getSecondary().getAxis().isVertical()) {
                box = new AxisDirectionalBB(box).withDirection(orientation.getSecondary().getOpposite());
            }
            return new AxisDirectionalBB(box).withDirection(orientation.getRotation().rotate(
                orientation.getSecondary() == EnumFacing.UP ? EnumFacing.SOUTH : EnumFacing.NORTH
            ));
        }

        @Override
        protected ImmutableSet<AxisAlignedBB> getCollisionBoxes(Orientation orientation, boolean full) {
            return (full ? FULL : INSET).getOrDefault(orientation, ImmutableSet.of());
        }
    }
}
