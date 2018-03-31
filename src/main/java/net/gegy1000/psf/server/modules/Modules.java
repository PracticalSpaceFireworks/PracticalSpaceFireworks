package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModuleFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class Modules {
    
    private static IForgeRegistry<IModuleFactory> registry; 
    
    @SubscribeEvent
    public static void createRegistry(RegistryEvent.NewRegistry event) {
        registry = new RegistryBuilder<IModuleFactory>()
                .setName(new ResourceLocation(PracticalSpaceFireworks.MODID, "modules"))
                .setType(IModuleFactory.class)
                .create();
    }
    
    @SubscribeEvent
    public static void registerModules(RegistryEvent.Register<IModuleFactory> event) {
        registry.register(new SimpleModuleFactory(() -> new EmptyModule("strut")).setRegistryName("strut"));
        
        registry.register(new SimpleModuleFactory(() -> new ModuleBattery(100000)).setRegistryName("battery_simple"));
        registry.register(new SimpleModuleFactory(() -> new ModuleThruster(ModuleThruster.ThrusterTier.SIMPLE)).setRegistryName("thruster"));
        registry.register(new SimpleModuleFactory(() -> new EmptyModule("antenna")).setRegistryName("antenna"));
        registry.register(new SimpleModuleFactory(() -> new EmptyModule("entity_detector")).setRegistryName("entity_detector"));
        registry.register(new SimpleModuleFactory(ModuleFuelTank::new).setRegistryName("fuel_tank"));
        registry.register(new SimpleModuleFactory(() -> new EmptyModule("payload_attacher")).setRegistryName("payload_attacher"));
    }

    public static @Nonnull IForgeRegistry<IModuleFactory> get() {
        final IForgeRegistry<IModuleFactory> r = registry;
        if (r != null) {
            return r;
        }
        throw new IllegalStateException("Accessing module registry too early!");
    }
}
