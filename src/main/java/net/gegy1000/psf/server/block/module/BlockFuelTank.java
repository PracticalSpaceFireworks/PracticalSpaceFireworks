package net.gegy1000.psf.server.block.module;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockFuelTank extends BlockModule {
    private static final PropertyBool NORTH = PropertyBool.create("north");
    private static final PropertyBool SOUTH = PropertyBool.create("south");
    private static final PropertyBool EAST = PropertyBool.create("east");
    private static final PropertyBool WEST = PropertyBool.create("west");

    public BlockFuelTank() {
        super(Material.IRON, "fuel_tank");
        this.setSoundType(SoundType.METAL);
        this.setHardness(3.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
        this.setDefaultState(this.blockState.getBaseState().withProperty(DIRECTION, EnumFacing.UP)
                .withProperty(NORTH, false).withProperty(SOUTH, false)
                .withProperty(EAST, false).withProperty(WEST, false));
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION, NORTH, SOUTH, WEST, EAST);
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, EnumFacing side) {
        return !canConnect(blockAccess, pos, side) && super.shouldSideBeRendered(state, blockAccess, pos, side);
    }

    @Override
    @Nonnull
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(NORTH, this.canConnect(world, pos, EnumFacing.NORTH))
                .withProperty(SOUTH, this.canConnect(world, pos, EnumFacing.SOUTH))
                .withProperty(EAST, this.canConnect(world, pos, EnumFacing.EAST))
                .withProperty(WEST, this.canConnect(world, pos, EnumFacing.WEST));
    }

    private boolean canConnect(IBlockAccess world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos.offset(side));
        return state.getBlock() == this || BlockModule.isStructural(state);
    }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(DIRECTION, EnumFacing.UP);
    }
    
    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        updateNeighbors(worldIn, pos);
    }
    
    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        updateNeighbors(worldIn, pos);
    }

    private void updateNeighbors(World worldIn, BlockPos pos) {
        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
            if (canConnect(worldIn, pos, dir)) {
                BlockPos pos2 = pos.offset(dir);
                worldIn.notifyNeighborsOfStateChange(pos2, worldIn.getBlockState(pos2).getBlock(), true);
            }
        }
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return 0;
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public boolean isStructuralModule(IBlockState state) {
        boolean n = state.getValue(NORTH);
        boolean s = state.getValue(SOUTH);
        boolean w = state.getValue(WEST);
        boolean e = state.getValue(EAST);
        if (n && e) {
            return w || s;
        } else if (n && w) {
            return e || s;
        } else if (s && e) {
            return n || w;
        } else if (s && w) {
            return n || e;
        }
        return true;
    }
}
