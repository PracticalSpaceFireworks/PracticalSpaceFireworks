package net.gegy1000.psf.server.block.module;

import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockSolarPanel extends BlockModule {
    
    private static final AxisDirectionalBB BOUNDING_BOX = AxisDirectionalBB.of(0, 0, 14 / 16D, 1, 1, 1);

    public BlockSolarPanel() {
        super(Material.IRON, "solar_panel_small");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX.withFacing(state.getValue(DIRECTION));
    }
}
