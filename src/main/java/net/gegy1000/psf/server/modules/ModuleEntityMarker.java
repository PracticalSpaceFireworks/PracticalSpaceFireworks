package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.data.IEntityList;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class ModuleEntityMarker extends EmptyModule {
    public ModuleEntityMarker() {
        super("entity_marker");
    }

    @Override
    public void onSatelliteTick(ISatellite satellite) {
        Collection<IEntityList> entityLists = satellite.getModuleCaps(CapabilityModuleData.ENTITY_LIST);
        for (IEntityList entityList : entityLists) {
            for (EntityLivingBase living : entityList.getEntities()) {
                if (!living.isPotionActive(MobEffects.GLOWING)) {
                    if (satellite.tryExtractEnergy(1000)) {
                        living.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 200, 0, false, false));
                    }
                }
            }
        }
    }

    @Override
    public int getTickInterval() {
        return 100;
    }
}
