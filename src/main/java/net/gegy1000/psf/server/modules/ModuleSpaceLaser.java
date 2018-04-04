package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.IEnergyHandler;
import net.gegy1000.psf.api.ILaser;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.client.render.laser.LaserRenderer.LaserState;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.modules.cap.EnergyHandler;
import net.gegy1000.psf.server.modules.data.PacketLaserState;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@ParametersAreNonnullByDefault
public class ModuleSpaceLaser extends EmptyModule implements ILaser {
    
    private static final int POWER_REQ = 10000;
    private static final int POWER_PER_TICK = 100;
    
    private static final int DELAY_TIME = 5 * 20;

    private static final IEnergyHandler ENERGY_HANDLER = new EnergyHandler(POWER_PER_TICK, 0);

    @Nullable
    private BlockPos target;
    
    private int powerUsed;
    
    private int fireDelay;

    public ModuleSpaceLaser() {
        super("laser");
    }

    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        super.onSatelliteTick(satellite);

        if (isActive()) {
            if (powerUsed < POWER_REQ) {
                powerUsed += satellite.extractEnergy(POWER_PER_TICK);
            }
            
            if (powerUsed >= POWER_REQ) {
                BlockPos landing = satellite.getWorld().getHeight(target);
                if (fireDelay < DELAY_TIME) {
                    if (fireDelay == 0) {
                        sendState(satellite, LaserState.FIRING);
                    }
                    if (DELAY_TIME - fireDelay < 30) {
                        Random rand = satellite.getWorld().rand;
                        if (rand.nextInt(5) == 0) {
                            satellite.getWorld().createExplosion(null, landing.getX() + (rand.nextInt(7) - 3), landing.getY() + 1, landing.getZ() + (rand.nextInt(7) - 3), 1, true);
                        }
                    }
                    fireDelay++;
                } else {
                    satellite.getWorld().createExplosion(null, landing.getX(), landing.getY(), landing.getZ(), 15, true);
                    satellite.getWorld().createExplosion(null, landing.getX(), landing.getY() - 3, landing.getZ(), 15, true);
                    satellite.getWorld().createExplosion(null, landing.getX(), landing.getY() - 6, landing.getZ(), 15, true);

                    sendState(satellite, LaserState.COMPLETE);
                    target = null;
                    powerUsed = 0;
                    fireDelay = 0;
                }
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
    public boolean activate(ISatellite craft, BlockPos target) {
        BlockPos oldTarget = this.target;
        if (oldTarget != null) {
            sendState(craft, oldTarget, LaserState.COMPLETE);
        }
        if (craft.getWorld().getHeight(craft.getPosition()).distanceSq(target) <= 4096) {
            this.target = target;
            sendState(craft, LaserState.CHARGING);
            return true;
        }
        return false;
    }
    
    private void sendState(ISatellite craft, BlockPos target, LaserState state) {
        PSFNetworkHandler.network.sendToDimension(new PacketLaserState(target, state), craft.getWorld().provider.getDimension());
    }

    private void sendState(ISatellite craft, LaserState state) {
        BlockPos p = this.target;
        if (p != null) {
            sendState(craft, p, state);
        }
    }
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.SPACE_LASER || capability == CapabilityModuleData.ENERGY_HANDLER) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.SPACE_LASER) {
            return CapabilityModuleData.SPACE_LASER.cast(this);
        } else if (capability == CapabilityModuleData.ENERGY_HANDLER) {
            return CapabilityModuleData.ENERGY_HANDLER.cast(ENERGY_HANDLER);
        }
        return super.getCapability(capability, facing);
    }
}
