package net.gegy1000.psf.api.event;

import net.gegy1000.psf.api.client.IVisualProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.registries.GameData;

import java.util.Map;

public final class RegisterVisualPropertiesEvent extends Event {
    private final Map<ResourceLocation, IVisualProperty<?>> registry;

    public RegisterVisualPropertiesEvent(Map<ResourceLocation, IVisualProperty<?>> registry) {
        this.registry = registry;
    }

    public void register(ResourceLocation identifier, IVisualProperty<?> property) {
        this.registry.put(identifier, property);
    }

    public void register(String identifier, IVisualProperty<?> property) {
        this.register(GameData.checkPrefix(identifier, true), property);
    }
}
