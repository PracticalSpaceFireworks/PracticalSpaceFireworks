package net.gegy1000.psf.client.render.laser;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import it.unimi.dsi.fastutil.longs.Long2IntArrayMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.sound.PSFSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID, value = Side.CLIENT)
public class LaserRenderer {

    public enum LaserState {

        CHARGING,
        FIRING,
        COMPLETE,;

    }
    
    private static final Random rand = new Random();

    private static Map<BlockPos, LaserState> lasers = new HashMap<>();
    private static Long2IntMap focus = new Long2IntArrayMap();
    
    @SubscribeEvent
    public static void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
        lasers.clear();
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        World world = Minecraft.getMinecraft().world;
        EntityPlayer player = Minecraft.getMinecraft().player;

        if (world == null || player == null)
            return;

        BufferBuilder buf = Tessellator.getInstance().getBuffer();

        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableLighting();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        
        GlStateManager.translate(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);
        
        for (val e : lasers.entrySet()) {
            BlockPos p = e.getKey();
            GlStateManager.pushMatrix();
            {
                GlStateManager.translate(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                
                float f = (focus.get(p.toLong()) + event.getPartialTicks()) / 80f;
                float radiusMod = e.getValue() == LaserState.FIRING ? (1 - f + 0.5f) * 2 : 1;
                float alphaMod = e.getValue() == LaserState.FIRING ? MathHelper.clamp(f, 0, 1) : 1;

                GlStateManager.pushMatrix();
                {
                    GlStateManager.rotate((world.getTotalWorldTime() + event.getPartialTicks()) * 2, 0, 1, 0);
                    GlStateManager.translate(-0.5, -0.5, -0.5);

                    drawBox(buf, 512, (4 / 16f) * radiusMod, e.getValue() == LaserState.CHARGING ? 0.2f : 0.5f * alphaMod);
                }
                GlStateManager.popMatrix();
                if (e.getValue() == LaserState.FIRING) {
                    GlStateManager.pushMatrix();
                    {
                        GlStateManager.rotate(-(world.getTotalWorldTime() + event.getPartialTicks()) * 2, 0, 1, 0);
                        GlStateManager.translate(-0.5, -0.5, -0.5);

                        drawBox(buf, 512, (8 / 16f) * radiusMod, 0.75f * alphaMod);
                    }
                    GlStateManager.popMatrix();
                }
            }
            GlStateManager.popMatrix();
        }

        buf.setTranslation(0, 0, 0);

        GlStateManager.enableTexture2D();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);

        GlStateManager.popMatrix();
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.END) {
            for (val e : lasers.entrySet()) {
                if (e.getValue() == LaserState.FIRING) {
                    BlockPos p = e.getKey();
                    int max = rand.nextInt(5);
                    for (int i = 0; i < max; i++) {
                        double px = (p.getX() + 0.5 + (rand.nextGaussian() * 2));
                        double py = p.getY() + 1;
                        double pz = (p.getZ() + 0.5 + (rand.nextGaussian() * 2));
                        Minecraft.getMinecraft().world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, 0, 0, 0);
                    }
                }
            }
            for (long l : focus.keySet().toLongArray()) {
                focus.put(l, focus.get(l) + 1);
            }
        }
    }
    
    private static void drawBox(BufferBuilder buf, float height, float width, float a) {
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        float w = width / 2;
        float c = 0.5f;
        float min = c - w;
        float max = c + w;
        buf.pos(min, 0, min).color(0, 1, 1, a).endVertex();
        buf.pos(min, 0, max).color(0, 1, 1, a).endVertex();
        buf.pos(min, height, max).color(0, 1, 1, a).endVertex();
        buf.pos(min, height, min).color(0, 1, 1, a).endVertex();
        
        buf.pos(max, 0, min).color(0, 1, 1, a).endVertex();
        buf.pos(max, height, min).color(0, 1, 1, a).endVertex();
        buf.pos(max, height, max).color(0, 1, 1, a).endVertex();
        buf.pos(max, 0, max).color(0, 1, 1, a).endVertex();
        
        buf.pos(min, 0, min).color(0, 1, 1, a).endVertex();
        buf.pos(min, height, min).color(0, 1, 1, a).endVertex();
        buf.pos(max, height, min).color(0, 1, 1, a).endVertex();
        buf.pos(max, 0, min).color(0, 1, 1, a).endVertex();
        
        buf.pos(min, 0, max).color(0, 1, 1, a).endVertex();
        buf.pos(max, 0, max).color(0, 1, 1, a).endVertex();
        buf.pos(max, height, max).color(0, 1, 1, a).endVertex();
        buf.pos(min, height, max).color(0, 1, 1, a).endVertex();
        
        Tessellator.getInstance().draw();
    }

    private static double lerp(double x1, double x2, float t) {
        return x1 + t * MathHelper.wrapDegrees(x2 - x1);
    }

    public static void updateLaser(BlockPos pos, LaserState state) {
        if (state != LaserState.COMPLETE) {
            lasers.put(pos, state);
            if (state == LaserState.FIRING) {
                focus.put(pos.toLong(), 0);
                Minecraft.getMinecraft().world.playSound(Minecraft.getMinecraft().player, pos, PSFSounds.LASER_FIRE, SoundCategory.BLOCKS, 4, 1);
            }
        } else {
            lasers.remove(pos);
            focus.remove(pos.toLong());
        }
    }
}
