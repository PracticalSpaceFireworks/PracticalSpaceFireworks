package net.gegy1000.psf.server.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.module.IAdditionalMass;
import net.gegy1000.psf.api.module.IEntityList;
import net.gegy1000.psf.api.module.ILaser;
import net.gegy1000.psf.api.module.ITerrainScan;
import net.gegy1000.psf.api.module.IWeatherData;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.minecraftforge.common.capabilities.CapabilityManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilityModuleData {

    public static void register() {
        // TODO default IStorage ?
        CapabilityManager.INSTANCE.register(IAdditionalMass.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(IEntityList.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(ITerrainScan.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(ILaser.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(EnergyStats.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(IWeatherData.class, new BlankStorage<>(), () -> null); // FIXME
    }
}
