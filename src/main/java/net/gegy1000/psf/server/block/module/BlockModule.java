package net.gegy1000.psf.server.block.module;

import com.google.common.base.MoreObjects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.module.IModuleFactory;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.item.ItemBlockModule;
import net.gegy1000.psf.server.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Preconditions.checkState;
import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockModule extends Block implements RegisterItemBlock, RegisterItemModel {
    public static final ThreadLocal<Boolean> CONVERTING = ThreadLocal.withInitial(() -> false);
    public static final IProperty<EnumFacing> DIRECTION = BlockDirectional.FACING;

    @Getter(AccessLevel.PROTECTED)
    private final ResourceLocation moduleId;

    @Getter(lazy = true, value = AccessLevel.PROTECTED)
    private final IModuleFactory factory = lookupFactory();

    public BlockModule(Material mat, String module) {
        this(mat, mat.getMaterialMapColor(), module);
    }

    public BlockModule(Material mat, MapColor color, String module) {
        super(mat, color);
        moduleId = namespace(module);
        setHarvestLevel("pickaxe", 1);
        setHardness(2.0F);
        setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    public static boolean isConnectedTo(IBlockState state, EnumFacing side) {
        val block = state.getBlock();
        if (block instanceof BlockModule) {
            if (((BlockModule) block).isDirectional()) {
                return side == state.getValue(DIRECTION);
            }
        }
        return true;
    }

    public boolean isDirectional() {
        return true;
    }

    @Override
    public ItemBlock createItemBlock(Block block) {
        return new ItemBlockModule(block);
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(DIRECTION, EnumFacing.byIndex(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DIRECTION).getIndex();
    }

    @Override
    @Deprecated
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        if (isDirectional()) {
            return state.withProperty(DIRECTION, rot.rotate(state.getValue(DIRECTION)));
        }
        return state;
    }

    @Override
    @Deprecated
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        if (isDirectional()) {
            return state.withRotation(mirror.toRotation(state.getValue(DIRECTION)));
        }
        return state;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        if (!BlockModule.CONVERTING.get()) {
            state = state.getActualState(world, pos);
            if (!isStructuralModule(null, state)) {
                val connectedTo = pos.offset(state.getValue(DIRECTION).getOpposite());
                if (connectedTo.equals(fromPos)) {
                    val other = world.getBlockState(connectedTo).getActualState(world, connectedTo);
                    if (!isStructural(state, other)) {
                        world.destroyBlock(pos, true);
                    }
                }
            }
            val module = TileModule.getModule(world.getTileEntity(pos));
            if (module != null) {
                val owner = module.getOwner();
                if (owner != null && !owner.isDestroyed()) {
                    val te = world.getTileEntity(owner.getPosition());
                    if (te instanceof TileController) {
                        ((TileController) te).scanStructure();
                    }
                }
            }
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return findValidFacing(world, pos) != null;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DIRECTION);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Nullable
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileModule(createModule(world, state));
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        if (isDirectional()) {
            val state = world.getBlockState(pos);
            if (this == state.getBlock()) {
                return world.setBlockState(pos, state.cycleProperty(DIRECTION));
            }
        }
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(DIRECTION, isSideValid(world, pos, side) ? side : MoreObjects.firstNonNull(findValidFacing(world, pos), EnumFacing.UP));
    }

    protected IModule createModule(World world, IBlockState state) {
        return getFactory().get();
    }

    @Nullable
    private EnumFacing findValidFacing(World world, BlockPos pos) {
        for (val facing : EnumFacing.values()) {
            if (isSideValid(world, pos, facing)) {
                return facing;
            }
        }
        return null;
    }

    private boolean isSideValid(World world, BlockPos pos, EnumFacing side) {
        val offset = pos.offset(side.getOpposite());
        val state = world.getBlockState(offset).getActualState(world, offset);
        if (canAttachOnSide(world, pos, getDefaultState().withProperty(DIRECTION, side), state, side)) {
            if (isStructuralModule(null, getDefaultState()) || isStructural(getDefaultState(), state)) {
                return super.canPlaceBlockAt(world, pos);
            }
        }
        return false;
    }

    protected boolean canAttachOnSide(World world, BlockPos pos, IBlockState state, IBlockState on, EnumFacing side) {
        return true;
    }

    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        return false;
    }

    public static boolean isStructural(@Nullable IBlockState connecting, IBlockState state) {
        val block = state.getBlock();
        if (!(block instanceof BlockController)) {
            if (block instanceof BlockModule) {
                return ((BlockModule) block).isStructuralModule(connecting, state);
            }
            return false;
        }
        return true;
    }

    private IModuleFactory lookupFactory() {
        @Nullable val factory = Modules.get().getValue(moduleId);
        checkState(factory != null, "No module factory for id '%s'", moduleId);
        return factory;
    }
}
