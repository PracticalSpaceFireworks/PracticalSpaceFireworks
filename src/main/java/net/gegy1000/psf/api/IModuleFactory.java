package net.gegy1000.psf.api;

import com.google.common.base.Supplier;

import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IModuleFactory extends IForgeRegistryEntry<IModuleFactory>, Supplier<IModule> {}
