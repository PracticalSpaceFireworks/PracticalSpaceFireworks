package net.gegy1000.psf.server.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.ISatellite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

public class ModuleEntityDetector extends EmptyModule{

    @RequiredArgsConstructor
    public enum EntityDetectorTier implements IStringSerializable {
        SIMPLE("simple", 1),
        ADVANCED("advanced", 5),
        CUSTOM("custom", 0),
        ;

        @Getter
        private final String name;
        @Getter
        private final int range;
    }

    private final int range;

    public ModuleEntityDetector(EntityDetectorTier tier) {
        this(tier, tier.getRange());
    }

    public ModuleEntityDetector(int range) {
        this(EntityDetectorTier.CUSTOM, range);
    }

    public ModuleEntityDetector(EntityDetectorTier tier, int range) {
        super("entity_detector." + tier.getName());
        this.range = range;
    }

    @Override
    public void onSatelliteTick(ISatellite satellite) {
        if(!satellite.getWorld().isRemote) {
            Collection<EntityLivingBase> entities = satellite.getWorld().getEntities(EntityLivingBase.class, e -> !(e instanceof EntityPlayer) && satellite.getWorld().canSeeSky(e.getPosition()) &&
            withinSatelliteRange(satellite, e.getPosition(), satellite.getWorld()));
            for(EntityLivingBase living : entities) {
                if (!living.isPotionActive(MobEffects.GLOWING)) {
                    PotionEffect potioneffect = new PotionEffect(MobEffects.GLOWING, 200, 0, false, false);
                    living.addPotionEffect(potioneffect);
                }
            }
        }
    }

    private boolean withinSatelliteRange(ISatellite satellite, BlockPos pos, World world) {
        BlockPos sat = new BlockPos(satellite.getPosition().getX() >> 4, 0, satellite.getPosition().getZ() >> 4);
        BlockPos entityChunk = new BlockPos(pos.getX() >> 4, 0, pos.getZ() >> 4);
        BlockPos diff = sat.subtract(entityChunk);

        if(Math.abs(diff.getX()) > range || Math.abs(diff.getZ()) > range) {
            return false;
        }

        return true;
    }
}
