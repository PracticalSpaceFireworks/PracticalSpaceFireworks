package net.gegy1000.psf.server.block.module;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockThruster extends BlockModule {
    private static final AxisAlignedBB BOUNDING_BOX_CENTER = new AxisAlignedBB(0.0625, 0.125, 0.0625, 0.9375, 1.0, 0.9375);
    private static final AxisDirectionalBB BOUNDING_BOX_WALL = AxisDirectionalBB.of(0.0625, 0.125, 0.3125, 0.9375, 1.0, 1.0);

    public BlockThruster(String module) {
        super(Material.IRON, MapColor.SILVER, module);
        setSoundType(SoundType.METAL);
        setLightOpacity(4);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return BlockRenderLayer.SOLID == layer || BlockRenderLayer.CUTOUT == layer;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(DIRECTION);
        return facing.getAxis().isVertical() ? BOUNDING_BOX_CENTER : BOUNDING_BOX_WALL.withFacing(facing);
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        if (EnumFacing.UP != side) {
            BlockPos offset = pos.offset(side.getOpposite());
            IBlockState other = world.getBlockState(offset);
            BlockFaceShape shape = other.getBlockFaceShape(world, offset, side);
            return BlockFaceShape.SOLID == shape;
        }
        return false;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }
}
