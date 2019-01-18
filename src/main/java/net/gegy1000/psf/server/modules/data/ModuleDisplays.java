package net.gegy1000.psf.server.modules.data;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModuleFactory;
import net.gegy1000.psf.api.data.IModuleDataDisplayFactory;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.gegy1000.psf.api.data.SimpleModuleDataDisplayFactory;
import net.gegy1000.psf.server.block.data.ModuleDisplayMap;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class ModuleDisplays {
    
    private static IForgeRegistry<IModuleDataDisplayFactory> registry; 
    
    @SubscribeEvent
    public static void createRegistry(RegistryEvent.NewRegistry event) {
        registry = new RegistryBuilder<IModuleDataDisplayFactory>()
                .setName(new ResourceLocation(PracticalSpaceFireworks.MODID, "module_displays"))
                .setType(IModuleDataDisplayFactory.class)
                .create();
    }
    
    @SubscribeEvent
    public static void registerModuleDisplays(RegistryEvent.Register<IModuleFactory> event) {
        registry.register(new SimpleModuleDataDisplayFactory(ModuleDisplayMap::new, crafts -> {
            Collection<ITerrainScan> scans = StreamSupport.stream(crafts.spliterator(), false).flatMap(s -> s.getModuleCaps(CapabilityModuleData.TERRAIN_SCAN).stream()).collect(Collectors.toList());
            if (scans.isEmpty()) {
                return null;
            }
            return new ModuleDisplayMap(new CompositeTerrainScan(scans));
        }).setRegistryName("map"));
    }

    public static @Nonnull IForgeRegistry<IModuleDataDisplayFactory> get() {
        final IForgeRegistry<IModuleDataDisplayFactory> r = registry;
        if (r != null) {
            return r;
        }
        throw new IllegalStateException("Accessing module registry too early!");
    }
}
