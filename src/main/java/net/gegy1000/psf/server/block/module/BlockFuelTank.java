package net.gegy1000.psf.server.block.module;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.block.property.FuelTankBorder;
import net.gegy1000.psf.server.modules.ModuleFuelTank;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Locale;
import java.util.function.Function;

public class BlockFuelTank extends BlockModule {

    private static final PropertyEnum<Rim> RIM = PropertyEnum.create("rim", Rim.class, Rim.RIM);

    private static final ImmutableMap<FuelTankBorder, PropertyBool> BORDERS = FuelTankBorder.BORDERS.stream()
            .collect(Maps.toImmutableEnumMap(Function.identity(), b -> PropertyBool.create(b.getName())));

    public BlockFuelTank() {
        super(Material.IRON, "fuel_tank");
        this.setSoundType(SoundType.METAL);
        this.setHardness(3.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        BlockStateContainer.Builder builder = new BlockStateContainer.Builder(this);
        BORDERS.values().forEach(builder::add);
        return builder.add(DIRECTION, RIM).build();
    }

    @Override
    public IBlockState withRotation(@Nonnull IBlockState state, @Nonnull Rotation rot) {
        return state;
    }

    @Override
    public IBlockState withMirror(@Nonnull IBlockState state, @Nonnull Mirror mirror) {
        return state;
    }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return getDefaultState();
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return true;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        state = state.getActualState(world, pos);
        Rim rim = state.getValue(RIM);
        if (Rim.TOP == rim || Rim.BOTH == rim) {
            if (side.getAxis().isHorizontal()) {
                BlockPos offset = pos.offset(side);
                return world.getBlockState(offset).getBlock() != this
                        || world.getBlockState(offset.up()).getBlock() != this;
            } else if (side == EnumFacing.UP) {
                for (PropertyBool property : BORDERS.values()) {
                    if (!state.getValue(property)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        Rim edges = sectionForPosition(world, pos);
        state = state.withProperty(RIM, edges);
        if (Rim.TOP == edges || Rim.BOTH == edges) {
            for (FuelTankBorder border : FuelTankBorder.CARDINALS) {
                Block block = world.getBlockState(border.offset(pos)).getBlock();
                state = state.withProperty(BORDERS.get(border), this != block);
            }
            for (FuelTankBorder border : FuelTankBorder.ORDINALS) {
                if (state.getValue(BORDERS.get(border.primary()))) {
                    state = state.withProperty(BORDERS.get(border), true);
                } else {
                    boolean secondary = state.getValue(BORDERS.get(border.secondary()));
                    boolean value = secondary || this != world.getBlockState(border.offset(pos)).getBlock();
                    state = state.withProperty(BORDERS.get(border), value);
                }
            }
        }
        return super.getActualState(state, world, pos);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (!CONVERTING.get()) {
            updateNeighbors(worldIn, pos);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (placer instanceof EntityPlayer && ((EntityPlayer) placer).capabilities.isCreativeMode) {
            IModule module = TileModule.getModule(world.getTileEntity(pos));
            if (module instanceof ModuleFuelTank) {
                ((ModuleFuelTank) module).setFull();
            }
        }
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        super.breakBlock(world, pos, state);
        if (!CONVERTING.get()) {
            updateNeighbors(world, pos);
        }
    }

    private void updateNeighbors(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offset = pos.offset(facing);
            Block other = world.getBlockState(offset).getBlock();
            world.notifyNeighborsOfStateChange(offset, other, true);
        }
    }

    @Override
    public boolean isStructuralModule(@Nullable IBlockState connecting, @Nonnull IBlockState state) {
        return true;
    }

    @Override
    protected boolean isDirectional() {
        return false;
    }

    private Rim sectionForPosition(IBlockAccess world, BlockPos pos) {
        if (this == world.getBlockState(pos.up()).getBlock()) {
            if (this == world.getBlockState(pos.down()).getBlock()) {
                return Rim.NONE;
            }
            return Rim.BOTTOM;
        }
        if (this == world.getBlockState(pos.down()).getBlock()) {
            return Rim.TOP;
        }
        return Rim.BOTH;
    }

    public enum Rim implements IStringSerializable {
        BOTTOM, TOP, NONE, BOTH;

        public static final ImmutableSet<Rim> RIM = Sets.immutableEnumSet(EnumSet.allOf(Rim.class));

        private static final Rim[] VALUES = values();

        @Override
        public final String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
