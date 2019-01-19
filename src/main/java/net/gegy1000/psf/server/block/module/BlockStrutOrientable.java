package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.*;

// todo matrix based computation for collision boxes? optimize them, pls
// todo placement orientation (getStateForPlacement)
// todo wrench orientation (withRotation/withMirror/rotateBlock)

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BlockStrutOrientable extends BlockStrutAbstract {
    private static final PropertyEnum<Orientation> ORIENTATION = PropertyEnum.create("orientation", Orientation.class, Orientation.ALL);

    public BlockStrutOrientable(String name) {
        super(name);
        setDefaultState(getDefaultState().withProperty(ORIENTATION, Orientation.SOUTH_WEST));
    }

    protected abstract ImmutableSet<AxisAlignedBB> getCollisionBoxes();

    protected abstract ImmutableSet<AxisAlignedBB> getRayTracingBoxes();

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ORIENTATION).ordinal();
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> boxes, @Nullable Entity entityIn, boolean isActualState) {
        Orientation orientation = state.getValue(ORIENTATION);
        for (AxisAlignedBB box : getCollisionBoxes()) {
            AxisAlignedBB rot = orientation.rotateBox(box);
            double minY = orientation.getSecondary() != EnumFacing.UP ? 0.0 : rot.minY;
            double maxY = orientation.getSecondary() != EnumFacing.DOWN ? 1.0 : rot.maxY;
            addCollisionBoxToList(pos, entityBox, boxes, new AxisAlignedBB(rot.minX, minY, rot.minZ, rot.maxX, maxY, rot.maxZ));
        }
    }

    @Override
    @Deprecated
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos position, Vec3d start, Vec3d end) {
        Set<AxisAlignedBB> boxes = new HashSet<>();
        Orientation orientation = state.getValue(ORIENTATION);
        for (AxisAlignedBB box : getRayTracingBoxes()) {
            boxes.add(orientation.rotateBox(box));

        }
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        Set<RayTraceResult> hits = new HashSet<>();
        Vec3d a = start.subtract(x, y, z);
        Vec3d b = end.subtract(x, y, z);
        for (AxisAlignedBB box : boxes) {
            @Nullable RayTraceResult hit = box.calculateIntercept(a, b);
            if (hit != null) {
                Vec3d vec = hit.hitVec.add(x, y, z);
                hits.add(new RayTraceResult(vec, hit.sideHit, position));
            }
        }
        RayTraceResult ret = null;
        double sqrDis = 0.0D;
        for (RayTraceResult hit : hits) {
            double newSqrDis = hit.hitVec.squareDistanceTo(end);
            if (newSqrDis > sqrDis) {
                ret = hit;
                sqrDis = newSqrDis;
            }
        }
        return ret;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return state.getValue(ORIENTATION).contains(side) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ORIENTATION, Orientation.valueOf(meta));
    }

    @Override // fixme
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        if (side.getAxis().isVertical()) {
            return getDefaultState().withProperty(ORIENTATION, Orientation.from(placer.getHorizontalFacing(), side.getOpposite()));
        } else {
            float offset = side.getAxis() == Axis.X ? hitZ : hitX;
            EnumFacing secondary;
            if (side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                secondary = (side.getAxis() == Axis.Z ? offset < 0.5 : offset >= 0.5) ? side.rotateY() : side.rotateYCCW();
            } else {
                secondary = (side.getAxis() == Axis.Z ? offset >= 0.5 : offset < 0.5) ? side.rotateY() : side.rotateYCCW();
            }
            return getDefaultState().withProperty(ORIENTATION, Orientation.from(side.getOpposite(), secondary.getOpposite()));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION, ORIENTATION);
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
        private static final ImmutableSet<Orientation> ALL = Sets.immutableEnumSet(EnumSet.allOf(Orientation.class));

        private final EnumFacing primary;
        private final EnumFacing secondary;
        private final Rotation rotation;

        public static Orientation valueOf(int ordinal) {
            return VALUES[ordinal % VALUES.length];
        }

        public static Orientation from(EnumFacing primary, EnumFacing secondary) {
            for (Orientation orientation : ALL) {
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

        // fixme this is awful
        public final AxisAlignedBB rotateBox(AxisAlignedBB box) {
            if (getSecondary().getAxis().isVertical()) {
                box = AxisDirectionalBB.copyOf(box).withFacing(getSecondary().getOpposite());
            }
            if (getSecondary() == EnumFacing.UP) {
                return AxisDirectionalBB.copyOf(box).withFacing(rotation.rotate(EnumFacing.SOUTH));
            }
            return AxisDirectionalBB.copyOf(box).withFacing(rotation.rotate(EnumFacing.NORTH));
        }

        @Override
        public final String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final class Slope extends BlockStrutOrientable {
        public Slope(String name) {
            super(name);
        }

        @Override // fixme this is awful
        protected ImmutableSet<AxisAlignedBB> getCollisionBoxes() {
            boolean full = ForgeModContainer.fullBoundingBoxLadders;
            ImmutableSet.Builder<AxisAlignedBB> boxes = ImmutableSet.builder();
            for (int i = 0; i <= 63; ++i) {
                if (i < 4 && !full) continue;
                if (i > 59 && !full) continue;
                boxes.add(new AxisAlignedBB(
                        (i / 4.0D) / 16.0D,
                        !full ? 0.0625 : 0.0,
                        (i / 4.0D) / 16.0D,
                        (i / 4.0D + 0.25D) / 16.0D,
                        !full ? 0.9375 : 1.0,
                        !full ? 0.9375 : 1.0
                ));
            }
            return boxes.build();
        }

        @Override // fixme this is awful
        protected ImmutableSet<AxisAlignedBB> getRayTracingBoxes() {
            ImmutableSet.Builder<AxisAlignedBB> boxes = ImmutableSet.builder();
            for (int i = 0; i <= 63; ++i) {
                boxes.add(new AxisAlignedBB(
                        (i / 4.0D) / 16.0D,
                        0.0,
                        (i / 4.0D) / 16.0D,
                        (i / 4.0D + 0.25D) / 16.0D,
                        1.0,
                        1.0
                ));
            }
            return boxes.build();
        }
    }
}
