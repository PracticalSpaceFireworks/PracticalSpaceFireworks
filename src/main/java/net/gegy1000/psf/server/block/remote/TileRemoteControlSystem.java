package net.gegy1000.psf.server.block.remote;

import com.google.common.base.Predicates;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class TileRemoteControlSystem extends TileEntity {
    
    private List<IListedSpacecraft> worldCrafts = new ArrayList<>();
    private List<IListedSpacecraft> orbitalCrafts = new ArrayList<>();

    public void rebuildCraftList() {
        List<ICapabilityProvider> providers = new ArrayList<>();
        providers.addAll(PracticalSpaceFireworks.PROXY.getControllerManager(getWorld().isRemote).getControllers());
        providers.addAll(getWorld().getEntities(EntitySpacecraft.class, Predicates.alwaysTrue()));
        
        this.worldCrafts = providers.stream()
                    .filter(te -> te.hasCapability(CapabilitySatellite.INSTANCE, null))
                    .map(te -> te.getCapability(CapabilitySatellite.INSTANCE, null).toListedCraft())
                    .collect(Collectors.toList());
    }

    public void provideServerListedCrafts(List<IListedSpacecraft> crafts) {
        this.orbitalCrafts.clear();
        this.orbitalCrafts.addAll(crafts);
    }
    
    public List<IListedSpacecraft> getCrafts() {
        List<IListedSpacecraft> crafts = new ArrayList<>(this.worldCrafts.size() + this.orbitalCrafts.size());
        crafts.addAll(this.worldCrafts);
        crafts.addAll(this.orbitalCrafts);
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
