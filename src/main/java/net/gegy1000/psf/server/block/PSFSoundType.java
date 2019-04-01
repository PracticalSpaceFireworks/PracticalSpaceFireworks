package net.gegy1000.psf.server.block;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.init.PSFSounds;
import net.minecraft.block.SoundType;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class PSFSoundType extends SoundType {
    public static final SoundType SMALL_DEVICE = new SoundType(
        1.0F, 1.8F,
        SoundEvents.BLOCK_METAL_BREAK,
        SoundEvents.BLOCK_METAL_STEP,
        SoundEvents.BLOCK_METAL_PLACE,
        SoundEvents.BLOCK_METAL_HIT,
        SoundEvents.BLOCK_METAL_FALL
    );

    public static final SoundType SCAFFOLD = new PSFSoundType(
        1.0F, 1.75F,
        () -> PSFSounds.METAL_BREAK,
        () -> PSFSounds.METAL_STEP,
        () -> PSFSounds.METAL_PLACE,
        () -> PSFSounds.METAL_HIT,
        () -> PSFSounds.METAL_FALL
    );

    private final Supplier<SoundEvent> breakSound;
    private final Supplier<SoundEvent> stepSound;
    private final Supplier<SoundEvent> placeSound;
    private final Supplier<SoundEvent> hitSound;
    private final Supplier<SoundEvent> fallSound;

    @SuppressWarnings("ConstantConditions")
    private PSFSoundType(
        float volume, float pitch,
        Supplier<SoundEvent> breakSound,
        Supplier<SoundEvent> stepSound,
        Supplier<SoundEvent> placeSound,
        Supplier<SoundEvent> hitSound,
        Supplier<SoundEvent> fallSound
    ) {
        super(volume, pitch, null, null, null, null, null);
        this.breakSound = breakSound;
        this.stepSound = stepSound;
        this.placeSound = placeSound;
        this.hitSound = hitSound;
        this.fallSound = fallSound;
    }

    @Override
    public SoundEvent getBreakSound() {
        return breakSound.get();
    }

    @Override
    public SoundEvent getStepSound() {
        return stepSound.get();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return placeSound.get();
    }

    @Override
    public SoundEvent getHitSound() {
        return hitSound.get();
    }

    @Override
    public SoundEvent getFallSound() {
        return fallSound.get();
    }
}
