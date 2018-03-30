package net.gegy1000.psf.server.block.module;

import javax.annotation.Nonnull;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.modules.ModuleBattery;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class BlockBattery extends BlockModule implements RegisterItemBlock {

    public BlockBattery() {
        super(Material.IRON);
    }

    @Override
    protected IModule createModule(@Nonnull World world, @Nonnull IBlockState state) {
        return new ModuleBattery(100000);
    }

}
