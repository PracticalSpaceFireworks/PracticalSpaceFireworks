package net.gegy1000.psf.server.item;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemBlockModule extends ItemBlock {

    public ItemBlockModule(Block block) {
        super(block);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
        Block block = world.getBlockState(pos).getBlock();
        if (!block.isReplaceable(world, pos)) {
            pos = pos.offset(side);
        }
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && player.canPlayerEdit(pos, side, stack)) {
            int meta = this.getMetadata(stack.getMetadata());
            IBlockState state = this.block.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, meta, player, hand);
            if (mayPlace(state, world, pos, side)) {
                if (placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
                    state = world.getBlockState(pos);
                    SoundType sound = state.getBlock().getSoundType(state, world, pos, player);
                    world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                    stack.shrink(1);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }

    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, EntityPlayer player, ItemStack stack) {
        Block block = world.getBlockState(pos).getBlock();
        boolean offset = false;
        if (Blocks.SNOW_LAYER == block && block.isReplaceable(world, pos)) {
            side = EnumFacing.UP;
        } else if (!block.isReplaceable(world, pos)) {
            pos = pos.offset(side);
            offset = true;
            block = world.getBlockState(pos).getBlock();
        }
        if (block.isReplaceable(world, pos)) {
            int meta = this.getMetadata(stack.getMetadata());
            @Nullable RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
            if (hit != null && RayTraceResult.Type.BLOCK == hit.typeOfHit) {
                Vec3d vec = hit.hitVec;
                BlockPos pos1 = offset ? pos.offset(side.getOpposite()) : pos;
                float x = (float) (vec.x - (double) pos1.getX());
                float y = (float) (vec.y - (double) pos1.getY());
                float z = (float) (vec.z - (double) pos1.getZ());
                IBlockState state = this.block.getStateForPlacement(world, pos, side, x, y, z, meta, player, player.getActiveHand());
                return mayPlace(state, world, pos, side);
            }
        }
        return false;
    }

    private boolean mayPlace(final IBlockState state, final World world, BlockPos pos, EnumFacing side) {
        @Nullable AxisAlignedBB box = state.getCollisionBoundingBox(world, pos);
        if (box == null) {
            return true;
        }
        if (world.checkNoEntityCollision(box.offset(pos), null)) {
            if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
                return block.canPlaceBlockOnSide(world, pos, side);
            }
        }
        return false;
    }
}
