package net.gegy1000.psf.server.block;

import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;

public class BlockPSFFluid extends BlockFluidFinite {
    
    public BlockPSFFluid(Fluid fluid, Material material) {
        super(fluid, material);
    }
    
    @Override
    public String getLocalizedName() {
        return definedFluid.getLocalizedName(null);
    }
}
