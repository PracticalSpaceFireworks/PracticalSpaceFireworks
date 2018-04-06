package net.gegy1000.psf.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public enum PSFParticles {
    ROCKET_PLUME((particleID, world, posX, posY, posZ, motionX, motionY, motionZ, parameters) -> {
        return new ParticleRocketPlume(world, posX, posY, posZ, motionX, motionY, motionZ);
    });

    IParticleFactory factory;

    PSFParticles(IParticleFactory factory) {
        this.factory = factory;
    }

    public Particle create(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int... parameters) {
        return this.factory.createParticle(-1, world, x, y, z, motionX, motionY, motionZ, parameters);
    }

    public void spawn(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int... parameters) {
        double distance = Minecraft.getMinecraft().player.getDistanceSq(x, y, z);
        if (distance < 128 * 128) {
            Particle particle = this.create(world, x, y, z, motionX, motionY, motionZ, parameters);
            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
        }
    }
}
