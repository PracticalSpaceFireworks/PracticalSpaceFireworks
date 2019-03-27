package net.gegy1000.psf.server.compat.waila;

import lombok.val;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.config.FormattingConfig;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@WailaPlugin
@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public final class PSFWailaPlugin implements IWailaEntityProvider, IWailaPlugin {
    private static final String RESET_FORMAT_CODE = "\u00a7r";

    @Nullable private static RayTraceResult subHit;
    @Nullable private static Vec3d lastHitVec;

    @SubscribeEvent
    static void wailaRenderPre(WailaRenderEvent.Pre event) {
        if (event.getAccessor().getEntity() instanceof EntitySpacecraft) {
            if (subHit == null || Type.BLOCK != subHit.typeOfHit) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    @Nonnull
    public List<String> getWailaHead(Entity entity, List<String> tooltip, IWailaEntityAccessor accessor, IWailaConfigHandler cfg) {
        val hitVec = accessor.getMOP().hitVec;
        if (lastHitVec != hitVec) {
            subHit = ((EntitySpacecraft) accessor.getEntity()).pointedBlock;
        }
        lastHitVec = hitVec;
        if (subHit != null && Type.BLOCK == subHit.typeOfHit) {
            tooltip.clear();
            val satellite = ((EntitySpacecraft) entity).getSatellite();
            tooltip.add(format(satellite.getName()));
        }
        return tooltip;
    }

    @Override
    @Nonnull
    public List<String> getWailaBody(Entity entity, List<String> tooltip, IWailaEntityAccessor accessor, IWailaConfigHandler cfg) {
        if (subHit != null && Type.BLOCK == subHit.typeOfHit) {
            val world = ((EntitySpacecraft) entity).getBody().getWorld();
            val pos = subHit.getBlockPos();
            val state = world.getBlockState(pos);
            val stack = state.getBlock().getPickBlock(state, subHit, world, pos, accessor.getPlayer());
            if (!stack.isEmpty()) {
                tooltip.clear();
                val name = localize(stack.getTranslationKey() + ".name");
                tooltip.add(localize("tooltip.psf.waila.looking_at", name));
            }
        }
        return tooltip;
    }

    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.registerHeadProvider(this, EntitySpacecraft.class);
        registrar.registerBodyProvider(this, EntitySpacecraft.class);
        registrar.registerTailProvider(this, EntitySpacecraft.class);
    }

    private String localize(String key, Object... args) {
        return new TextComponentTranslation(key, args).getUnformattedComponentText();
    }

    private String format(String string) {
        return RESET_FORMAT_CODE + String.format(FormattingConfig.entityFormat, string) + RESET_FORMAT_CODE;
    }
}
