package net.gegy1000.psf.server.block.production;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.block.Machine;
import net.gegy1000.psf.server.util.FluidTransferUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockAirSeparator extends BlockHorizontal implements Machine, RegisterItemModel, RegisterItemBlock, RegisterTileEntity {
    private static final AxisAlignedBB AABB_X = new AxisAlignedBB(0.25, 0.0, 0.0, 0.75, 0.875, 1.0);
    private static final AxisAlignedBB AABB_Z = new AxisAlignedBB(0.0, 0.0, 0.25, 1.0, 0.875, 0.75);

    public BlockAirSeparator() {
        super(Material.IRON);
        setHarvestLevel("pickaxe", 1);
        setSoundType(SoundType.METAL);
        setHardness(2.0F);
        setResistance(3.0F);
        setLightOpacity(1);
        setCreativeTab(PracticalSpaceFireworks.TAB);
        setDefaultState(getDefaultState().withProperty(ACTIVE, false));
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        val active = 1 == (meta & 0b1);
        val facing = EnumFacing.byHorizontalIndex(meta >> 1);
        return getDefaultState().withProperty(ACTIVE, active).withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        val active = state.getValue(ACTIVE) ? 1 : 0;
        val facing = state.getValue(FACING).getHorizontalIndex() << 1;
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
        return Axis.X == state.getValue(FACING).getAxis() ? AABB_X : AABB_Z;
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockChanged, BlockPos fromPos) {
        @Nullable val te = world.getTileEntity(pos);
        if (te instanceof TileAirSeparator) {
            for (val separator : ((TileAirSeparator) te).getConnectedSeparators()) {
                separator.markConnectedDirty();
            }
        }
        super.neighborChanged(state, world, pos, blockChanged, fromPos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        @Nullable val fluidItem = player.getHeldItem(hand).getCapability(FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidItem != null) {
            @Nullable val te = world.getTileEntity(pos);
            if (te != null) {
                @Nullable val fluidTank = te.getCapability(FLUID_HANDLER_CAPABILITY, facing);
                if (fluidTank != null) {
                    val capacity = fluidTank.getTankProperties()[0].getCapacity();
                    FluidTransferUtils.transfer(fluidTank, fluidItem, capacity);
                    player.setHeldItem(hand, fluidItem.getContainer());
                    return true;
                }
            }
        }
        return false;
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
        return new TileAirSeparator();
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        val state = world.getBlockState(pos);
        return this == state.getBlock() && world.setBlockState(pos, state.cycleProperty(FACING));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public Class<? extends TileEntity> getEntityClass() {
        return TileAirSeparator.class;
    }
}
