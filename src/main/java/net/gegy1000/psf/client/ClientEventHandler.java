package net.gegy1000.psf.client;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event) {
        if (event.getTarget() instanceof EntitySpacecraft) {
            EntitySpacecraft spacecraft = (EntitySpacecraft) event.getTarget();
            if (spacecraft.model != null) {
                spacecraft.model.delete();
                spacecraft.model = null;
            }
        }
    }
}
