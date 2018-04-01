package net.gegy1000.psf.server.modules.data;

import net.gegy1000.psf.api.data.IEntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityListData implements IEntityList {
    private final List<EntityLivingBase> entities = new ArrayList<>();

    public void updateEntities(Collection<EntityLivingBase> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
    }

    @Override
    public Collection<EntityLivingBase> getEntities() {
        return this.entities;
    }
}
