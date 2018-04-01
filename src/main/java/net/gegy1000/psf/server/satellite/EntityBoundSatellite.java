package net.gegy1000.psf.server.satellite;

import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.remote.GuiControlSystem;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Setter;

public class EntityBoundSatellite implements ISatellite {
    private final EntitySpacecraft spacecraft;

    private final List<IModule> modules = new ArrayList<>();
    private IController controller;
    @Setter
    @Nonnull
    private String name = "";

    public EntityBoundSatellite(EntitySpacecraft spacecraft) {
        this.spacecraft = spacecraft;
    }

    public void detectModules() {
        this.modules.clear();
        this.controller = null;

        for (TileEntity entity : this.spacecraft.getBlockAccess().getEntities()) {
            if (entity.hasCapability(CapabilityController.INSTANCE, null)) {
                this.controller = entity.getCapability(CapabilityController.INSTANCE, null);
            } else if (entity.hasCapability(CapabilityModule.INSTANCE, null)) {
                this.modules.add(entity.getCapability(CapabilityModule.INSTANCE, null));
            }
        }
    }
    
    @Override
    public String getName() {
        return name.isEmpty() ? ISatellite.super.getName() : name;
    }

    @Override
    public UUID getId() {
        return this.spacecraft.getUniqueID();
    }

    @Override
    public IController getController() {
        return this.controller;
    }

    @Override
    public Collection<IModule> getModules() {
        return this.modules;
    }

    @Override
    public BlockPos getPosition() {
        return this.spacecraft.getPosition();
    }

    @Override
    public SpacecraftBlockAccess buildBlockAccess(BlockPos origin, World world) {
        return this.spacecraft.getBlockAccess();
    }

    @Override
    public void requestModules() {
        if (this.spacecraft.getEntityWorld().isRemote) {
            this.respondModulesClient();
        }
    }

    private void respondModulesClient() {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof GuiControlSystem) {
            ((GuiControlSystem) gui).setModules(this.getModules());
        }
    }

    @Override
    public World getWorld() {
        return spacecraft.getEntityWorld();
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ISatellite && ((ISatellite) obj).getId().equals(this.getId());
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = ISatellite.super.serializeNBT();
        tag.setString("name", name);
        return tag;
    }
    
    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {
        ISatellite.super.deserializeNBT(tag);
        if (tag != null) {
            this.name = tag.getString("name");
        }
    }
}
