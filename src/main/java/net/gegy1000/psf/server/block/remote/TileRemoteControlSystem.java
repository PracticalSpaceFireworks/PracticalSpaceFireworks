package net.gegy1000.psf.server.block.remote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

@ParametersAreNonnullByDefault
public class TileRemoteControlSystem extends TileEntity {
    
    private List<IListedSpacecraft> crafts = new ArrayList<>();

    public void rebuildCraftList() {
        if (!getWorld().isRemote) {
            crafts = PracticalSpaceFireworks.PROXY.getSatellites().getAll().stream()
                            .map(ISatellite::toListedCraft)
                            .collect(Collectors.toList());
        }
    }

    public void provideServerCrafts(List<IListedSpacecraft> crafts) {
        this.crafts.clear();
        this.crafts.addAll(crafts);
    }
    
    public List<IListedSpacecraft> getCrafts() {
        return Collections.unmodifiableList(crafts);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return super.writeToNBT(compound);
    }
}
