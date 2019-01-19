package net.gegy1000.psf.server.modules.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModuleFactory;
import net.gegy1000.psf.api.data.IModuleDataDisplayFactory;
import net.gegy1000.psf.api.data.IScannedChunk;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.gegy1000.psf.api.data.SimpleModuleDataDisplayFactory;
import net.gegy1000.psf.server.block.data.ModuleDisplayMap;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
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
        registry.register(new SimpleModuleDataDisplayFactory(ModuleDisplayMap::new, requestData -> {
            Collection<ITerrainScan> scans = StreamSupport.stream(PracticalSpaceFireworks.PROXY.getSatellites().spliterator(), false)
            		.flatMap(s -> s.getModuleCaps(CapabilityModuleData.TERRAIN_SCAN).stream())
            		.collect(Collectors.toList());
            if (scans.isEmpty()) {
                return new NBTTagCompound();
            }
            Map<ChunkPos, List<IScannedChunk>> allChunks = new CompositeTerrainScan(scans).getChunks().stream()
            		.collect(Collectors.groupingBy(c -> new ChunkPos(c.getChunkPos().getX(), c.getChunkPos().getZ())));
            ITerrainScan localScan = new TerrainScanData();
    		int[] chunks = requestData.getIntArray("chunks");
    		for (int i = 0; i < chunks.length; i += 2) {
    			int x = chunks[i], z = chunks[i + 1];
    			List<IScannedChunk> sections = allChunks.get(new ChunkPos(x, z));
    			if (sections != null) {
    				sections.forEach(localScan::addChunk);
    			}
    		}
    		return localScan.serializeNBT();
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
