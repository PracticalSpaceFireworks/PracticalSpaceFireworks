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
    private static final AxisDirectionalBB BOUNDING_BOX =
        AxisDirectionalBB.of(0.1875, 0.25, 0.75, 0.8125, 0.75, 1.0);

    public BlockBattery(String name) {
        super(Material.CIRCUITS, name);
        setSoundType(SoundType.METAL);
        setHardness(1.0F);
        setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return BOUNDING_BOX.withFacing(state.getValue(DIRECTION));
    }
}
