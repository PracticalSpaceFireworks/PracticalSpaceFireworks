package net.gegy1000.psf.server.item;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.ILaser;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ItemTargetSelector extends Item implements RegisterItemModel {

    public ItemTargetSelector() {
        super();
        setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + "I heard you liked dev textures...");
    }

    @Override
    public @Nonnull ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        if (!worldIn.isRemote) {
            Vec3d headVec = playerIn.getPositionEyes(1);
            Vec3d start = headVec;
            Vec3d lookVec = playerIn.getLook(1);
            double reach = 500;
            headVec = headVec.addVector(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
            RayTraceResult mop = worldIn.rayTraceBlocks(start, headVec);
            if (mop != null) {
                playerIn.swingArm(handIn);
                BlockPos p = mop.getBlockPos();

                ISatellite closest = PracticalSpaceFireworks.PROXY.getSatellites().getAll().stream()
                        .filter(ISatellite::isOrbiting)
                        .filter(s -> s.getWorld() == worldIn)
                        .filter(s -> !s.getModuleCaps(CapabilityModuleData.SPACE_LASER).isEmpty())
                        .min(Comparator.comparingDouble(s -> s.getPosition().distanceSq(p)))
                    .orElse(null);

                if (closest != null) {
                    boolean activated = false;
                    Collection<ILaser> moduleCaps = closest.getModuleCaps(CapabilityModuleData.SPACE_LASER);
                    for (ILaser laser : moduleCaps) {
                        activated |= laser.activate(closest, p);
                    }

                    if (activated) {
                        playerIn.sendStatusMessage(new TextComponentString(closest.getName() + " firing at " + p), true);
                    }
                }
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
