package net.gegy1000.psf.server.block.module;

import lombok.val;
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
    private static final AxisDirectionalBB AABB = new AxisDirectionalBB(0.0, 0.0, 0.75, 1.0, 1.0, 1.0);

    public BlockSmallSolarPanel() {
        super(Material.GLASS, MapColor.LAPIS, "solar_panel_small");
        setSoundType(PSFSoundType.SMALL_DEVICE);
    }

    @Override
    @Deprecated
    public MapColor getMapColor(IBlockState state, IBlockAccess access, BlockPos pos) {
        switch (state.getValue(DIRECTION)) {
            case DOWN: return MapColor.BLACK;
            case UP: return MapColor.LAPIS;
            default: return MapColor.AIR;
        }
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return AABB.withDirection(state.getValue(DIRECTION));
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
        return side == state.getValue(DIRECTION).getOpposite() ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess access, BlockPos pos, EnumFacing side) {
        val facing = state.getValue(DIRECTION);
        if (side != facing.getOpposite()) {
            val other = access.getBlockState(pos.offset(side));
            return this == other.getBlock() && facing.getAxis() == other.getValue(DIRECTION).getAxis();
        }
        return true;
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        val offset = pos.offset(side.getOpposite());
        return BlockFaceShape.SOLID == world.getBlockState(offset).getBlockFaceShape(world, offset, side);
    }
}
