package net.gegy1000.psf.server.satellite;

import lombok.Setter;
import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.entity.EntityListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
        SpacecraftBlockAccess blockAccess = this.spacecraft.getBlockAccess();

        this.modules.clear();
        this.modules.addAll(blockAccess.findModules());
        this.controller = blockAccess.findController();
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
    public SpacecraftBlockAccess buildBlockAccess(World world) {
        return this.spacecraft.getBlockAccess();
    }

    @Override
    public IListedSpacecraft toListedCraft() {
        return new EntityListedSpacecraft(spacecraft);
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

    public ISatellite toOrbiting() {
        return new OrbitingSatellite(this.getWorld(), this.name, this.getId(), this.getPosition(), this.spacecraft.getBlockAccess());
    }
}
