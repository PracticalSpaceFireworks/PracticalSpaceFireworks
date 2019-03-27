package net.gegy1000.psf.server.block;

import net.gegy1000.psf.server.sound.PSFSounds;
import net.minecraft.block.SoundType;
import net.minecraft.util.SoundEvent;

public final class PSFSoundType extends SoundType {
    public static final SoundType SOLAR_PANEL = new PSFSoundType(1.0F, 1.8F, SoundType.METAL);

    public static final SoundType METAL = new PSFSoundType(
        1.0F, 1.75F,
        PSFSounds.METAL_BREAK,
        PSFSounds.METAL_STEP,
        PSFSounds.METAL_PLACE,
        PSFSounds.METAL_HIT,
        PSFSounds.METAL_FALL
    );

    private PSFSoundType(float volume, float pitch, SoundEvent breakSound, SoundEvent stepSound, SoundEvent placeSound, SoundEvent hitSound, SoundEvent fallSound) {
        super(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
    }

    private PSFSoundType(float volume, float pitch, final SoundType type) {
        this(volume, pitch, type.getBreakSound(), type.getStepSound(), type.getPlaceSound(), type.getHitSound(), type.getFallSound());
    }
}
