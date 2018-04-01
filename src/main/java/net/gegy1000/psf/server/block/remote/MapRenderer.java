package net.gegy1000.psf.server.block.remote;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.data.IScannedChunk;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class MapRenderer extends Gui {
    private static final VertexFormat POSITION_COLOR_NORMAL = new VertexFormat();

    static {
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.POSITION_3F);
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.COLOR_4UB);
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.NORMAL_3B);
        POSITION_COLOR_NORMAL.addElement(DefaultVertexFormats.PADDING_1B);
    }

    private static final EnumFacing[] RENDER_FACES = new EnumFacing[] { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST };

    private final ITerrainScan terrainScan;
    private final List<ChunkRenderer> chunkRenderers;

    private final ExecutorService chunkBuildService = Executors.newSingleThreadExecutor();

    public MapRenderer(ITerrainScan terrainScan) {
        this.terrainScan = terrainScan;

        List<IScannedChunk> chunks = new ArrayList<>(terrainScan.getChunks());
        chunks.sort(Comparator.comparingInt(value -> {
            ChunkPos chunkPos = value.getChunkPos();
            return (chunkPos.x * chunkPos.x) + (chunkPos.z * chunkPos.z);
        }));
        this.chunkRenderers = chunks.stream().map(ChunkRenderer::new).collect(Collectors.toList());
    }

    public void performUploads() {
        this.chunkRenderers.forEach(ChunkRenderer::performUploads);
    }

    public void render() {
        this.chunkRenderers.forEach(ChunkRenderer::render);
    }

    public void delete() {
        this.chunkBuildService.shutdownNow();
        this.chunkRenderers.forEach(ChunkRenderer::delete);
    }

    @Override
    protected void finalize() throws Throwable {
        this.delete();
        super.finalize();
    }

    private class ChunkRenderer {
        private final IScannedChunk scannedChunk;
        private final int globalX;
        private final int globalZ;

        private Future<BufferBuilder> builtMesh;
        private int displayList = -1;

        private ChunkRenderer(IScannedChunk scannedChunk) {
            this.scannedChunk = scannedChunk;
            this.globalX = scannedChunk.getChunkPos().getXStart();
            this.globalZ = scannedChunk.getChunkPos().getZStart();

            this.builtMesh = MapRenderer.this.chunkBuildService.submit(() -> {
                BufferBuilder builder = new BufferBuilder(0x8000);
                this.buildMesh(builder);
                return builder;
            });
        }

        private void performUploads() {
            if (this.builtMesh != null && this.builtMesh.isDone()) {
                try {
                    BufferBuilder builder = this.builtMesh.get();
                    if (builder != null) {
                        int id = GLAllocation.generateDisplayLists(1);

                        builder.finishDrawing();
                        GlStateManager.glNewList(id, GL11.GL_COMPILE);
                        new WorldVertexBufferUploader().draw(builder);
                        GlStateManager.glEndList();

                        this.displayList = id;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    PracticalSpaceFireworks.LOGGER.error("Failed to retrieve built chunk mesh", e);
                }
                this.builtMesh = null;
            }
        }

        private void render() {
            if (this.displayList != -1) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(this.globalX, 0, this.globalZ);
                GlStateManager.callList(this.displayList);
                GlStateManager.popMatrix();
            }
        }

        private void delete() {
            if (this.displayList != -1) {
                GLAllocation.deleteDisplayLists(this.displayList);
            }
        }

        private void buildMesh(BufferBuilder builder) {
            builder.begin(GL11.GL_QUADS, POSITION_COLOR_NORMAL);

            IScannedChunk scannedChunk = this.scannedChunk;
            for (BlockPos pos : BlockPos.getAllInBoxMutable(0, 0, 0, 15, 255, 15)) {
                MapColor mapColor = scannedChunk.getMapColor(pos.getX(), pos.getY(), pos.getZ());
                if (mapColor != null) {
                    List<EnumFacing> faces = Arrays.stream(RENDER_FACES)
                            .filter(face -> {
                                BlockPos offset = pos.offset(face);
                                return this.getMapColor(offset.getX(), offset.getY(), offset.getZ()) == null;
                            })
                            .collect(Collectors.toList());

                    if (!faces.isEmpty()) {
                        int color = mapColor.colorValue;
                        int red = (color >> 16) & 0xFF;
                        int green = (color >> 8) & 0xFF;
                        int blue = color & 0xFF;

                        builder.setTranslation(pos.getX(), pos.getY(), pos.getZ());

                        for (EnumFacing face : faces) {
                            this.buildFace(builder, face, red, green, blue);
                        }
                    }
                }
            }

            builder.setTranslation(0.0, 0.0, 0.0);
        }

        private MapColor getMapColor(int x, int y, int z) {
            if (x < 0 || y < 0 || z < 0 || x >= 16 || y >= 256 || z >= 16) {
                return MapRenderer.this.terrainScan.getMapColor(x + this.globalX, y, z + this.globalZ);
            } else {
                return this.scannedChunk.getMapColor(x, y, z);
            }
        }

        private void buildFace(BufferBuilder builder, EnumFacing facing, int red, int green, int blue) {
            switch (facing) {
                case NORTH:
                    builder.pos(0.0F, 0.0F, 0.0F).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
                    builder.pos(0.0F, 1.0F, 0.0F).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
                    builder.pos(1.0F, 1.0F, 0.0F).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
                    builder.pos(1.0F, 0.0F, 0.0F).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
                    break;
                case SOUTH:
                    builder.pos(0.0F, 0.0F, 1.0F).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
                    builder.pos(1.0F, 0.0F, 1.0F).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
                    builder.pos(1.0F, 1.0F, 1.0F).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
                    builder.pos(0.0F, 1.0F, 1.0F).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
                    break;
                case WEST:
                    builder.pos(0.0F, 0.0F, 0.0F).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex();
                    builder.pos(0.0F, 0.0F, 1.0F).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex();
                    builder.pos(0.0F, 1.0F, 1.0F).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex();
                    builder.pos(0.0F, 1.0F, 0.0F).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex();
                    break;
                case EAST:
                    builder.pos(1.0F, 1.0F, 0.0F).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
                    builder.pos(1.0F, 1.0F, 1.0F).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
                    builder.pos(1.0F, 0.0F, 1.0F).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
                    builder.pos(1.0F, 0.0F, 0.0F).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
                    break;
                case UP:
                    builder.pos(0.0F, 1.0F, 1.0F).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                    builder.pos(1.0F, 1.0F, 1.0F).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                    builder.pos(1.0F, 1.0F, 0.0F).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                    builder.pos(0.0F, 1.0F, 0.0F).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                    break;
            }
        }
    }
}
