package net.gegy1000.psf.server.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IAdditionalMass;
import net.gegy1000.psf.api.data.IEntityList;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilityModuleData {
    @SuppressWarnings("null")
    @CapabilityInject(IAdditionalMass.class)
    @Nonnull
    public static final Capability<IAdditionalMass> ADDITIONAL_MASS = null;

    @SuppressWarnings("null")
    @CapabilityInject(IEntityList.class)
    @Nonnull
    public static final Capability<IEntityList> ENTITY_LIST = null;

    @SuppressWarnings("null")
    @CapabilityInject(ITerrainScan.class)
    @Nonnull
    public static final Capability<ITerrainScan> TERRAIN_SCAN = null;

    public static void register() {
        // TODO default IStorage ?
        CapabilityManager.INSTANCE.register(IAdditionalMass.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(IEntityList.class, new BlankStorage<>(), () -> null); // FIXME
        CapabilityManager.INSTANCE.register(ITerrainScan.class, new BlankStorage<>(), () -> null); // FIXME
    }
}
