package net.gegy1000.psf.server.compat.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.event.WailaRenderEvent;
import mcp.mobius.waila.config.FormattingConfig;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@WailaPlugin
public class PSFWailaPlugin implements IWailaEntityProvider, IWailaPlugin {
    private RayTraceResult subHit;
    private Vec3d lastHitVec;

    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    protected void onWailaRenderPre(WailaRenderEvent.Pre event) {
        if (event.getAccessor().getEntity() instanceof EntitySpacecraft) {
            if (subHit == null || subHit.typeOfHit != Type.BLOCK) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    @Nonnull
    public List<String> getWailaHead(Entity entity, List<String> list, IWailaEntityAccessor accessor, IWailaConfigHandler cfg) {
        if (lastHitVec != accessor.getMOP().hitVec) {
            subHit = ((EntitySpacecraft) accessor.getEntity()).pointedBlock;
        }
        lastHitVec = accessor.getMOP().hitVec;
        if (subHit != null && subHit.typeOfHit == Type.BLOCK) {
            ISatellite satellite = ((EntitySpacecraft) entity).getSatellite();
            return withValues(list, withFormatting(satellite.getName()));
        }
        return withValues(list);
    }

    @Override
    @Nonnull
    public List<String> getWailaBody(Entity entity, List<String> list, IWailaEntityAccessor accessor, IWailaConfigHandler cfg) {
        if (subHit != null && subHit.typeOfHit == Type.BLOCK) {
            DelegatedWorld world = ((EntitySpacecraft) entity).getDelegatedWorld();
            BlockPos pos = subHit.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            ItemStack stack = state.getBlock().getPickBlock(state, subHit, world, pos, accessor.getPlayer());
            if (!stack.isEmpty()) {
                String name = withLocalization(stack.getTranslationKey() + ".name");
                return withValues(list, withLocalization("tooltip.psf.waila.looking_at", name));
            }
        }
        return withValues(list);
    }

    @Nonnull
    @Override
    public List<String> getWailaTail(Entity entity, List<String> list, IWailaEntityAccessor accessor, IWailaConfigHandler cfg) {
        return subHit != null && subHit.typeOfHit == Type.BLOCK ? list : withValues(list);
    }

    @Override
    public void register(@Nonnull IWailaRegistrar registrar) {
        registrar.registerHeadProvider(this, EntitySpacecraft.class);
        registrar.registerBodyProvider(this, EntitySpacecraft.class);
        registrar.registerTailProvider(this, EntitySpacecraft.class);
    }

    @SafeVarargs
    private final <V> List<V> withValues(List<V> list, V... values) {
        list.clear();
        Collections.addAll(list, values);
        return list;
    }

    private String withLocalization(@Nonnull String key, @Nonnull Object... values) {
        return new TextComponentTranslation(key, values).getUnformattedComponentText();
    }

    private String withFormatting(@Nonnull String string) {
        return "\u00a7r" + String.format(FormattingConfig.entityFormat, string) + "\u00a7r";
    }
}
