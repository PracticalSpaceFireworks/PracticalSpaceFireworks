package net.gegy1000.psf.server.block.module;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.init.PSFSounds;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static net.gegy1000.psf.server.init.PSFSounds.PAYLOAD_SEPARATOR_CONNECT;
import static net.gegy1000.psf.server.init.PSFSounds.PAYLOAD_SEPARATOR_DISCONNECT;

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
        setDefaultState(getDefaultState()
            .withProperty(DIRECTION, EnumFacing.UP)
            .withProperty(SECURE, false)
        );
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION, SECURE);
    }

    @Override
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        return EnumFacing.UP == side;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(SECURE, isStructural(getDefaultState(), world.getBlockState(pos.up())));
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos offset) {
        if (!CONVERTING.get() && pos.getX() == offset.getX() && pos.getY() + 1 == offset.getY() && pos.getZ() == offset.getZ()) {
            val secure = state.getValue(SECURE);
            if (secure != isStructural(state, world.getBlockState(offset)))
                if (world.setBlockState(pos, state.withProperty(SECURE, !secure))) {
                    val sound = !secure ? PAYLOAD_SEPARATOR_CONNECT : PAYLOAD_SEPARATOR_DISCONNECT;
                    world.playSound(null, pos, sound, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.25F + 0.6F);
                }
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(SECURE) ? 1 : 0;
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(SECURE, 1 == meta);
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public boolean causesSuffocation(IBlockState state) {
        return true;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (CONVERTING.get() || !state.getValue(SECURE)) return;
        val pitch = world.rand.nextFloat() * 0.25F + 0.6F;
        world.playSound(null, pos, PAYLOAD_SEPARATOR_CONNECT, SoundCategory.BLOCKS, 0.5F, pitch);
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess access, BlockPos pos, EnumFacing side) {
        return side.getAxis().isVertical() || this == access.getBlockState(pos.offset(side)).getBlock();
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return BlockRenderLayer.SOLID == layer || BlockRenderLayer.CUTOUT == layer;
    }
}
