package net.gegy1000.psf.server.modules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Arrays;
import java.util.Random;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.module.IEnergyStats;
import net.gegy1000.psf.api.module.ILaser;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.client.render.laser.LaserRenderer.LaserState;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.gegy1000.psf.server.modules.configs.ConfigBasicToggle;
import net.gegy1000.psf.server.modules.data.PacketLaserState;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

@ParametersAreNonnullByDefault
public class ModuleSpaceLaser extends EmptyModule implements ILaser {
    
    @RequiredArgsConstructor
    @Getter
    enum Strength {
        LOW(0.5),
        NORMAL(1),
        HIGH(2),
        ;

        private final double multiplier;
    }
    
    private static final int POWER_REQ = 100_000;
    private static final int POWER_PER_TICK = 250;
    
    private static final int DELAY_TIME = 5 * 20;

    private static final IEnergyStats USAGE_STATS = new EnergyStats(POWER_PER_TICK, 0);
    
    private final ConfigBasicToggle strengthConfig = new ConfigBasicToggle("strength", 1, Arrays.stream(Strength.values()).map(Enum::name).toArray(String[]::new));

    @Nullable
    private BlockPos target;
    
    private int powerUsed;
    
    private int fireDelay;

    public ModuleSpaceLaser() {
        super("laser");
        registerConfigs(strengthConfig);
    }

    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        super.onSatelliteTick(satellite);

        if (isActive()) {
            Strength str = Strength.valueOf(strengthConfig.getValue());
            int power = (int) (POWER_REQ * str.getMultiplier());
            
            if (powerUsed < power) {
                powerUsed += satellite.extractEnergy(POWER_PER_TICK);
            }
            
            if (powerUsed >= power) {
                BlockPos landing = satellite.getWorld().getHeight(target);
                if (fireDelay < DELAY_TIME) {
                    if (fireDelay == 0) {
                        sendState(satellite, LaserState.FIRING);
                    }
                    if (DELAY_TIME - fireDelay < 30) {
                        Random rand = satellite.getWorld().rand;
                        if (rand.nextInt((int) (5 / str.getMultiplier())) == 0) {
                            satellite.getWorld().createExplosion(null, landing.getX() + (rand.nextInt(7) - 3), landing.getY() + 1, landing.getZ() + (rand.nextInt(7) - 3), 1, true);
                        }
                    }
                    fireDelay++;
                } else {
                    satellite.getWorld().createExplosion(null, landing.getX(), landing.getY(), landing.getZ(), 15, true);
                    if (str == Strength.NORMAL || str == Strength.HIGH) {
                        satellite.getWorld().createExplosion(null, landing.getX(), landing.getY() - 3, landing.getZ(), 15, true);
                        satellite.getWorld().createExplosion(null, landing.getX(), landing.getY() - 6, landing.getZ(), 15, true);
                    }
                    if (str == Strength.HIGH) {
                        satellite.getWorld().createExplosion(null, landing.getX() + 10, landing.getY() - 3, landing.getZ() + 10, 15, true);
                        satellite.getWorld().createExplosion(null, landing.getX() + 10, landing.getY() - 3, landing.getZ() - 10, 15, true);
                        satellite.getWorld().createExplosion(null, landing.getX() - 10, landing.getY() - 3, landing.getZ() + 10, 15, true);
                        satellite.getWorld().createExplosion(null, landing.getX() - 10, landing.getY() - 3, landing.getZ() - 10, 15, true);
                    }

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
        if (capability == ModuleCapabilities.SPACE_LASER || capability == ModuleCapabilities.ENERGY_STATS) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.SPACE_LASER) {
            return ModuleCapabilities.SPACE_LASER.cast(this);
        } else if (capability == ModuleCapabilities.ENERGY_STATS) {
            return ModuleCapabilities.ENERGY_STATS.cast(USAGE_STATS);
        }
        return super.getCapability(capability, facing);
    }
}
