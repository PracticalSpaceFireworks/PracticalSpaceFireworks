package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.property.FuelTankBorder;
import net.gegy1000.psf.server.block.property.Part;
import net.gegy1000.psf.server.modules.ModuleFuelTank;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ExtensionMethod(BlockFuelTank.class)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockFuelTank extends BlockModule {
    public static final PropertyEnum<Part> PART = PropertyEnum.create("part", Part.class, Part.PARTS);
    public static final ImmutableMap<FuelTankBorder, PropertyBool> BORDERS = FuelTankBorder.BORDERS.stream()
        .collect(Maps.toImmutableEnumMap(Function.identity(), b -> PropertyBool.create(b.getName())));

    public BlockFuelTank(String module) {
        super(Material.IRON, module);
        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    public static boolean isFuelTank(Block block) {
        return block instanceof BlockFuelTank;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        val builder = new BlockStateContainer.Builder(this);
        BORDERS.values().forEach(builder::add);
        return builder.add(DIRECTION, PART).build();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState();
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        state = super.getActualState(state, access, pos);
        val part = Part.forPosition(access, pos, BlockFuelTank::isFuelTank);
        state = state.withProperty(PART, part);
        if (Part.TOP == part || Part.BOTH == part) {
            for (val border : FuelTankBorder.CARDINALS) {
                val block = access.getBlockState(border.offset(pos)).getBlock();
                boolean value = !isFuelTank(block);
                state = state.withProperty(BORDERS.get(border), value);
            }
            for (val border : FuelTankBorder.ORDINALS) {
                if (state.getValue(BORDERS.get(border.primary()))) {
                    state = state.withProperty(BORDERS.get(border), true);
                } else {
                    val secondary = state.getValue(BORDERS.get(border.secondary()));
                    val value = secondary || !isFuelTank(access.getBlockState(border.offset(pos)).getBlock());
                    state = state.withProperty(BORDERS.get(border), value);
                }
            }
        }
        return state;
    }

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
        if (EnumFacing.UP != side) {
            state = state.getActualState(access, pos);
            if (Part.TOP != state.getValue(PART) || state.getValue(BORDERS.get(FuelTankBorder.forDirection(side)))) {
                return BlockFaceShape.SOLID;
            }
        }
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (!CONVERTING.get()) {
            updateNeighbors(worldIn, pos);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        if (!CONVERTING.get()) {
            updateNeighbors(world, pos);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (placer instanceof EntityPlayer && ((EntityPlayer) placer).capabilities.isCreativeMode) {
            val module = TileModule.getModule(world.getTileEntity(pos));
            if (module instanceof ModuleFuelTank) {
                ((ModuleFuelTank) module).setFull();
            }
        }
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess access, BlockPos pos, EnumFacing side) {
        state = state.getActualState(access, pos);
        val part = state.getValue(PART);
        if (Part.TOP == part || Part.BOTH == part) {
            if (side.getAxis().isHorizontal()) {
                val offset = pos.offset(side);
                return !isFuelTank(access.getBlockState(offset).getBlock())
                    || !isFuelTank(access.getBlockState(offset.up()).getBlock());
            }
            if (EnumFacing.UP == side) {
                for (val property : BORDERS.values()) {
                    if (!state.getValue(property)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void updateNeighbors(World world, BlockPos pos) {
        for (val facing : EnumFacing.values()) {
            val offset = pos.offset(facing);
            val other = world.getBlockState(offset).getBlock();
            world.notifyNeighborsOfStateChange(offset, other, true);
        }
    }
}
