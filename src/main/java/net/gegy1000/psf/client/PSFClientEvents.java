package net.gegy1000.psf.client;

import com.google.common.base.Predicates;
import lombok.Getter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.module.BlockStrutAbstract;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID, value = Side.CLIENT)
public final class PSFClientEvents {
    private static final Minecraft CLIENT = Minecraft.getMinecraft();

    private static double shakeX, shakeY, shakeZ;
    private static double lastShakeX, lastShakeY, lastShakeZ;

    @Getter
    private static int elapsedTicks;

    private PSFClientEvents() {
      throw new UnsupportedOperationException();
    }

    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event) {
        Entity target = event.getTarget();

        if (target instanceof EntitySpacecraft) {
            EntitySpacecraft sc = (EntitySpacecraft) target;

            if (sc.model != null) {
                sc.model.delete();
                sc.model = null;
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        @Nullable EntityPlayer player = CLIENT.player;
        
        if (event.phase != TickEvent.Phase.END || player == null) {
            return;
        }

        elapsedTicks++;
        lastShakeX = shakeX;
        lastShakeY = shakeY;
        lastShakeZ = shakeZ;

        if (CLIENT.isGamePaused()) {
            return;
        }

        World world = CLIENT.world;
        double totalShake = 0.0;

        for (EntitySpacecraft sc : world.getEntities(EntitySpacecraft.class, Predicates.alwaysTrue())) {
            double shake = sc.getState().getCameraShake();
            double dist = MathHelper.clamp(sc.getDistance(player), 10.0, 400.0);

            totalShake += shake * (1.0 - dist / 400.0);
        }

        if (!player.onGround && !player.collidedVertically && !player.collidedHorizontally) {
            totalShake *= 0.5;
        }

        if (totalShake > 1e-3) {
            totalShake = Math.min(totalShake, 0.25);

            Random rand = world.rand;

            shakeX = (rand.nextDouble() * 2.0 - 1.0) * totalShake;
            shakeY = (rand.nextDouble() * 2.0 - 1.0) * totalShake;
            shakeZ = (rand.nextDouble() * 2.0 - 1.0) * totalShake;
        }
    }

    @SubscribeEvent
    public static void onSetupCamera(EntityViewRenderEvent.CameraSetup event) {
        double lerpShakeX = lastShakeX + (shakeX - lastShakeX) * CLIENT.getRenderPartialTicks();
        double lerpShakeY = lastShakeY + (shakeY - lastShakeY) * CLIENT.getRenderPartialTicks();
        double lerpShakeZ = lastShakeZ + (shakeZ - lastShakeZ) * CLIENT.getRenderPartialTicks();

        GlStateManager.translate(lerpShakeX, lerpShakeY, lerpShakeZ);
    }

    @SubscribeEvent
    public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (!FMLLaunchHandler.isDeobfuscatedEnvironment()) return;
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) return;

        @Nullable RayTraceResult hit = event.getTarget();

        if (hit == null || RayTraceResult.Type.BLOCK != hit.typeOfHit) return;

        BlockPos pos = event.getTarget().getBlockPos();
        EntityPlayer player = event.getPlayer();
        float partialTicks = event.getPartialTicks();
        World world = player.getEntityWorld();
        IBlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof BlockStrutAbstract)) return;

        double offsetX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double offsetY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double offsetZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        AxisAlignedBB entityBox = player.getEntityBoundingBox().grow(6.0D);
        List<AxisAlignedBB> collisionBoxes = new ArrayList<>();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
            SourceFactor.ONE, DestFactor.ZERO
        );
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        state.addCollisionBoxToList(world, pos, entityBox, collisionBoxes, event.getPlayer(), false);
        builder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (@Nullable AxisAlignedBB box : collisionBoxes) {
            if (box == null) continue;
            double minX = box.minX - offsetX;
            double minY = box.minY - offsetY;
            double minZ = box.minZ - offsetZ;
            double maxX = box.maxX - offsetX;
            double maxY = box.maxY - offsetY;
            double maxZ = box.maxZ - offsetZ;
            RenderGlobal.drawBoundingBox(builder, minX, minY, minZ, maxX, maxY, maxZ, 0.0F, 1.0F, 0.0F, 1.0F);
        }

        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
