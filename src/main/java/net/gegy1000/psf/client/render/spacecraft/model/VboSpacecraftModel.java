package net.gegy1000.psf.client.render.spacecraft.model;

import lombok.Getter;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftWorldHandler;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VboSpacecraftModel implements SpacecraftModel {
    @Getter
    private final DelegatedWorld renderWorld;
    @Getter
    private final SpacecraftWorldHandler worldHandler;

    private final Map<BlockRenderLayer, VertexBuffer> buffers = new EnumMap<>(BlockRenderLayer.class);

    private boolean available = true;

    VboSpacecraftModel(DelegatedWorld world, SpacecraftWorldHandler worldHandler) {
        this.renderWorld = world;
        this.worldHandler = worldHandler;

        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            VertexBuffer buffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
            buffer.bindBuffer();
            this.bindAttributes();

            drawBlocks(world, worldHandler, layer, BUILDER);
            buffer.bufferData(BUILDER.getByteBuffer());

            buffer.unbindBuffer();

            this.buffers.put(layer, buffer);
        }
    }

    @Override
    public void render(BlockRenderLayer layer) {
        if (!this.available) {
            throw new IllegalStateException("Cannot render spacecraft after VBO has been deleted");
        }

        VertexBuffer buffer = this.buffers.get(layer);

        this.enableRenderState();

        buffer.bindBuffer();
        this.bindAttributes();
        buffer.drawArrays(GL11.GL_QUADS);
        buffer.unbindBuffer();

        this.disableRenderState();
    }

    @Override
    public void delete() {
        this.available = false;
        this.buffers.values().forEach(VertexBuffer::deleteGlBuffers);
    }

    @Override
    public boolean isAvailable() {
        return this.available;
    }

    private void bindAttributes() {
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private void enableRenderState() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
    }

    private void disableRenderState() {
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
    }

    @Override
    protected void finalize() {
        if (this.available) {
            this.delete();
        }
    }
}
