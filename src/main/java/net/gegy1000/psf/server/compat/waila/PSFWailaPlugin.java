package net.gegy1000.psf.server.compat.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.config.FormattingConfig;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.List;

@WailaPlugin
public class PSFWailaPlugin implements IWailaEntityProvider, IWailaPlugin {
    @Override
    @Nonnull
    public List<String> getWailaHead(Entity entity, List<String> list, IWailaEntityAccessor accessor, IWailaConfigHandler cfg) {
        return toSingleton(list, withFormatting(((EntitySpacecraft) entity).getSatellite().getName()));
    }

    @Override
    @Nonnull
    public List<String> getWailaBody(Entity entity, List<String> list, IWailaEntityAccessor accessor, IWailaConfigHandler cfg) {
        EntityPlayer player = accessor.getPlayer();
        DelegatedWorld world = ((EntitySpacecraft) entity).getDelegatedWorld();
        ((EntitySpacecraft) entity).playerRayTrace(player).ifPresent(result -> {
            BlockPos pos = result.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            ItemStack stack = state.getBlock().getPickBlock(state, result, world, pos, player);
            if (!stack.isEmpty()) {
                String name = withLocalization(stack.getUnlocalizedName() + ".name");
                list.add(withLocalization("tooltip.psf.waila.looking_at" ,name));
            }
        });
        return list;
    }

    @Override
    public void register(@Nonnull IWailaRegistrar registrar) {
        registrar.registerHeadProvider(this, EntitySpacecraft.class);
        registrar.registerBodyProvider(this, EntitySpacecraft.class);
    }

    private <V> List<V> toSingleton(List<V> list, V value) {
        list.clear();
        list.add(value);
        return list;
    }

    private String withLocalization(@Nonnull String key, @Nonnull Object... values) {
        return new TextComponentTranslation(key, values).getUnformattedComponentText();
    }

    private String withFormatting(@Nonnull String string) {
        return "\u00a7r" + String.format(FormattingConfig.entityFormat, string) + "\u00a7r";
    }
}
