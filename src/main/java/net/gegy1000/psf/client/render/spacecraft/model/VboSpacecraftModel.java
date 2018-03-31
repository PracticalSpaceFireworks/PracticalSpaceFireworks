package net.gegy1000.psf.client.render.spacecraft.model;

import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

public class VboSpacecraftModel implements SpacecraftModel {
    private static final BlockRendererDispatcher BLOCK_RENDERER = Minecraft.getMinecraft().getBlockRendererDispatcher();
    private static final BufferBuilder BUILDER = new BufferBuilder(0x20000);

    @Getter
    private final SpacecraftBlockAccess renderWorld;
    private final Map<BlockRenderLayer, VertexBuffer> buffers = new EnumMap<>(BlockRenderLayer.class);

    private boolean available = true;

    VboSpacecraftModel(SpacecraftBlockAccess blockAccess) {
        this.renderWorld = blockAccess;
        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            VertexBuffer buffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
            buffer.bindBuffer();
            this.bindAttributes();

            BufferBuilder builder = BUILDER;

            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

            for (BlockPos pos : BlockPos.getAllInBoxMutable(blockAccess.getMinPos(), blockAccess.getMaxPos())) {
                IBlockState state = blockAccess.getBlockState(pos);
                if (state.getBlock() != Blocks.AIR && state.getBlock().canRenderInLayer(state, layer)) {
                    BLOCK_RENDERER.renderBlock(state, pos, blockAccess, builder);
                }
            }

            builder.finishDrawing();
            buffer.bufferData(builder.getByteBuffer());

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
