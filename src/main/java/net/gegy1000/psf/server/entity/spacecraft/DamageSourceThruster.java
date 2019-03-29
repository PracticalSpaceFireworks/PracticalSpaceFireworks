package net.gegy1000.psf.server.entity.spacecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class DamageSourceThruster extends DamageSource {
    
    private final EntitySpacecraft cause;
    
    public DamageSourceThruster(EntitySpacecraft cause) {
        super("psf.thruster");
        this.cause = cause;
    }

    @Override
    public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
        return new TextComponentTranslation("death.attack." + getDamageType(), entityLivingBaseIn.getDisplayName(), cause.getSatellite().getName());
    }
}
