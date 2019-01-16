package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

// todo rotate and manage bounding boxes (getSouthWestBoxes -> addCollisionBoxToList)
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

    protected abstract Set<AxisAlignedBB> getSouthWestBoxes();

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ORIENTATION).ordinal();
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
            return getDefaultState().withProperty(ORIENTATION, Orientation.from(placer.getHorizontalFacing(), side));
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
        NORTH_DOWN(EnumFacing.NORTH, EnumFacing.DOWN),
        NORTH_UP(EnumFacing.NORTH, EnumFacing.UP),
        NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
        NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
        SOUTH_DOWN(EnumFacing.SOUTH, EnumFacing.DOWN),
        SOUTH_UP(EnumFacing.SOUTH, EnumFacing.UP),
        SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST),
        SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST),
        WEST_DOWN(EnumFacing.WEST, EnumFacing.DOWN),
        WEST_UP(EnumFacing.WEST, EnumFacing.UP),
        EAST_DOWN(EnumFacing.EAST, EnumFacing.DOWN),
        EAST_UP(EnumFacing.EAST, EnumFacing.UP);

        private static final Orientation[] VALUES = values();
        private static final ImmutableSet<Orientation> ALL = Sets.immutableEnumSet(EnumSet.allOf(Orientation.class));

        private final EnumFacing primary;
        private final EnumFacing secondary;

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

        @Override
        public final String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final class Slope extends BlockStrutOrientable {
        public Slope(String name) {
            super(name);
        }

        @Override
        protected Set<AxisAlignedBB> getSouthWestBoxes() {
            return ImmutableSet.of(FULL_BLOCK_AABB); // fixme
        }
    }
}
