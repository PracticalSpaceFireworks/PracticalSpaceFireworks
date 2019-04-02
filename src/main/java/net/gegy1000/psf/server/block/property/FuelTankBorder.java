package net.gegy1000.psf.server.block.property;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.Locale;

@RequiredArgsConstructor
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public enum FuelTankBorder implements IStringSerializable {
    SOUTH(EnumFacing.SOUTH, null),
    SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST),
    WEST(EnumFacing.WEST, null),
    NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
    NORTH(EnumFacing.NORTH, null),
    NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
    EAST(EnumFacing.EAST, null),
    SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST);

    public static final ImmutableCollection<FuelTankBorder> BORDERS =
        Sets.immutableEnumSet(EnumSet.allOf(FuelTankBorder.class));

    public static final ImmutableCollection<FuelTankBorder> CARDINALS =
        BORDERS.stream().filter(FuelTankBorder::isCardinal).collect(Sets.toImmutableEnumSet());

    public static final ImmutableCollection<FuelTankBorder> ORDINALS =
        BORDERS.stream().filter(FuelTankBorder::isOrdinal).collect(Sets.toImmutableEnumSet());

    @Nonnull private final EnumFacing primary;
    @Nullable private final EnumFacing secondary;

    public static FuelTankBorder forDirection(EnumFacing dir) {
        for (val border : CARDINALS) {
            if (dir == border.primary) {
                return border;
            }
        }
        throw new Error(String.valueOf(dir));
    }

    public final FuelTankBorder primary() {
        switch (this) {
            case SOUTH: return SOUTH;
            case SOUTH_WEST: return SOUTH;
            case WEST: return WEST;
            case NORTH_WEST: return NORTH;
            case NORTH: return NORTH;
            case NORTH_EAST: return NORTH;
            case EAST: return EAST;
            case SOUTH_EAST: return SOUTH;
        }
        throw new Error(toString());
    }

    public final FuelTankBorder secondary() {
        switch (this) {
            case SOUTH_WEST: return WEST;
            case NORTH_WEST: return WEST;
            case NORTH_EAST: return EAST;
            case SOUTH_EAST: return EAST;
        }
        throw new Error(toString());
    }

    public final boolean isCardinal() {
        return secondary == null;
    }

    public final boolean isOrdinal() {
        return secondary != null;
    }

    public final BlockPos offset(BlockPos origin) {
        val offset = origin.offset(primary);
        if (secondary != null) {
            return offset.offset(secondary);
        }
        return offset;
    }

    @Override
    public final String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + '.' + name();
    }
}
