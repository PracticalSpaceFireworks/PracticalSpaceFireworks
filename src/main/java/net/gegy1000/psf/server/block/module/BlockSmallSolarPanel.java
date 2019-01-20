package net.gegy1000.psf.server.block.module;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.block.PSFSoundType;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockSmallSolarPanel extends BlockModule {
    private static final AxisDirectionalBB BOUNDING_BOX = AxisDirectionalBB.of(0.0, 0.0, 0.75, 1.0, 1.0, 1.0);

    public BlockSmallSolarPanel() {
        super(Material.GLASS, MapColor.LAPIS, "solar_panel_small");
        this.setSoundType(PSFSoundType.SOLAR_PANEL);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX.withFacing(state.getValue(DIRECTION));
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch (state.getValue(DIRECTION)) {
            case DOWN:
                return MapColor.SILVER;
            case UP:
                return MapColor.LAPIS;
            default:
                return MapColor.AIR;
        }
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        BlockPos offset = pos.offset(side.getOpposite());
        return BlockFaceShape.SOLID == world.getBlockState(offset).getBlockFaceShape(world, offset, side);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        EnumFacing facing = state.getValue(DIRECTION);
        if (side != facing.getOpposite()) {
            IBlockState other = world.getBlockState(pos.offset(side));
            return this == other.getBlock() && facing.getAxis() == other.getValue(DIRECTION).getAxis();
        }
        return true;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return face == state.getValue(DIRECTION).getOpposite() ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }
}
