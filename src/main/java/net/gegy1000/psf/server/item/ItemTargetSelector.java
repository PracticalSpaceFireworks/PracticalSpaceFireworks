package net.gegy1000.psf.server.item;

import lombok.val;
import lombok.var;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemTargetSelector extends Item implements RegisterItemModel {
    private static final double REACH = 500.0;

    public ItemTargetSelector() {
        super();
        setCreativeTab(PracticalSpaceFireworks.TAB);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            val start = player.getPositionEyes(1.0F);
            val look = player.getLook(1.0F);
            val end = start.add(look.x * REACH, look.y * REACH, look.z * REACH);
            Optional.ofNullable(world.rayTraceBlocks(start, end)).map(RayTraceResult::getBlockPos).ifPresent(pos ->
                PracticalSpaceFireworks.PROXY.getSatellites().getAll().stream()
                    .filter(ISatellite::isOrbiting)
                    .filter(s -> s.getWorld() == world)
                    .filter(s -> !s.getModuleCaps(ModuleCapabilities.SPACE_LASER).isEmpty())
                    .min(Comparator.comparingDouble(s -> s.getPosition().distanceSq(pos))).ifPresent(s -> {
                    var activated = false;
                    for (val laser : s.getModuleCaps(ModuleCapabilities.SPACE_LASER)) {
                        activated |= laser.activate(s, pos);
                    }
                    if (activated) {
                        sendActivationMessage(player, s, pos);
                    }
                }));
        }
        return super.onItemRightClick(world, player, hand);
    }

    private void sendActivationMessage(EntityPlayer player, ISatellite satellite, BlockPos target) {
        val name = satellite.getName();
        val x = target.getX();
        val y = target.getY();
        val z = target.getZ();
        val key = "message.psf.target_selector.activated";
        player.sendStatusMessage(new TextComponentTranslation(key, name, x, y, z), true);
    }
}
