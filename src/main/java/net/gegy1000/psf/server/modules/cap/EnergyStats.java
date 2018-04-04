package net.gegy1000.psf.server.modules.cap;

public class EnergyStats {
    private final int maxDrain;
    private final int maxFill;

    public EnergyStats(int maxDrain, int maxFill) {
        this.maxDrain = maxDrain;
        this.maxFill = maxFill;
    }

    public EnergyStats(int maxDrain, int maxFill, int interval) {
        this.maxDrain = maxDrain / interval;
        this.maxFill = maxFill / interval;
    }

    public int getMaxFill() {
        return this.maxFill;
    }

    public int getMaxDrain() {
        return this.maxDrain;
    }
}
