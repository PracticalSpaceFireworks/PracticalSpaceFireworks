package net.gegy1000.psf.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleRocketPlume extends Particle {
    private static final int BASE_LIFETIME = 7 * 20;

    private final float scale;

    private final float baseYellow;
    private final float baseBrightness;

    public ParticleRocketPlume(World world, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        super(world, posX, posY, posZ, motionX, motionY, motionZ);
        this.canCollide = true;

        this.scale = (float) (Math.random() * 0.5F + 5.5F);
        this.particleMaxAge = BASE_LIFETIME + world.rand.nextInt(10);

        this.baseYellow = (float) (Math.random() * 0.3 + 0.7);
        this.baseBrightness = (float) (Math.random() * 0.2 + 0.8);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.move(this.motionX, this.motionY, this.motionZ);

        this.motionY *= 0.9999F;

        if (this.particleAge > BASE_LIFETIME * 0.7) {
            this.canCollide = false;
        }

        if (this.onGround) {
            this.particleAge *= 4;
            this.canCollide = false;
            this.motionY = -this.motionY * 0.5F;
            this.motionX += (Math.random() - 0.5) * 0.5;
            this.motionZ += (Math.random() - 0.5) * 0.5;
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float age = Math.min((this.particleAge + partialTicks) / this.particleMaxAge, 1.0F);
        this.particleScale = this.scale + age * 1.5F;

        float colorTransition = Math.min(age * 1.5F, 1.0F);
        this.particleRed = lerp(1.0F, 0.83F, colorTransition) * this.baseBrightness;
        this.particleGreen = lerp(this.baseYellow * 0.9F, 0.83F, colorTransition) * this.baseBrightness;
        this.particleBlue = lerp(0.0F, 0.83F, colorTransition) * this.baseBrightness;

        super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        float age = Math.min((this.particleAge + partialTicks) / this.particleMaxAge, 1.0F);
        float colorTransition = Math.min(age * 1.5F, 1.0F);

        int brightness = super.getBrightnessForRender(partialTicks);
        int blockLight = brightness & 0xFF;
        int skyLight = brightness >> 16 & 0xFF;
        blockLight = (int) (blockLight + (1.0F - colorTransition) * 255);

        if (blockLight > 240) {
            blockLight = 240;
        }

        return blockLight | skyLight << 16;
    }

    private float lerp(float curr, float target, float intermediate) {
        return curr + (target - curr) * intermediate;
    }
}
