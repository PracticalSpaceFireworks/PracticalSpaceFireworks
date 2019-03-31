package net.gegy1000.psf.client.render.spacecraft.model;

import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.EnumMap;
import java.util.Map;

import lombok.Getter;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.api.ISpacecraftBodyData;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DisplayListSpacecraftModel implements SpacecraftModel {
    @Getter
    private final ISpacecraftBodyData body;

    private final Map<BlockRenderLayer, Integer> lists = new EnumMap<>(BlockRenderLayer.class);
    private boolean available = true;

    DisplayListSpacecraftModel(ISpacecraftBodyData body) {
        this.body = body;

        BlockRenderLayer original = MinecraftForgeClient.getRenderLayer();
        for (BlockRenderLayer layer : BlockRenderLayer.values()) {
            ForgeHooksClient.setRenderLayer(layer);
            drawBlocks(body, layer, BUILDER);

            int id = GLAllocation.generateDisplayLists(1);

            GlStateManager.glNewList(id, GL11.GL_COMPILE);
            new WorldVertexBufferUploader().draw(BUILDER);
            GlStateManager.glEndList();

            this.lists.put(layer, id);
        }
        ForgeHooksClient.setRenderLayer(original);
    }

    @Override
    public void render(BlockRenderLayer layer) {
        if (!this.available) {
            throw new IllegalStateException("Cannot render spacecraft after VBO has been deleted");
        }

        int list = this.lists.get(layer);
        GlStateManager.callList(list);
    }

    @Override
    public void delete() {
        this.available = false;
        this.lists.values().forEach(GLAllocation::deleteDisplayLists);
    }

    @Override
    public boolean isAvailable() {
        return this.available;
    }

    @Override
    protected void finalize() {
        if (this.available) {
            this.delete();
        }
    }
}
