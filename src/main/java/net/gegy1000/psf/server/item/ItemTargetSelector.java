package net.gegy1000.psf.server.item;

import javax.annotation.Nonnull;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemTargetSelector extends Item {
    
    public ItemTargetSelector() {
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
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
                
                System.out.println(p);
                
                ISatellite closest = PracticalSpaceFireworks.PROXY.getSatellites().getAll().stream()
                    .filter(ISatellite::isOrbiting)
                    .filter(s -> s.getWorld() == worldIn)
                    .sorted((s1, s2) -> Double.compare(s1.getPosition().distanceSq(p), s2.getPosition().distanceSq(p)))
                    .findFirst()
                    .orElse(null);
                
                if (closest != null) {
                    playerIn.sendStatusMessage(new TextComponentString(closest.getName() + " firing at " + p), true);
                    closest.getModuleCaps(CapabilityModuleData.SPACE_LASER).forEach(l -> l.activate(closest, p));
                }
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
