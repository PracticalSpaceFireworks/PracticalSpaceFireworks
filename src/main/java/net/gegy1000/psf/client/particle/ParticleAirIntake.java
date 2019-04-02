package net.gegy1000.psf.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleAirIntake extends Particle {
    private final float baseScale;
    private final double targetX;
    private final double targetY;
    private final double targetZ;

    protected ParticleAirIntake(World world, double targetX, double targetY, double targetZ, double deltaX, double deltaY, double deltaZ) {
        super(world, targetX, targetY, targetZ, deltaX, deltaY, deltaZ);

        this.motionX = deltaX;
        this.motionY = deltaY;
        this.motionZ = deltaZ;
        this.posX = targetX;
        this.posY = targetY;
        this.posZ = targetZ;
        this.targetX = this.posX;
        this.targetY = this.posY;
        this.targetZ = this.posZ;

        this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F;
        this.baseScale = this.particleScale;

        this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.4F + 0.6F;
        this.particleMaxAge = (int) (Math.random() * 10.0D) + 40;
        this.setParticleTextureIndex((int) (Math.random() * 8.0D));

        this.canCollide = false;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = (this.particleAge + partialTicks) / this.particleMaxAge;
        f = 1.0F - f;
        f = f * f;
        f = 1.0F - f;
        this.particleScale = this.baseScale * f;
        super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        float age = (float) this.particleAge / this.particleMaxAge;
        float progress = 1.0F + age - age * age * 2.0F;

        this.posX = this.targetX + this.motionX * progress;
        this.posY = this.targetY + this.motionY * progress;
        this.posZ = this.targetZ + this.motionZ * progress;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
    }
}
