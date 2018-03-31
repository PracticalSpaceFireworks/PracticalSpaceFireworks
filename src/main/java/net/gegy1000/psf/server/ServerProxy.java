package net.gegy1000.psf.server;

import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.util.MaterialMass;

public class ServerProxy {
    public void onPreInit() {
        MaterialMass.register();

        CapabilityController.register();
        CapabilityModule.register();
    }

    public void onInit() {
    }

    public void onPostInit() {
    }
}
