package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockAirCompressor extends Block implements RegisterItemModel, RegisterItemBlock, RegisterTileEntity {
    public BlockAirCompressor() {
        super(Material.IRON);
        this.setHarvestLevel("pickaxe", 1);
        this.setSoundType(SoundType.METAL);
        this.setHardness(2.0F);
        this.setResistance(3.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileAirCompressor();
    }

    @Override
    public Class<? extends TileEntity> getEntityClass() {
        return TileAirCompressor.class;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
