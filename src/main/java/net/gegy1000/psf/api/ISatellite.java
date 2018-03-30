package net.gegy1000.psf.api;

import java.util.Collection;

public interface ISatellite {
    
    IController getController();
    
    Collection<IModule> getModules();
    
    // Other stuff about position/speed/etc ?

}
