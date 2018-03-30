package net.gegy1000.psf.server.block.module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.api.RegisterBlockEntity;
import net.gegy1000.psf.server.block.PSFBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockModule extends Block implements RegisterBlockEntity {
    
    public BlockModule() {
        super(Material.IRON);
    }
    
    protected abstract IModule createModule(@Nonnull World world, @Nonnull IBlockState state);
    
    @Override
    public boolean canPlaceBlockOnSide(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        IBlockState on = worldIn.getBlockState(pos.offset(side.getOpposite()));
        if (on.getBlock() == PSFBlockRegistry.strut) {
            return super.canPlaceBlockOnSide(worldIn, pos, side);
        }
        return false;
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
    public Class<? extends TileEntity> getEntityClass() {
        return TileModule.class;
    }
}
