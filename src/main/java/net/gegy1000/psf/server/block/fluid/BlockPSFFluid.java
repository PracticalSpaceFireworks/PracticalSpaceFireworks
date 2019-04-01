package net.gegy1000.psf.server.block.fluid;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockPSFFluid extends BlockFluidFinite {
    public BlockPSFFluid(Fluid fluid, Material material) {
        super(fluid, material);
    }
    
    @Override
    public String getLocalizedName() {
        return definedFluid.getLocalizedName(null);
    }
}
