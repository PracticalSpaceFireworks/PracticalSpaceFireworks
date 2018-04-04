package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.IEnergyHandler;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.data.IEntityList;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.modules.cap.EnergyHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class ModuleEntityMarker extends EmptyModule {
    private static final int POWER_PER_TICK = 5000;
    private static final int TICK_INTERVAL = 40;

    private static final IEnergyHandler ENERGY_HANDLER = new EnergyHandler(POWER_PER_TICK, 0, TICK_INTERVAL);

    public ModuleEntityMarker() {
        super("entity_marker");
    }

    @Override
    public void onSatelliteTick(ISatellite satellite) {
        Collection<IEntityList> entityLists = satellite.getModuleCaps(CapabilityModuleData.ENTITY_LIST);
        for (IEntityList entityList : entityLists) {
            for (EntityLivingBase living : entityList.getEntities()) {
                if (!living.isPotionActive(MobEffects.GLOWING)) {
                    if (satellite.tryExtractEnergy(POWER_PER_TICK)) {
                        living.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0, false, false));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public int getTickInterval() {
        return TICK_INTERVAL;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == CapabilityModuleData.ENERGY_HANDLER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.ENERGY_HANDLER) {
            return CapabilityModuleData.ENERGY_HANDLER.cast(ENERGY_HANDLER);
        }
        return super.getCapability(capability, facing);
    }
}
