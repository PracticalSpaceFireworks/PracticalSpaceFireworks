package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockStrutFixed extends BlockStrutAbstract {
    private static final ImmutableMap<EnumFacing, PropertyBool> SIDE_PROPS = Arrays.stream(EnumFacing.values())
        .collect(Maps.toImmutableEnumMap(Function.identity(), f -> PropertyBool.create(f.getName())));

    public BlockStrutFixed(String name) {
        super(name);
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        val builder = new BlockStateContainer.Builder(this);
        SIDE_PROPS.values().forEach(builder::add);
        return builder.add(DIRECTION).build();
    }

    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        for (val entry : SIDE_PROPS.entrySet()) {
            val property = entry.getValue();
            val offset = pos.offset(entry.getKey());
            val target = access.getBlockState(offset);
            state = state.withProperty(property, this != target.getBlock());
        }
        return state;
    }
}
