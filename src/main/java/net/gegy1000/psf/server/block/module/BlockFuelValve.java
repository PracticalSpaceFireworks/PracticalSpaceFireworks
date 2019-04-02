package net.gegy1000.psf.server.block.module;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.Machine;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.gegy1000.psf.server.init.PSFItems;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.network.PacketDisplayContainerMessage;
import net.gegy1000.psf.server.util.FluidTransferUtils;
import net.gegy1000.psf.server.util.PSFGuiHandler;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockFuelValve extends BlockFuelTank implements Machine {
    public BlockFuelValve() {
        super("fuel_valve");
        setDefaultState(getDefaultState().withProperty(ACTIVE, false));
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        val builder = new BlockStateContainer.Builder(this);
        BORDERS.values().forEach(builder::add);
        return builder.add(ACTIVE, DIRECTION, PART).build();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        val module = TileModule.getModule(world.getTileEntity(pos));
        if (module == null) return false;
        if (world.isRemote) return true;
        if (FluidTransferUtils.transferWithHeldItem(world, pos, player, hand, side)) {
            return true;
        }
        if (module.getOwner() == null) {
            if (player instanceof EntityPlayerMP) {
                PSFNetworkHandler.network.sendTo(PacketDisplayContainerMessage.builder()
                    .title(new TextComponentTranslation("gui.psf.fuel_valve"))
                    .line(new TextComponentTranslation("gui.psf.fuel_valve.no_controller.0")
                        .setStyle(new Style().setBold(true).setColor(TextFormatting.RED)))
                    .line(new TextComponentTranslation("gui.psf.fuel_valve.no_controller.1"))
                    .build(), (EntityPlayerMP) player);
            }
        } else {
            PracticalSpaceFireworks.openGui(PSFGuiHandler.ID_FUEL_VALVE, player, world, pos);
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess access, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(PSFBlocks.FUEL_TANK));
        drops.add(new ItemStack(PSFItems.FUEL_VALVE));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult hit, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(PSFBlocks.FUEL_TANK);
    }
}
