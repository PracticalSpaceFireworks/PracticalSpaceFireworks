package net.gegy1000.psf.server.block.module;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleFactory;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockModule extends Block implements RegisterItemBlock, RegisterItemModel {

    public static final @Nonnull IProperty<EnumFacing> DIRECTION = PropertyEnum.create("facing", EnumFacing.class);

    private final ResourceLocation moduleID;

    private IModuleFactory factory; // lazy loaded

    public BlockModule(Material mat, @Nonnull String module) {
        super(mat);
        this.moduleID = new ResourceLocation(PracticalSpaceFireworks.MODID, module);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
        this.setHardness(2);
        this.setHarvestLevel("pickaxe", 1);
    }

    protected IModule createModule(@Nonnull World world, @Nonnull IBlockState state) {
        IModuleFactory factory = this.factory;
        if (factory == null) {
            factory = Modules.get().getValue(moduleID);
            if (factory == null) {
                throw new IllegalStateException("Could not find module for ID: " + moduleID);
            }
            this.factory = factory;
        }
        return factory.get();
    }

    @Override
    protected @Nonnull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION);
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return false;
    }
    
    protected boolean canAttachOnSide(IBlockState state, IBlockState on, EnumFacing side) {
        return true;
    }

    @Override
    public boolean canPlaceBlockOnSide(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        BlockPos pos2 = pos.offset(side.getOpposite());
        IBlockState on = world.getBlockState(pos2).getActualState(world, pos2);
        
        if (!canAttachOnSide(getDefaultState().withProperty(DIRECTION, side), on, side)) {
            return false;
        }
        
        if (isStructuralModule(null, getDefaultState())) {
            return canPlaceBlockAt(world, pos);
        }

        return isStructural(getDefaultState(), on) && super.canPlaceBlockOnSide(world, pos, side);
    }

    @Override
    public @Nonnull IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(DIRECTION, facing);
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        state = state.getActualState(worldIn, pos);
        if (!isStructuralModule(null, state)) {
            BlockPos connectedTo = pos.offset(state.getValue(DIRECTION).getOpposite());
            if (connectedTo.equals(fromPos)) {
                IBlockState other = worldIn.getBlockState(connectedTo).getActualState(worldIn, connectedTo);
                if (!isStructural(state, other)) {
                    worldIn.destroyBlock(pos, true);
                }
            }
        }
    }

    @Override
    public boolean hasTileEntity(@Nonnull IBlockState state) {
        return true;
    }

    @Override
    @Nullable
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileModule(createModule(world, state));
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
        return state.getValue(DIRECTION).ordinal();
    }

    @Override
    public @Nonnull IBlockState getStateFromMeta(int meta) {
        meta = Math.abs(meta & 7) % EnumFacing.values().length;
        return getDefaultState().withProperty(DIRECTION, EnumFacing.values()[meta]);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(DIRECTION, rot.rotate(state.getValue(DIRECTION)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withProperty(DIRECTION, mirror.mirror(state.getValue(DIRECTION)));
    }

    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        return false;
    }

    public static boolean isStructural(@Nullable IBlockState connecting, IBlockState state) {
        return state.getBlock() instanceof BlockController ||
                (state.getBlock() instanceof BlockModule && ((BlockModule) state.getBlock()).isStructuralModule(connecting, state));
    }
}
