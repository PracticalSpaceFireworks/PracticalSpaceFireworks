package net.gegy1000.psf.server.block.module;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockPayloadSeparator extends BlockModule {
    public static final PropertyBool SECURE = PropertyBool.create("secure");

    public BlockPayloadSeparator() {
        super(Material.IRON, "payload_separator");
        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setLightOpacity(4);
        setCreativeTab(PracticalSpaceFireworks.TAB);
        setDefaultState(blockState.getBaseState()
                .withProperty(DIRECTION, EnumFacing.UP)
                .withProperty(SECURE, false)
        );
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return BlockRenderLayer.SOLID == layer || BlockRenderLayer.CUTOUT == layer;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION, SECURE);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(SECURE, isStructural(state, world.getBlockState(pos.up())));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos offset) {
        if (!CONVERTING.get() && pos.getX() == offset.getX() && pos.getY() + 1 == offset.getY() && pos.getZ() == offset.getZ()) {
            boolean secure = state.getActualState(world, pos).getValue(SECURE);
            SoundEvent sound = secure ? SoundEvents.BLOCK_PISTON_EXTEND : SoundEvents.BLOCK_FIRE_EXTINGUISH;
            float pitch = world.rand.nextFloat() * 0.25F + 0.6F;
            world.playSound(null, pos, sound, SoundCategory.BLOCKS, 0.5F, pitch);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (state.getActualState(world, pos).getValue(SECURE)) {
            float pitch = world.rand.nextFloat() * 0.25F + 0.6F;
            world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, pitch);
        }
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        return EnumFacing.UP == side;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side.getAxis().isVertical() || this == world.getBlockState(pos.offset(side)).getBlock();
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        return true;
    }

    @Override
    protected boolean isDirectional(IBlockState state) {
        return false;
    }
}
