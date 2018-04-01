package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.data.IEntityList;
import net.gegy1000.psf.api.data.IModuleData;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Set;

@ParametersAreNonnullByDefault
public class ModuleEntityMarker extends ConnectableModule {
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
    protected <T extends IModuleData> boolean canConnect(Capability<T> capability, Set<IModule> connected) {
        return capability == CapabilityModuleData.ENTITY_LIST;
    }

    @Override
    public int getTickInterval() {
        return 100;
    }
}
