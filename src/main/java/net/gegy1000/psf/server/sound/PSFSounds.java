package net.gegy1000.psf.server.sound;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class PSFSounds {
    
    public static final SoundEvent LASER_FIRE = new SoundEvent(new ResourceLocation(PracticalSpaceFireworks.MODID, "laser.fire")).setRegistryName("laser_fire");

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(LASER_FIRE);
    }
}
