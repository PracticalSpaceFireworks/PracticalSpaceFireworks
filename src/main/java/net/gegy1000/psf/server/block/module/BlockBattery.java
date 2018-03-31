package net.gegy1000.psf.server.block.module;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockBattery extends BlockModule {
    private static final AxisAlignedBB DOWN_BOUNDS = new AxisAlignedBB(0.25, 0.0, 0.125, 0.75, 0.09375, 0.875);
    private static final AxisAlignedBB UP_BOUNDS = new AxisAlignedBB(0.25, 0.90625, 0.125, 0.75, 1.0, 0.875);
    private static final AxisAlignedBB SOUTH_BOUNDS = new AxisAlignedBB(0.25, 0.125, 0.09375, 0.75, 0.875, 0.0);
    private static final AxisAlignedBB NORTH_BOUNDS = new AxisAlignedBB(0.25, 0.125, 0.90625, 0.75, 0.875, 1.0);
    private static final AxisAlignedBB EAST_BOUNDS = new AxisAlignedBB(0.09375, 0.125, 0.25, 0.0, 0.875, 0.75);
    private static final AxisAlignedBB WEST_BOUNDS = new AxisAlignedBB(0.90625, 0.125, 0.25, 1.0, 0.875, 0.75);

    public BlockBattery(String name) {
        super(Material.CIRCUITS, name);
        this.setSoundType(SoundType.METAL);
        this.setHardness(1.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(DIRECTION);
        switch (facing) {
            case DOWN:
                return DOWN_BOUNDS;
            case NORTH:
                return NORTH_BOUNDS;
            case SOUTH:
                return SOUTH_BOUNDS;
            case WEST:
                return WEST_BOUNDS;
            case EAST:
                return EAST_BOUNDS;
            case UP:
            default:
                return UP_BOUNDS;
        }
    }
}
