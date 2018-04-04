package net.gegy1000.psf.server.modules.cap;

import net.gegy1000.psf.api.IEnergyHandler;

public class EnergyHandler implements IEnergyHandler {
    private final int maxDrain;
    private final int maxFill;

    public EnergyHandler(int maxDrain, int maxFill) {
        this.maxDrain = maxDrain;
        this.maxFill = maxFill;
    }

    public EnergyHandler(int maxDrain, int maxFill, int interval) {
        this.maxDrain = maxDrain / interval;
        this.maxFill = maxFill / interval;
    }

    @Override
    public int getMaxFill() {
        return this.maxFill;
    }

    @Override
    public int getMaxDrain() {
        return this.maxDrain;
    }
}
