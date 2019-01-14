package net.gegy1000.psf.server.modules.data;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        registry.register(new SimpleModuleDataDisplayFactory(ModuleDisplayMap::new, s -> {
            Collection<ITerrainScan> scans = s.getModuleCaps(CapabilityModuleData.TERRAIN_SCAN);
            if (scans.isEmpty()) {
                return null;
            }
            return new ModuleDisplayMap(new CompositeTerrainScan(scans));
        }));
    }

    public static @Nonnull IForgeRegistry<IModuleDataDisplayFactory> get() {
        final IForgeRegistry<IModuleDataDisplayFactory> r = registry;
        if (r != null) {
            return r;
        }
        throw new IllegalStateException("Accessing module registry too early!");
    }
}
