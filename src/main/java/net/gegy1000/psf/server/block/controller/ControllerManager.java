package net.gegy1000.psf.server.block.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ControllerManager {
    
    private static final Object VALUE = new Object();
    
    private Map<TileController, Object> controllers = new WeakHashMap<>();
    
    public Collection<TileController> getControllers() {
        return Collections.unmodifiableSet(controllers.keySet());
    }

    public void registerController(TileController controller) {
        this.controllers.put(controller, VALUE);
    }
    
    public void unregisterController(TileController controller) {
        this.controllers.remove(controller);
    }
}
