package net.gegy1000.psf.server.block.module;

import com.google.common.base.MoreObjects;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleFactory;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.item.ItemBlockModule;
import net.gegy1000.psf.server.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockModule extends Block implements RegisterItemBlock, RegisterItemModel {
    
    public static final ThreadLocal<Boolean> CONVERTING = ThreadLocal.withInitial(() -> false);

    public static final IProperty<EnumFacing> DIRECTION = PropertyEnum.create("facing", EnumFacing.class);

    private final ResourceLocation moduleID;

    @Nullable
    private IModuleFactory factory; // lazy loaded

    public BlockModule(Material mat, String module) {
        this(mat, mat.getMaterialMapColor(), module);
    }

    public BlockModule(Material mat, MapColor color, String module) {
        super(mat, color);
        this.moduleID = new ResourceLocation(PracticalSpaceFireworks.MODID, module);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
        this.setHardness(2);
        this.setHarvestLevel("pickaxe", 1);
    }

    protected IModule createModule(World world, IBlockState state) {
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
    public ItemBlock createItemBlock(@Nonnull Block block) {
        return new ItemBlockModule(block);
    }

    @Override
    protected @Nonnull
    BlockStateContainer createBlockState() {
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
    
    @Override
    public @Nonnull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        return true;
    }
    
    public boolean isDirectional() {
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(@Nonnull World world, @Nonnull BlockPos pos) {
        return findValidFacing(world, pos) != null;
    }

    private boolean isSideValid(World world, BlockPos pos, EnumFacing side) {
        BlockPos offset = pos.offset(side.getOpposite());
        IBlockState state = world.getBlockState(offset).getActualState(world, offset);
        if (canAttachOnSide(world, pos, getDefaultState().withProperty(DIRECTION, side), state, side)) {
            if (isStructuralModule(null, getDefaultState()) || isStructural(getDefaultState(), state)) {
                return super.canPlaceBlockAt(world, pos);
            }
        }
        return false;
    }

    @Override
    public @Nonnull IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return this.getDefaultState().withProperty(DIRECTION, isSideValid(world, pos, side) ? side : MoreObjects.firstNonNull(findValidFacing(world, pos), EnumFacing.UP));
    }
    
    @Nullable
    private EnumFacing findValidFacing(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (isSideValid(world, pos, facing)) {
                return facing;
            }
        }
        return null;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        if (BlockModule.CONVERTING.get()) return;
        state = state.getActualState(world, pos);
        if (!isStructuralModule(null, state)) {
            BlockPos connectedTo = pos.offset(state.getValue(DIRECTION).getOpposite());
            if (connectedTo.equals(fromPos)) {
                IBlockState other = world.getBlockState(connectedTo).getActualState(world, connectedTo);
                if (!isStructural(state, other)) {
                    world.destroyBlock(pos, true);
                }
            }
        }
        IModule module = TileModule.getModule(world.getTileEntity(pos));
        if (module != null) {
            ISatellite owner = module.getOwner();
            if (owner != null && !owner.isInvalid()) {
                TileEntity te = world.getTileEntity(owner.getPosition());
                if (te instanceof TileController) {
                    ((TileController)te).scanStructure();
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
        Block block = state.getBlock();
        if (block instanceof BlockController) return true;
        if (!(block instanceof BlockModule)) return false;
        return ((BlockModule) block).isStructuralModule(connecting, state);
    }

    public static boolean isConnectedTo(IBlockState state, EnumFacing side) {
        Block block = state.getBlock();
        if (!(block instanceof BlockModule)) return true;
        if (!((BlockModule) block).isDirectional()) return true;
        return side == state.getValue(DIRECTION);
    }
}
