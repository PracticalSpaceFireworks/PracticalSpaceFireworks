package net.gegy1000.psf.client;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static double shakeX, shakeY, shakeZ;
    private static double lastShakeX, lastShakeY, lastShakeZ;

    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event) {
        if (event.getTarget() instanceof EntitySpacecraft) {
            EntitySpacecraft spacecraft = (EntitySpacecraft) event.getTarget();
            if (spacecraft.model != null) {
                spacecraft.model.delete();
                spacecraft.model = null;
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && MC.player != null) {
            lastShakeX = shakeX;
            lastShakeY = shakeY;
            lastShakeZ = shakeZ;

            double totalShake = 0.0;

            List<EntitySpacecraft> entities = MC.world.getEntities(EntitySpacecraft.class, s -> true);
            for (EntitySpacecraft spacecraft : entities) {
                double shake = spacecraft.getState().getCameraShake();
                double distance = MathHelper.clamp(spacecraft.getDistance(MC.player), 10.0, 400.0);
                totalShake += shake * (1.0 - distance / 400.0);
            }

            if (!MC.player.onGround && !MC.player.collidedVertically && !MC.player.collidedHorizontally) {
                totalShake *= 0.5;
            }

            if (totalShake > 1e-3) {
                totalShake = Math.min(totalShake, 0.25);

                Random rand = MC.world.rand;

                shakeX = (rand.nextDouble() * 2.0 - 1.0) * totalShake;
                shakeY = (rand.nextDouble() * 2.0 - 1.0) * totalShake;
                shakeZ = (rand.nextDouble() * 2.0 - 1.0) * totalShake;
            }
        }
    }

    @SubscribeEvent
    public static void onSetupCamera(EntityViewRenderEvent.CameraSetup setup) {
        double lerpShakeX = lastShakeX + (shakeX - lastShakeX) * MC.getRenderPartialTicks();
        double lerpShakeY = lastShakeY + (shakeY - lastShakeY) * MC.getRenderPartialTicks();
        double lerpShakeZ = lastShakeZ + (shakeZ - lastShakeZ) * MC.getRenderPartialTicks();
        GlStateManager.translate(lerpShakeX, lerpShakeY, lerpShakeZ);
    }
}
