package net.gegy1000.psf.server.block.property;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;

@RequiredArgsConstructor
public enum FuelTankBorder implements IStringSerializable {
    SOUTH(EnumFacing.SOUTH, null),
    SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST),
    WEST(EnumFacing.WEST, null),
    NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
    NORTH(EnumFacing.NORTH, null),
    NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
    EAST(EnumFacing.EAST, null),
    SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST);

    private static final FuelTankBorder[] VALUES = values();

    public static final ImmutableCollection<FuelTankBorder> BORDERS =
        Arrays.stream(VALUES).collect(Sets.toImmutableEnumSet());

    public static final ImmutableCollection<FuelTankBorder> CARDINALS =
        Arrays.stream(VALUES).filter(FuelTankBorder::isCardinal).collect(Sets.toImmutableEnumSet());

    public static final ImmutableCollection<FuelTankBorder> ORDINALS =
        Arrays.stream(VALUES).filter(FuelTankBorder::isOrdinal).collect(Sets.toImmutableEnumSet());

    @Nonnull private final EnumFacing primary;
    @Nullable private final EnumFacing secondary;

    @Nonnull
    public static FuelTankBorder valueOf(int ordinal) {
        return VALUES[ordinal % VALUES.length];
    }

    public static FuelTankBorder forDirection(EnumFacing dir) {
        for (val border : CARDINALS) {
            if (dir == border.primary) {
                return border;
            }
        }
        throw new Error(String.valueOf(dir));
    }

    @Nonnull
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

    @Nonnull
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

    @Nonnull
    public final BlockPos offset(@Nonnull BlockPos origin) {
        val offset = origin.offset(primary);
        if (secondary != null) {
            return offset.offset(secondary);
        }
        return offset;
    }

    @Override
    @Nonnull
    public final String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    @Nonnull
    public final String toString() {
        return getClass().getSimpleName() + '.' + name();
    }
}
