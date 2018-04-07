package net.gegy1000.psf.server.block.remote;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class TileRemoteControlSystem extends TileEntity {
    
    private Map<UUID, IListedSpacecraft> crafts = new HashMap<>();
    
    private Collector<IListedSpacecraft, ?, Map<UUID, IListedSpacecraft>> toMap() {
        return Collectors.toMap(IListedSpacecraft::getId, Functions.identity());
    }

    public void rebuildCraftList() {
        if (!getWorld().isRemote) {
            crafts = PracticalSpaceFireworks.PROXY.getSatellites().getAll().stream()
                            .map(ISatellite::toListedCraft)
                            .collect(toMap());
        }
    }

    public void provideServerCrafts(List<IListedSpacecraft> crafts) {
        this.crafts = crafts.stream().collect(toMap());
    }
    
    public void provideSingleCraft(IListedSpacecraft craft) { 
        this.crafts.put(craft.getId(), craft);
    }
    
    public List<IListedSpacecraft> getCrafts() {
        return Lists.newArrayList(crafts.values());
    }
}
