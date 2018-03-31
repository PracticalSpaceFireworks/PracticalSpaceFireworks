package net.gegy1000.psf.server.block.remote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Predicates;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

@ParametersAreNonnullByDefault
public class TileRemoteControlSystem extends TileEntity {
    
    private List<ISatellite> crafts = new ArrayList<>(); 

    public void rebuildCraftList() {
        List<ICapabilityProvider> providers = new ArrayList<>();
        providers.addAll(PracticalSpaceFireworks.PROXY.getControllerManager(getWorld().isRemote).getControllers());
        providers.addAll(getWorld().getEntities(EntitySpacecraft.class, Predicates.alwaysTrue()));
        
        this.crafts = providers.stream()
                    .filter(te -> te.hasCapability(CapabilitySatellite.INSTANCE, null))
                    .map(te -> te.getCapability(CapabilitySatellite.INSTANCE, null))
                    .collect(Collectors.toList());
    }
    
    public List<ISatellite> getCrafts() {
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
