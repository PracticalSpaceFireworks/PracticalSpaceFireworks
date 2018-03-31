package net.gegy1000.psf.server.block.module;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStrut extends BlockModule {
    
    private static final @Nonnull IProperty<StrutType> TYPE = PropertyEnum.create("type", StrutType.class);

    public BlockStrut() {
        super(Material.IRON, "strut");
        setSoundType(SoundType.METAL);
        setHardness(2.0f);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }
    
    @Override
    protected @Nonnull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION, TYPE);
    }
    
    @Override
    public @Nonnull BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    @Override
    public boolean shouldSideBeRendered(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return blockAccess.getBlockState(pos.offset(side)).getBlock() != this && super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
    
    @Override
    public boolean canPlaceBlockOnSide(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return canPlaceBlockAt(worldIn, pos);
    }
    
    @Override
    public @Nonnull IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
            @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(DIRECTION, EnumFacing.UP);
    }
    
    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(TYPE).ordinal();
    }
    
    @Override
    public @Nonnull IBlockState getStateFromMeta(int meta) {
        meta = Math.abs(meta) % StrutType.values().length;
        return getDefaultState().withProperty(TYPE, StrutType.values()[meta]);
    }
}
