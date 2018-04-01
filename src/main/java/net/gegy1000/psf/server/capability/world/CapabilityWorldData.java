package net.gegy1000.psf.server.capability.world;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.capability.DelegatedStorage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilityWorldData {
    public static final ResourceLocation SATELLITE_ID = new ResourceLocation(PracticalSpaceFireworks.MODID, "satellite_data");

    @CapabilityInject(SatelliteWorldData.class)
    public static Capability<SatelliteWorldData> SATELLITE_INSTANCE;

    public static void register() {
        CapabilityManager.INSTANCE.register(SatelliteWorldData.class, new DelegatedStorage<>(), () -> {
            throw new IllegalStateException("Cannot construct satellite world data without world reference");
        });
    }
}
