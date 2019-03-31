package net.gegy1000.psf.api.module;

public interface IWeatherData extends IModuleData {
    int getCleanWeatherTime();
    int getRainTime();
    int getThunderTime();
}
