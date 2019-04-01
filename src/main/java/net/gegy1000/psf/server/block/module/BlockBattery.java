package net.gegy1000.psf.server.block.module;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.SoundType;
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
public class BlockBattery extends BlockModule {
    private static final AxisDirectionalBB AABB =
        new AxisDirectionalBB(0.125, 0.0625, 0.625, 0.875, 0.9375, 1.0);

    public BlockBattery(String module) {
        super(Material.CIRCUITS, module);
        setSoundType(SoundType.METAL);
        setHardness(1.0F);
        setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        val offset = pos.offset(side.getOpposite());
        val shape = on.getBlockFaceShape(world, offset, side);
        return BlockFaceShape.SOLID == shape;
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return AABB.withDirection(state.getValue(DIRECTION));
    }
}
