package net.gegy1000.psf.server.block;

import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;

public class BlockPSFFluid extends BlockFluidFinite implements RegisterItemBlock, RegisterItemModel {
    public BlockPSFFluid(Fluid fluid, Material material) {
        super(fluid, material);
    }
}
