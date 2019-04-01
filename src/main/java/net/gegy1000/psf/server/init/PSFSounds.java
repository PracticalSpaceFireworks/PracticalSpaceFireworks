package net.gegy1000.psf.server.init;

import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

@ObjectHolder(PracticalSpaceFireworks.MODID)
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public final class PSFSounds {
    public static final SoundEvent PAYLOAD_SEPARATOR_CONNECT = null;
    public static final SoundEvent PAYLOAD_SEPARATOR_DISCONNECT = null;
    public static final SoundEvent SPACECRAFT_LAUNCH = null;
    public static final SoundEvent LASER_FIRE = null;
    public static final SoundEvent METAL_BREAK = null;
    public static final SoundEvent METAL_STEP = null;
    public static final SoundEvent METAL_PLACE = null;
    public static final SoundEvent METAL_HIT = null;
    public static final SoundEvent METAL_FALL = null;

    private PSFSounds() {
        throw new UnsupportedOperationException();
    }

    @SubscribeEvent
    static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {
        @Nonnull val registry = event.getRegistry();

        register(registry, "payload_separator_connect", "payload_separator.connect");
        register(registry, "payload_separator_disconnect", "payload_separator.disconnect");

        register(registry, "spacecraft_launch", "entity.spacecraft.launch");
        register(registry, "laser_fire", "laser.fire");

        register(registry, "metal_break", "metal.break");
        register(registry, "metal_step", "metal.step");
        register(registry, "metal_place", "metal.place");
        register(registry, "metal_hit", "metal.hit");
        register(registry, "metal_fall", "metal.fall");
    }

    private static void register(final IForgeRegistry<SoundEvent> registry, String name, String key) {
        registry.register(new SoundEvent(namespace(key)).setRegistryName(namespace(name)));
    }
}
