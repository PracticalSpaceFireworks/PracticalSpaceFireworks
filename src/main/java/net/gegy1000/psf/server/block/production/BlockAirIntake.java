package net.gegy1000.psf.server.block.production;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.block.Machine;
import net.gegy1000.psf.server.util.AxisDirectionalBB;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockAirIntake extends BlockDirectional implements Machine, RegisterItemModel, RegisterItemBlock, RegisterTileEntity {
    private static final AxisDirectionalBB AABB = new AxisDirectionalBB(0.0625, 0.0625, 0.25, 0.9375, 0.9375, 1.0);

    public BlockAirIntake() {
        super(Material.IRON);
        setHarvestLevel("pickaxe", 1);
        setSoundType(SoundType.METAL);
        setHardness(2.0F);
        setResistance(3.0F);
        setLightOpacity(4);
        setCreativeTab(PracticalSpaceFireworks.TAB);
        setDefaultState(getDefaultState()
            .withProperty(ACTIVE, false)
            .withProperty(FACING, EnumFacing.NORTH)
        );
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        val active = 1 == (meta & 0b1);
        val facing = EnumFacing.byIndex(meta >> 1);
        return getDefaultState().withProperty(ACTIVE, active).withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        val active = state.getValue(ACTIVE) ? 1 : 0;
        val facing = state.getValue(FACING).getIndex() << 1;
        return active | facing;
    }

    @Override
    @Deprecated
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @Deprecated
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
        return AABB.withDirection(state.getValue(FACING));
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
        return side == state.getValue(FACING).getOpposite() ? BlockFaceShape.CENTER_BIG : BlockFaceShape.UNDEFINED;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0F;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTIVE, FACING);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileAirIntake();
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        val state = world.getBlockState(pos);
        return this == state.getBlock() && world.setBlockState(pos, state.cycleProperty(FACING));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        if (!placer.isSneaking()) {
            val state = world.getBlockState(pos.offset(side.getOpposite()));
            if (this == state.getBlock()) {
                return getDefaultState().withProperty(FACING, state.getValue(FACING));
            }
        }
        return getDefaultState().withProperty(FACING, side);
    }

    @Override
    public Class<? extends TileEntity> getEntityClass() {
        return TileAirIntake.class;
    }
}
