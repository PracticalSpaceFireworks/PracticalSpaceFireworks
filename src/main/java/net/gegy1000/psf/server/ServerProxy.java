package net.gegy1000.psf.server;

import net.gegy1000.psf.server.capability.CapabilityController;

public class ServerProxy {
    public void onPreInit() {
        CapabilityController.register();
    }

    public void onInit() {
    }

    public void onPostInit() {
    }
}
