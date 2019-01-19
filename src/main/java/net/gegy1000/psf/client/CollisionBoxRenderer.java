package net.gegy1000.psf.client;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID, value = Side.CLIENT)
public final class CollisionBoxRenderer {
    private CollisionBoxRenderer() {}

    @SubscribeEvent
    static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (!FMLLaunchHandler.isDeobfuscatedEnvironment()) return;
        @Nullable RayTraceResult hit = event.getTarget();
        if (hit == null || Type.BLOCK != hit.typeOfHit) return;
        BlockPos pos = event.getTarget().getBlockPos();
        List<AxisAlignedBB> collisionBoxes = new ArrayList<>();
        EntityPlayer player = event.getPlayer();
        float partialTicks = event.getPartialTicks();
        AxisAlignedBB entityBox = player.getEntityBoundingBox().grow(6.0D);
        World world = player.getEntityWorld();
        IBlockState state = world.getBlockState(pos);
        double offsetX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double offsetY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double offsetZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        AxisAlignedBB selectedBox = state.getSelectedBoundingBox(world, pos);
        boolean checkedSelectedBox = false;
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
            if (!checkedSelectedBox && box.equals(selectedBox)) {
                checkedSelectedBox = true;
                event.setCanceled(true);
            }
            double minX = (box.minX - 0.002) - offsetX;
            double minY = (box.minY - 0.002) - offsetY;
            double minZ = (box.minZ - 0.002) - offsetZ;
            double maxX = (box.maxX + 0.002) - offsetX;
            double maxY = (box.maxY + 0.002) - offsetY;
            double maxZ = (box.maxZ + 0.002) - offsetZ;
            RenderGlobal.drawBoundingBox(builder, minX, minY, minZ, maxX, maxY, maxZ, 0.0F, 1.0F, 0.0F, 1.0F);
        }
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
