package net.gegy1000.psf.server.block.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMultiblockModule extends BlockModule {

    @Nonnull
    public static final IProperty<Boolean> DUMMY = PropertyBool.create("dummy");
    
    public BlockMultiblockModule(Material mat, @Nonnull String module) {
        super(mat, module);
        setDefaultState(getDefaultState().withProperty(DUMMY, false));
    }
    
    @Override
    protected @Nonnull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION, DUMMY);
    }
    
    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return (super.getMetaFromState(state) & 7) | ((state.getValue(DUMMY) ? 1 : 0) << 3);
    }
    
    @Override
    public @Nonnull IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(DUMMY, (meta & 8) > 0);
    }
    
    @Override
    @Deprecated
    public @Nonnull EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return state.getValue(DUMMY) ? EnumBlockRenderType.ENTITYBLOCK_ANIMATED : super.getRenderType(state);
    }
    
    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        return super.isStructuralModule(connecting, state) || (connecting != null && connecting.getBlock() == this && connecting.getValue(DUMMY));
    }
    
    protected int getHeight() {
        return 3;
    }

    protected Collection<BlockPos> getDummyPositions(IBlockState state, BlockPos master) {
        List<BlockPos> ret = new ArrayList<>();
        EnumFacing dir = state.getValue(DIRECTION);
        for (int i = 1; i < getHeight(); i++) {
            ret.add(master.offset(dir, i));
        }
        return ret;
    }
    
    @Override
    @Nullable
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        if (state.getValue(DUMMY)) {
            return new TileDummyModule();
        } else {
            return super.createTileEntity(world, state);
        }
    }
    
    @Override
    @Deprecated
    public @Nonnull AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (state.getValue(DUMMY) && te instanceof TileDummyModule) {
            pos = ((TileDummyModule)te).getMaster();
            te = worldIn.getTileEntity(pos);
            state = worldIn.getBlockState(pos);
        }
        // Can happen during load if crosses chunks, or if the multiblock is corrupted in some other way
        if (state.getBlock() != this) {
            return super.getSelectedBoundingBox(state, worldIn, pos);
        }
        AxisAlignedBB bb = new AxisAlignedBB(pos);
        for (BlockPos bp : getDummyPositions(state, pos)) {
            bb = bb.union(new AxisAlignedBB(bp));
        }
        return bb;
    }
    
    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, @Nonnull EnumFacing side) {
        for (BlockPos check : getDummyPositions(getDefaultState().withProperty(DIRECTION, side), pos)) {
            if (!world.getBlockState(check).getBlock().isReplaceable(world, check)) {
                return false;
            }
        }
        return super.canAttachOnSide(world, pos, state, on, side);
    }
    
    @Override
    public void onBlockAdded(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        
        if (!state.getValue(DUMMY)) {
            IBlockState dummystate = state.withProperty(DUMMY, true);
            for (BlockPos dummypos : getDummyPositions(state, pos)) {
                worldIn.setBlockState(dummypos, dummystate);
                TileEntity te = worldIn.getTileEntity(dummypos);
                if (te != null && te instanceof TileDummyModule) {
                    ((TileDummyModule) te).setMaster(pos);
                }
            }
        }
    }
    
    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if (state.getValue(DUMMY)) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te != null && te instanceof TileDummyModule) {
                pos = ((TileDummyModule) te).getMaster();
            }
        }
        for (BlockPos dummy : getDummyPositions(state, pos)) {
            worldIn.setBlockToAir(dummy);
        }
        worldIn.setBlockToAir(pos);
        super.breakBlock(worldIn, pos, state);
    }
}
