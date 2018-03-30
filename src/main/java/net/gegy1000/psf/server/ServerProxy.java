package net.gegy1000.psf.server;

import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;

public class ServerProxy {
    public void onPreInit() {
        CapabilityController.register();
        CapabilityModule.register();
    }

    public void onInit() {
    }

    public void onPostInit() {
    }
}
