package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.property.FuelTankBorder;
import net.gegy1000.psf.server.modules.ModuleFuelTank;
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
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;
import java.util.Locale;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockFuelTank extends BlockModule {
    private static final PropertyEnum<Rim> RIM = PropertyEnum.create("rim", Rim.class, Rim.RIMS);

    private static final ImmutableMap<FuelTankBorder, PropertyBool> BORDERS = FuelTankBorder.BORDERS.stream()
        .collect(Maps.toImmutableEnumMap(Function.identity(), b -> PropertyBool.create(b.getName())));

    public BlockFuelTank() {
        super(Material.IRON, "fuel_tank");
        setSoundType(SoundType.METAL);
        setHardness(3.0F);
        setCreativeTab(PracticalSpaceFireworks.TAB);
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
        return builder.add(DIRECTION, RIM).build();
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
        val rim = rimForPosition(access, pos);
        state = state.withProperty(RIM, rim);
        if (Rim.TOP == rim || Rim.BOTH == rim) {
            for (val border : FuelTankBorder.CARDINALS) {
                val block = access.getBlockState(border.offset(pos)).getBlock();
                state = state.withProperty(BORDERS.get(border), this != block);
            }
            for (val border : FuelTankBorder.ORDINALS) {
                if (state.getValue(BORDERS.get(border.primary()))) {
                    state = state.withProperty(BORDERS.get(border), true);
                } else {
                    val secondary = state.getValue(BORDERS.get(border.secondary()));
                    val value = secondary || this != access.getBlockState(border.offset(pos)).getBlock();
                    state = state.withProperty(BORDERS.get(border), value);
                }
            }
        }
        return super.getActualState(state, access, pos);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess access, IBlockState state, BlockPos pos, EnumFacing side) {
        if (EnumFacing.UP != side) {
            state = state.getActualState(access, pos);
            if (Rim.TOP != state.getValue(RIM)) {
                return BlockFaceShape.SOLID;
            }
            if (state.getValue(BORDERS.get(FuelTankBorder.forDirection(side)))) {
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
        val rim = state.getValue(RIM);
        if (Rim.TOP == rim || Rim.BOTH == rim) {
            if (side.getAxis().isHorizontal()) {
                val offset = pos.offset(side);
                return this != access.getBlockState(offset).getBlock()
                    || this != access.getBlockState(offset.up()).getBlock();
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

    private Rim rimForPosition(IBlockAccess access, BlockPos pos) {
        if (this == access.getBlockState(pos.up()).getBlock()) {
            if (this == access.getBlockState(pos.down()).getBlock()) {
                return Rim.NONE;
            }
            return Rim.BOTTOM;
        }
        if (this == access.getBlockState(pos.down()).getBlock()) {
            return Rim.TOP;
        }
        return Rim.BOTH;
    }

    public enum Rim implements IStringSerializable {
        BOTTOM, TOP, NONE, BOTH;

        public static final ImmutableSet<Rim> RIMS =
            Sets.immutableEnumSet(EnumSet.allOf(Rim.class));

        @Override
        public final String getName() {
            return toString();
        }

        @Override
        public final String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
