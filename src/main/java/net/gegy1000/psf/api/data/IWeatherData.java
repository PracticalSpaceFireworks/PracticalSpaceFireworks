package net.gegy1000.psf.api.data;

public interface IWeatherData extends IModuleData {
    int getCleanWeatherTime();
    int getRainTime();
    int getThunderTime();
}
