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
        int count = 0;
        if (this.canConnect(blockAccess, pos, EnumFacing.NORTH)) count++;
        if (this.canConnect(blockAccess, pos, EnumFacing.SOUTH)) count++;
        if (this.canConnect(blockAccess, pos, EnumFacing.WEST)) count++;
        if (this.canConnect(blockAccess, pos, EnumFacing.EAST)) count++;
        return count == 2 || super.shouldSideBeRendered(state, blockAccess, pos, side);
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
        return BlockModule.isStructuralModule(world.getBlockState(pos.offset(side)));
    }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(DIRECTION, EnumFacing.UP);
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
    public boolean isStructuralModule() {
        return true;
    }
}
