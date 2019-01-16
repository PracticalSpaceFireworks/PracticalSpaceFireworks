package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
    private static final ImmutableMap<EnumFacing, PropertyBool> SIDE_PROPS = Arrays.stream(EnumFacing.VALUES)
            .collect(Maps.toImmutableEnumMap(Function.identity(), f -> PropertyBool.create(f.getName())));

    public BlockStrutFixed(String name) {
        super(name);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        BlockStateContainer.Builder builder = new BlockStateContainer.Builder(this);
        for (PropertyBool prop : SIDE_PROPS.values()) {
            builder.add(prop);
        }
        return builder.add(DIRECTION).build();
    }

    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        for (EnumFacing side : EnumFacing.VALUES) {
            PropertyBool property = SIDE_PROPS.get(side);
            BlockPos offset = pos.offset(side);
            IBlockState target = world.getBlockState(offset);
            state = state.withProperty(property, target.getBlock() != this);
        }
        return state;
    }
}
