package net.gegy1000.psf.api;

public interface IDataModule<T> extends IModule {
    T generateData();
}
