package net.gegy1000.psf.server.block.module;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.SoundType;
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
public class BlockThruster extends BlockModule {
    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0.125, 0.0625, 0.9375, 1.0, 0.9375);
    private static final AxisDirectionalBB AABB_WALL = new AxisDirectionalBB(0.0625, 0.125, 0.3125, 0.9375, 1.0, 1.0);

    public BlockThruster(String module) {
        super(Material.IRON, MapColor.SILVER, module);
        setSoundType(SoundType.METAL);
        setLightOpacity(4);
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        val facing = state.getValue(DIRECTION);
        return facing.getAxis().isVertical() ? AABB : AABB_WALL.withDirection(facing);
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        if (EnumFacing.UP == side) return false;
        val offset = pos.offset(side.getOpposite());
        val shape = on.getBlockFaceShape(world, offset, side);
        return BlockFaceShape.SOLID == shape;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }
}
