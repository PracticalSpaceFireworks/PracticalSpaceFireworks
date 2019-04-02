package net.gegy1000.psf.server.block.property;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.Locale;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public enum Part implements IStringSerializable {
    /**
     * The blockstate has a matching block below but not above
     */
    BOTTOM,

    /**
     * The blockstate has a matching block above but not below
     */
    TOP,

    /**
     * The blockstate has a matching block above and below
     */
    NONE,

    /**
     * The blockstate has no matching block above or below
     */
    BOTH;

    public static final ImmutableSet<Part> PARTS = Sets.immutableEnumSet(EnumSet.allOf(Part.class));

    public static Part forPosition(IBlockAccess access, BlockPos pos, Block matching) {
        return forPosition(access, pos, matching::equals);
    }

    public static Part forPosition(IBlockAccess access, BlockPos pos, Predicate<Block> matcher) {
        if (matcher.test(access.getBlockState(pos.up()).getBlock())) {
            return matcher.test(access.getBlockState(pos.down()).getBlock()) ? Part.NONE : Part.BOTTOM;
        }
        return matcher.test(access.getBlockState(pos.down()).getBlock()) ? Part.TOP : Part.BOTH;
    }

    @Override
    public final String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "." + name();
    }
}
