package net.gegy1000.psf.server.modules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Collection;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.module.IEnergyStats;
import net.gegy1000.psf.api.module.IEntityList;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

@ParametersAreNonnullByDefault
public class ModuleEntityMarker extends EmptyModule {
    private static final int POWER_PER_TICK = 5000;
    private static final int TICK_INTERVAL = 40;

    private static final IEnergyStats USAGE_STATS = new EnergyStats(POWER_PER_TICK, 0, TICK_INTERVAL);

    public ModuleEntityMarker() {
        super("entity_marker");
    }

    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        Collection<IEntityList> entityLists = satellite.getModuleCaps(ModuleCapabilities.ENTITY_LIST);
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
        return super.hasCapability(capability, facing) || capability == ModuleCapabilities.ENERGY_STATS;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.ENERGY_STATS) {
            return ModuleCapabilities.ENERGY_STATS.cast(USAGE_STATS);
        }
        return super.getCapability(capability, facing);
    }
}
