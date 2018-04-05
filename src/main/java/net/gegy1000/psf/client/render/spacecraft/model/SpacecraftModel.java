package net.gegy1000.psf.client.render.spacecraft.model;

import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public interface SpacecraftModel {
    BlockRendererDispatcher BLOCK_RENDERER = Minecraft.getMinecraft().getBlockRendererDispatcher();
    BufferBuilder BUILDER = new BufferBuilder(0x20000);

    @SideOnly(Side.CLIENT)
    static SpacecraftModel build(SpacecraftBlockAccess blockAccess) {
        if (Minecraft.getMinecraft().gameSettings.useVbo) {
            return new VboSpacecraftModel(blockAccess);
        } else {
            return new DisplayListSpacecraftModel(blockAccess);
        }
    }

    default void drawBlocks(SpacecraftBlockAccess blockAccess, BlockRenderLayer layer, BufferBuilder builder) {
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (BlockPos pos : BlockPos.getAllInBoxMutable(blockAccess.getMinPos(), blockAccess.getMaxPos())) {
            IBlockState state = blockAccess.getBlockState(pos);
            if (state.getBlock() != Blocks.AIR && state.getBlock().canRenderInLayer(state, layer)) {
                BLOCK_RENDERER.renderBlock(state, pos, blockAccess, builder);
            }
        }

        builder.finishDrawing();
    }

    void render(BlockRenderLayer layer);

    void delete();

    boolean isAvailable();

    SpacecraftBlockAccess getRenderWorld();
}
