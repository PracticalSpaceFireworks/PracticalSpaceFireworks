package net.gegy1000.psf.server.block.module;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockBattery extends BlockModule {
    private static final AxisDirectionalBB BOUNDING_BOX = AxisDirectionalBB.of(
            0.25D, 0.125D, 0.90625D, 0.75D, 0.875D, 1.0D
    );

    public BlockBattery(String name) {
        super(Material.CIRCUITS, name);
        this.setSoundType(SoundType.METAL);
        this.setHardness(1.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX.withFacing(state.getValue(DIRECTION));
    }
}
