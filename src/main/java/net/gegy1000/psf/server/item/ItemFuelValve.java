package net.gegy1000.psf.server.item;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemFuelValve extends Item {
    public ItemFuelValve() {
        setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == PSFBlocks.FUEL_TANK && player.canPlayerEdit(pos, side, held)) {
            world.setBlockState(pos, PSFBlocks.FUEL_VALVE.getDefaultState());
            if (!player.capabilities.isCreativeMode) held.shrink(1);
            SoundType sound = state.getBlock().getSoundType(state, world, pos, player);
            world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
    }
}
