package net.gegy1000.psf.client.render.spacecraft.model;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBodyData;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
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

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface SpacecraftModel {
    BlockRendererDispatcher BLOCK_RENDERER = Minecraft.getMinecraft().getBlockRendererDispatcher();
    BufferBuilder BUILDER = new BufferBuilder(0x20000);

    @SideOnly(Side.CLIENT)
    static SpacecraftModel build(SpacecraftBodyData body) {
        if (Minecraft.getMinecraft().gameSettings.useVbo) {
            return new VboSpacecraftModel(body);
        } else {
            return new DisplayListSpacecraftModel(body);
        }
    }

    default void drawBlocks(SpacecraftBodyData body, BlockRenderLayer layer, BufferBuilder builder) {
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        // TODO: parent of bodydata is bad
        DelegatedWorld world = body.buildWorld(Minecraft.getMinecraft().world);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(body.getMinPos(), body.getMaxPos())) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() != Blocks.AIR && state.getBlock().canRenderInLayer(state, layer)) {
                BLOCK_RENDERER.renderBlock(state, pos, world, builder);
            }
        }

        builder.finishDrawing();
    }

    void render(BlockRenderLayer layer);

    void delete();

    boolean isAvailable();

    SpacecraftBodyData getBody();
}
