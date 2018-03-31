package net.gegy1000.psf.client.render.spacecraft;

import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;

public class RenderSpacecraft extends Render<EntitySpacecraft> {
    private static final Minecraft MC = Minecraft.getMinecraft();

    public RenderSpacecraft(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySpacecraft entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.bindEntityTexture(entity);

        SpacecraftModel model = entity.model;
        if (model == null || !model.isAvailable()) {
            entity.model = model = SpacecraftModel.build(entity.getBlockAccess());
        }

        float lerpYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float lerpPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180.0F - lerpYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(lerpPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.5, 0.0, -0.5);

        GlStateManager.disableLighting();
        this.renderLayers(model);
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    private void renderLayers(SpacecraftModel model) {
        model.render(BlockRenderLayer.SOLID);

        GlStateManager.enableAlpha();
        model.render(BlockRenderLayer.CUTOUT_MIPPED);
        MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        model.render(BlockRenderLayer.CUTOUT);
        MC.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

        GlStateManager.enableBlend();
        model.render(BlockRenderLayer.TRANSLUCENT);
        GlStateManager.disableBlend();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpacecraft entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
