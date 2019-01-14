package net.gegy1000.psf.server.modules.data;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.data.IWeatherData;
import net.minecraft.nbt.NBTTagCompound;

public class WeatherScanData implements IWeatherData {

    private int cleanWeatherTime;
    private int rainTime;
    private int thunderTime;

    public void setCleanWeatherTime(int cleanWeatherTime) {
        this.cleanWeatherTime = cleanWeatherTime;
    }

    public void setRainTime(int rainTime) {
        this.rainTime = rainTime;
    }

    public void setThunderTime(int thunderTime) {
        this.thunderTime = thunderTime;
    }

    @Override
    public int getCleanWeatherTime() {
        return cleanWeatherTime;
    }

    @Override
    public int getRainTime() {
        return rainTime;
    }

    @Override
    public int getThunderTime() {
        return thunderTime;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("cleanWeatherTime", getCleanWeatherTime());
        nbt.setInteger("rainTime", getRainTime());
        nbt.setInteger("thunderTime", getThunderTime());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        setCleanWeatherTime(nbt.getInteger("cleanWeatherTime"));
        setRainTime(nbt.getInteger("rainTime"));
        setThunderTime(nbt.getInteger("thunderTime"));
    }
}
