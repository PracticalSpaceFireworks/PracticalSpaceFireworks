package net.gegy1000.psf.server.block.module;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleFactory;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.modules.Modules;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@NoArgsConstructor
@AllArgsConstructor
public class TileModule extends TileEntity {

    @Getter
    private IModule module;
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityModule.INSTANCE || 
               (module instanceof IEnergyStorage && capability == CapabilityEnergy.ENERGY) ||
               super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModule.INSTANCE) {
            return CapabilityModule.INSTANCE.cast(module);
        }
        if (module instanceof IEnergyStorage && capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast((IEnergyStorage) module);
        }
        return super.getCapability(capability, facing);
    }
    
    @Override
    public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        ResourceLocation moduleID = module.getRegistryName();
        Preconditions.checkNotNull(moduleID, "Module does not have registry name set!");
        compound.setString("moduleID", moduleID.toString());
        compound.setTag("moduleData", module.serializeNBT());
        return super.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        String id = Strings.emptyToNull(compound.getString("moduleID"));
        Preconditions.checkNotNull(id, "No module data found!");
        IModuleFactory factory = Modules.get().getValue(new ResourceLocation(id));
        Preconditions.checkNotNull(factory, "Unknown module type!");
        this.module = factory.get();
        this.module.deserializeNBT(compound.getCompoundTag("moduleData"));
    }
}
