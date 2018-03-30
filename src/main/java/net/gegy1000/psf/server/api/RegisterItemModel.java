package net.gegy1000.psf.server.api;

import net.minecraft.util.ResourceLocation;

public interface RegisterItemModel {
    default String getResource(ResourceLocation registryName) {
        return registryName.toString();
    }
}
