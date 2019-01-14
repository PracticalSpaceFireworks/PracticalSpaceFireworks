package net.gegy1000.psf.server.block.data;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import net.gegy1000.psf.api.data.IModuleDataDisplay;
import net.gegy1000.psf.server.modules.data.CompositeTerrainScan;

@AllArgsConstructor
@NoArgsConstructor
public class ModuleDisplayMap implements IModuleDataDisplay {
    
    @Delegate
    private CompositeTerrainScan scan = new CompositeTerrainScan(new ArrayList<>());
    
    @Override
    public void draw(int x, int y, int width, int height, float partialTicks) {
        // TODO Auto-generated method stub
        
    }
}
