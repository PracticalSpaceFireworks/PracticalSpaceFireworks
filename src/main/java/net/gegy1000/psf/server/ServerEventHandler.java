package net.gegy1000.psf.server;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        event.addCapability(CapabilityWorldData.SATELLITE_ID, new SatelliteWorldData.Impl(event.getObject()));
    }

    @SubscribeEvent
    public static void onWorldUpdate(TickEvent.WorldTickEvent event) {
        if (event.phase == Phase.END && !event.world.isRemote && event.world.hasCapability(CapabilityWorldData.SATELLITE_INSTANCE, null)) {
            Collection<ISatellite> satellites = event.world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null).getSatellites();
            satellites.forEach(satellite -> satellite.tickSatellite(event.world.getTotalWorldTime()));
        }
    }

    @SubscribeEvent
    public static void onCollide(GetCollisionBoxesEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) {
            World world = entity.getEntityWorld();

            int radius = TileController.CONTIGUOUS_RANGE;
            AxisAlignedBB searchBounds = event.getAabb().expand(radius, radius, radius);
            List<EntitySpacecraft> spacecrafts = world.getEntitiesWithinAABB(EntitySpacecraft.class, searchBounds);

            for (EntitySpacecraft spacecraft : spacecrafts) {
                if (event.getAabb().intersects(spacecraft.getEntityBoundingBox())) {
                    spacecraft.collectTransformedBlockBounds(event.getAabb(), event.getCollisionBoxesList());
                }
            }
        }
    }
}
