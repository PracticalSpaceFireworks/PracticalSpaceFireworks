package net.gegy1000.psf.server.block.module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleFactory;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.block.PSFBlockRegistry;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockModule extends Block implements RegisterItemBlock {
    
    public static final @Nonnull IProperty<EnumFacing> DIRECTION = PropertyEnum.create("facing", EnumFacing.class);
    
    private final ResourceLocation moduleID;
    
    private IModuleFactory factory; // lazy loaded
    
    public BlockModule(Material mat, @Nonnull String module) {
        super(mat);
        this.moduleID = new ResourceLocation(PracticalSpaceFireworks.MODID, module);
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
    
    @Override
    public boolean canPlaceBlockOnSide(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        IBlockState on = worldIn.getBlockState(pos.offset(side.getOpposite()));
        if (on.getBlock() == PSFBlockRegistry.strut) {
            return super.canPlaceBlockOnSide(worldIn, pos, side);
        }
        return false;
    }

    @Override
    public @Nonnull IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(DIRECTION, facing);
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
        meta = Math.abs(meta) % EnumFacing.values().length;
        return getDefaultState().withProperty(DIRECTION, EnumFacing.values()[meta]);
    }
}
