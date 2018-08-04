package net.gegy1000.psf.server.sound;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class PSFSounds {

    public static final SoundEvent LASER_FIRE = createSoundWithName("laser.fire", "laser_fire");

    public static final SoundEvent METAL_BREAK = createSoundWithName("metal.break", "metal_break");
    public static final SoundEvent METAL_STEP = createSoundWithName("metal.step", "metal_step");
    public static final SoundEvent METAL_PLACE = createSoundWithName("metal.place", "metal_place");
    public static final SoundEvent METAL_HIT = createSoundWithName("metal.hit", "metal_hit");
    public static final SoundEvent METAL_FALL = createSoundWithName("metal.fall", "metal_fall");

    public static final SoundType METAL = new SoundType(1.0F, 1.75F, METAL_BREAK, METAL_STEP, METAL_PLACE, METAL_HIT, METAL_FALL);

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(LASER_FIRE, METAL_BREAK, METAL_STEP, METAL_PLACE, METAL_HIT, METAL_FALL);
    }

    private static SoundEvent createSoundWithName(String soundName, String registryName) {
        ResourceLocation sn = new ResourceLocation(PracticalSpaceFireworks.MODID, soundName);
        ResourceLocation rn = new ResourceLocation(PracticalSpaceFireworks.MODID, registryName);
        return new SoundEvent(sn).setRegistryName(rn);
    }
}
