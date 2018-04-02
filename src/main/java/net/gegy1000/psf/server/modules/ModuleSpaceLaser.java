package net.gegy1000.psf.server.modules;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.data.ILaser;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.modules.data.EmptyTerrainScan;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

@ParametersAreNonnullByDefault
public class ModuleSpaceLaser extends EmptyModule implements ILaser {
    
    private static final int POWER_REQ = 10000;
    private static final int POWER_PER_TICK = 100;
    
    @Nullable
    private BlockPos target;
    
    private int powerUsed;

    public ModuleSpaceLaser() {
        super("laser");
    }

    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        super.onSatelliteTick(satellite);

        if (isActive()) {
            Collection<IEnergyStorage> powerSources = satellite.getModuleCaps(CapabilityEnergy.ENERGY);

            int powerToUse = POWER_PER_TICK;
            for (IEnergyStorage source : powerSources) {
                powerToUse -= source.extractEnergy(powerToUse, false);
                if (powerToUse <= 0) {
                    break;
                }
            }
            powerUsed += POWER_PER_TICK - powerToUse;
            
            if (powerUsed >= POWER_REQ) {
                BlockPos landing = satellite.getWorld().getHeight(target);
                satellite.getWorld().createExplosion(null, landing.getX(), landing.getY(), landing.getZ(), 20, true);
                target = null;
                powerUsed = 0;
            }
        }
    }
    
    @Override
    public int getTickInterval() {
        return isActive() ? 1 : Integer.MAX_VALUE;
    }
    
    @Override
    public boolean isActive() {
        return target != null;
    }
    
    @Override
    public void activate(ISatellite craft, BlockPos target) {
        if (craft.getWorld().getHeight(craft.getPosition()).distanceSq(target) <= 4096) {
            this.target = target;
        }
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.SPACE_LASER) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.SPACE_LASER) {
            return CapabilityModuleData.SPACE_LASER.cast(this);
        }
        return super.getCapability(capability, facing);
    }
}
