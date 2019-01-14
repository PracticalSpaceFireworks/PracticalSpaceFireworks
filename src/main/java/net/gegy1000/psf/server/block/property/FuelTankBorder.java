package net.gegy1000.psf.server.block.property;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Locale;

@RequiredArgsConstructor
public enum FuelTankBorder implements IStringSerializable {
    SOUTH(EnumFacing.SOUTH),
    SOUTH_WEST(EnumFacing.SOUTH, EnumFacing.WEST),
    WEST(EnumFacing.WEST),
    NORTH_WEST(EnumFacing.NORTH, EnumFacing.WEST),
    NORTH(EnumFacing.NORTH),
    NORTH_EAST(EnumFacing.NORTH, EnumFacing.EAST),
    EAST(EnumFacing.EAST),
    SOUTH_EAST(EnumFacing.SOUTH, EnumFacing.EAST);

    private static final FuelTankBorder[] VALUES = values();

    public static final ImmutableCollection<FuelTankBorder> BORDERS =
            Sets.immutableEnumSet(EnumSet.allOf(FuelTankBorder.class));

    public static final ImmutableCollection<FuelTankBorder> CARDINALS = BORDERS.stream()
            .filter(FuelTankBorder::isCardinal)
            .collect(Sets.toImmutableEnumSet());

    public static final ImmutableCollection<FuelTankBorder> ORDINALS = BORDERS.stream()
            .filter(FuelTankBorder::isOrdinal)
            .collect(Sets.toImmutableEnumSet());

    private final EnumFacing primary;

    @Nullable
    private final EnumFacing secondary;

    FuelTankBorder(EnumFacing primary) {
        this(primary, null);
    }

    public static FuelTankBorder valueOf(int ordinal) {
        return VALUES[ordinal % VALUES.length];
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
        throw new UnsupportedOperationException(toString());
    }

    public final FuelTankBorder secondary() {
        switch (this) {
            case SOUTH_WEST: return WEST;
            case NORTH_WEST: return WEST;
            case NORTH_EAST: return EAST;
            case SOUTH_EAST: return EAST;
        }
        throw new UnsupportedOperationException(toString());
    }

    public final EnumFacing primaryDirection() {
        return this.primary;
    }

    public final EnumFacing secondaryDirection() {
        if (this.secondary == null) {
            throw new UnsupportedOperationException(toString());
        }
        return this.secondary;
    }

    public final boolean isCardinal() {
        return this.secondary == null;
    }

    public final boolean isOrdinal() {
        return this.secondary != null;
    }

    public final BlockPos offset(BlockPos origin) {
        BlockPos offset = origin.offset(primary);
        if (secondary == null) return offset;
        return offset.offset(secondary);
    }

    @Override
    public final String getName() {
        return toString();
    }

    @Override
    public final String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
