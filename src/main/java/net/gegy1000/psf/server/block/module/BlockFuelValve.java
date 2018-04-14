package net.gegy1000.psf.server.block.module;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockFuelValve extends BlockModule {
    public BlockFuelValve() {
        super(Material.IRON, "fuel_valve");
        this.setSoundType(SoundType.METAL);
        this.setHardness(3.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, @Nonnull IBlockState state) {
        return true;
    }

    @Override
    protected boolean isDirectional(@Nonnull IBlockState state) {
        return false;
    }
}
