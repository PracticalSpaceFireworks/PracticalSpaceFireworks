package net.gegy1000.psf.server.block.module;

import lombok.val;
import lombok.var;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockMultiblockModule extends BlockModule {
    public static final IProperty<Boolean> DUMMY = PropertyBool.create("dummy");

    public BlockMultiblockModule(Material mat, String module) {
        super(mat, module);
        setDefaultState(getDefaultState().withProperty(DUMMY, false));
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta & 7).withProperty(DUMMY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (super.getMetaFromState(state) & 7) | ((state.getValue(DUMMY) ? 1 : 0) << 3);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION, DUMMY);
    }

    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return state.getValue(DUMMY) ? new TileDummyModule() : super.createTileEntity(world, state);
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        for (val offset : getDummyPositions(getDefaultState().withProperty(DIRECTION, side), pos)) {
            if (!world.getBlockState(offset).getBlock().isReplaceable(world, offset)) {
                return false;
            }
        }
        return super.canAttachOnSide(world, pos, state, on, side);
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        if (super.isStructuralModule(connecting, state)) {
            return true;
        }
        if (connecting != null && this == connecting.getBlock()) {
            return connecting.getValue(DUMMY);
        }
        return false;
    }

    protected Iterable<BlockPos> getDummyPositions(IBlockState state, BlockPos master) {
        val positions = new HashSet<BlockPos>();
        val dir = state.getValue(DIRECTION);
        for (var i = 1; i < getHeight(); i++) {
            positions.add(master.offset(dir, i));
        }
        return positions;
    }

    protected int getHeight() {
        return 3;
    }

    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return state.getValue(DUMMY) ? EnumBlockRenderType.ENTITYBLOCK_ANIMATED : super.getRenderType(state);
    }

    @Override
    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        if (state.getValue(DUMMY)) {
            @Nullable val te = world.getTileEntity(pos);
            if (te instanceof TileDummyModule) {
                pos = ((TileDummyModule) te).getMaster();
                state = world.getBlockState(pos);
            }
        }
        var aabb = super.getSelectedBoundingBox(state, world, pos);
        if (this == state.getBlock()) {
            for (val offset : getDummyPositions(state, pos)) {
                aabb = aabb.union(super.getSelectedBoundingBox(state, world, offset));
            }
        }
        return aabb;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!state.getValue(DUMMY)) {
            val dummyState = state.withProperty(DUMMY, true);
            for (val dummyPos : getDummyPositions(state, pos)) {
                world.setBlockState(dummyPos, dummyState);
                @Nullable val te = world.getTileEntity(dummyPos);
                if (te instanceof TileDummyModule) {
                    ((TileDummyModule) te).setMaster(pos);
                }
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(DUMMY)) {
            @Nullable val te = world.getTileEntity(pos);
            if (te instanceof TileDummyModule) {
                pos = ((TileDummyModule) te).getMaster();
            }
        }
        val air = Blocks.AIR.getDefaultState();
        val flags = CONVERTING.get() ? 10 : 3;
        for (val dummyPos : getDummyPositions(state, pos)) {
            world.setBlockState(dummyPos, air, flags);
        }
        world.setBlockState(pos, air, flags);
        super.breakBlock(world, pos, state);
    }
}
