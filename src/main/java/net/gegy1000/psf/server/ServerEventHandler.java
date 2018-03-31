package net.gegy1000.psf.server;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        event.addCapability(CapabilityWorldData.SATELLITE_ID, new SatelliteWorldData.Impl(event.getObject()));
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event){
        EntityLivingBase living = event.getEntityLiving();
        World world = living.getEntityWorld();
        if(!world.isRemote && world.hasCapability(CapabilityWorldData.SATELLITE_INSTANCE, null)) {
            BlockPos pos = new BlockPos(living.posX, living.posY + (double)living.getEyeHeight(), living.posZ);
            if(!(living instanceof EntityPlayer) && !living.isPotionActive(MobEffects.GLOWING) &&
              world.canSeeSky(pos) &&
              withinSatelliteRange(world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null).getSatellites(), pos, world)) {
                PotionEffect potioneffect = new PotionEffect(MobEffects.GLOWING, 200, 0, false, false);
                living.addPotionEffect(potioneffect);
            }
        }
    }

    private static boolean withinSatelliteRange(Collection<ISatellite> satellites, BlockPos pos, World world) {
        for (ISatellite satellite : satellites) {
            if(satellite.getModules().stream().anyMatch(s -> s.getName().equals("entity_detector")) &&
              world.getChunkFromBlockCoords(satellite.getPosition()) == world.getChunkFromBlockCoords(pos))
                return true;
        }
        return false;
    }
}
