package net.gegy1000.psf.client.sound;

import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.init.PSFSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class SpacecraftSound extends MovingSound {

    private static final float MAX_SOUND_DIST = 128;
    private static final float MAX_SOUND_DIST_SQ = MAX_SOUND_DIST * MAX_SOUND_DIST;
    
    private final EntitySpacecraft entity;
    
    private float startVol = -1;
    private int fadeOut = 20;

    public SpacecraftSound(EntitySpacecraft entity) {
        super(PSFSounds.SPACECRAFT_LAUNCH, SoundCategory.BLOCKS);
        this.entity = entity;
        this.attenuationType = AttenuationType.NONE;
    }

    @Override
    public void update() {
        if (entity.isDead || entity.getState().getType() != EntitySpacecraft.StateType.LAUNCH) {
            if (--fadeOut == 0) {
                this.donePlaying = true;
            } else {
                if (startVol == -1) {
                    startVol = this.volume;
                }
                this.volume = startVol * (fadeOut / 20F);
            }
        } else {
            this.xPosF = (float) entity.posX;
            this.yPosF = (float) entity.posY;
            this.zPosF = (float) entity.posZ;

            Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
            if (viewEntity == null) {
                return;
            }
            float distSq = (float) viewEntity.getDistanceSq(entity.posX, entity.posY / 8, entity.posZ);
            if (distSq > MAX_SOUND_DIST_SQ) {
                float dist = (float) Math.sqrt(distSq);
                this.volume = MathHelper.clamp((1 - ((dist - MAX_SOUND_DIST) / 64)), 0, 1);
            }
        }
    }
}
