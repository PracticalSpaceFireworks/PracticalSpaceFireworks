package net.gegy1000.psf.server.block.module;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPayloadSeparator extends BlockModule {
    public BlockPayloadSeparator() {
        super(Material.IRON, "payload_separator");
        this.setSoundType(SoundType.METAL);
        this.setHardness(3.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
        this.setDefaultState(this.blockState.getBaseState().withProperty(DIRECTION, EnumFacing.UP));
    }

    @Override
    protected boolean canAttachOnSide(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull IBlockState on, @Nonnull EnumFacing side) {
        return EnumFacing.UP == side;
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return 0;
    }

    @Override
    public boolean doesSideBlockRendering(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return side.getAxis().isVertical();
    }

    @Override
    @Nonnull
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, @Nonnull IBlockState state) {
        return true;
    }

    @Override
    protected boolean isDirectional(@Nonnull IBlockState state) {
        return false;
    }
}
