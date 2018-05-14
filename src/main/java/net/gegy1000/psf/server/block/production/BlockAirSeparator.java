package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.util.FluidTransferUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockAirSeparator extends Block implements RegisterItemModel, RegisterItemBlock, RegisterTileEntity {
    public BlockAirSeparator() {
        super(Material.IRON);
        this.setHarvestLevel("pickaxe", 1);
        this.setSoundType(SoundType.METAL);
        this.setHardness(2.0F);
        this.setResistance(3.0F);
        this.setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IFluidHandlerItem heldFluidHandler = player.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (heldFluidHandler != null) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity != null) {
                IFluidHandler fluidHandler = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
                if (fluidHandler != null) {
                    int amount = fluidHandler.getTankProperties()[0].getCapacity();
                    FluidTransferUtils.transfer(fluidHandler, heldFluidHandler, amount);
                    player.setHeldItem(hand, heldFluidHandler.getContainer());
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockChanged, BlockPos fromPos) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileAirSeparator) {
            TileAirSeparator separator = (TileAirSeparator) entity;
            for (TileAirSeparator connected : separator.getConnectedSeparators()) {
                connected.markConnectedDirty();
            }
        }
        super.neighborChanged(state, world, pos, blockChanged, fromPos);
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
    public Class<? extends TileEntity> getEntityClass() {
        return TileAirSeparator.class;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
