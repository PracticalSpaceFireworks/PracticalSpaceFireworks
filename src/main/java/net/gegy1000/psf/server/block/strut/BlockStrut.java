package net.gegy1000.psf.server.block.strut;

import javax.annotation.Nonnull;

import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;

public class BlockStrut extends Block implements RegisterItemBlock {
    
    private static final @Nonnull IProperty<StrutType> TYPE = PropertyEnum.create("type", StrutType.class);

    public BlockStrut() {
        super(Material.IRON);
        setSoundType(SoundType.METAL);
        setHardness(2.0f);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }
    
    @Override
    protected @Nonnull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE);
    }
    
    @Override
    public @Nonnull BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }
    
    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
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
