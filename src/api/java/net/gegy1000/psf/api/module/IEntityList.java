package net.gegy1000.psf.api.module;

import net.minecraft.entity.EntityLivingBase;

import java.util.Collection;

public interface IEntityList extends IModuleData {
    Collection<EntityLivingBase> getEntities();
}
