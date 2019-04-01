package net.gegy1000.psf.server.block.controller;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockController extends BlockDirectional implements RegisterItemBlock, RegisterItemModel, RegisterTileEntity {
    public BlockController() {
        super(Material.IRON);
        setHarvestLevel("pickaxe", 1);
        setSoundType(SoundType.METAL);
        setHardness(2.0F);
        setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos offset) {
        super.neighborChanged(state, world, pos, neighbor, offset);
        @Nullable val te = world.getTileEntity(pos);
        if (te instanceof TileController) {
            ((TileController) te).scanStructure();
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        @Nullable val te = world.getTileEntity(pos);
        if (te instanceof TileController) {
            if (!world.isRemote) {
                ((TileController) te).constructEntity();
            }
            return true;
        }
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileController();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(FACING, side);
    }

    @Override
    public Class<? extends TileEntity> getEntityClass() {
        return TileController.class;
    }
}
