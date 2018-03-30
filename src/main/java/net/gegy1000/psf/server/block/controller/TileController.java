package net.gegy1000.psf.server.block.controller;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.gegy1000.psf.server.capability.CapabilityController;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;


public class TileController extends TileEntity {
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityController.INSTANCE || super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityController.INSTANCE) {
            // TODO
            return null;
        }
        return super.getCapability(capability, facing);
    }

}
