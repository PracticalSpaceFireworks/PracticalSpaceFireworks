package net.gegy1000.psf.server.block.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import net.gegy1000.psf.api.data.IModuleDataDisplay;
import net.gegy1000.psf.api.data.IScannedChunk;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.gegy1000.psf.server.block.remote.MapRenderer;
import net.gegy1000.psf.server.modules.data.CompositeTerrainScan;
import net.gegy1000.psf.server.modules.data.TerrainScanData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

@NoArgsConstructor
public class ModuleDisplayMap implements IModuleDataDisplay {
    
    @Delegate
    private CompositeTerrainScan scan = new CompositeTerrainScan(new ArrayList<>());
    
    private MapRenderer mapRenderer;
    
    private int lastMouseDragX, lastMouseDragY;
    private int prevOriginX, prevOriginZ;
    private int originX, originZ;

    @Override
    public void draw(int x, int y, int width, int height, float partialTicks) {
    	System.out.println(originX + " " + originZ);
        if (mapRenderer == null || mapRenderer.shouldUpdate(scan)) {
            if (mapRenderer != null) {
                mapRenderer.delete();
            }
            mapRenderer = new MapRenderer(scan);
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x + (width / 2), y + (height / 2), 500);

        GlStateManager.rotate(-45.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.scale(-1.8, -1.8, -1.8);

        GlStateManager.translate(-8.0, 0.0, -8.0);
        GlStateManager.translate(-originX, 0, -originZ);

        mapRenderer.performUploads();
        mapRenderer.render();

        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }
    
    @Override
    public void mouseMove(int x, int y, boolean drag) {
    	if (drag) {
    	    prevOriginX = originX;
    	    prevOriginZ = originZ;
    		originX += x - lastMouseDragX;
    		originZ += y - lastMouseDragY;
    		lastMouseDragX = x;
    		lastMouseDragY = y;
    		
    		Iterator<IScannedChunk> iter = mapRenderer.getTerrainScan().getChunks().iterator();
    	    ChunkPos origin = new ChunkPos(originX >> 4, originZ >> 4);
    		while (iter.hasNext()) {
    		    IScannedChunk chunk = iter.next();
    		    BlockPos pos = chunk.getChunkPos();
    		    if (Math.abs(pos.getX() - origin.x) > 4 || Math.abs(pos.getZ() - origin.z) > 4) {
    		        mapRenderer.removeChunk(chunk);
    		    } else {
//    		        System.out.println();
    		    }
    		}
    	}
    }
    
    @Override
    public void mouseClick(int x, int y, int button, boolean down) {
    	if (button == 0) {
    		if (down) {
    			lastMouseDragX = x;
    			lastMouseDragY = y;
    		}
    	}
    }
    
    @Override
    public boolean needsUpdate() {
    	return mapRenderer != null && (prevOriginX >> 4 != originX >> 4 || prevOriginZ >> 4 != originZ >> 4);
    }
    
    @Override
    public NBTTagCompound getRequestData() {
    	Set<ChunkPos> known = mapRenderer.getTerrainScan().getChunks().stream()
    			.map(IScannedChunk::getChunkPos)
    			.map(p -> new ChunkPos(p.getX(), p.getZ()))
    			.distinct()
    			.collect(Collectors.toSet());
    	
    	ChunkPos origin = new ChunkPos(originX >> 4, originZ >> 4);
    	IntList data = new IntArrayList();
    	for (int x = origin.x - 4; x <= origin.x + 4; x++) {
    		for (int z = origin.z - 4; z <= origin.z + 4; z++) {
    			if (!known.contains(new ChunkPos(x, z))) {
    				data.add(x);
    				data.add(z);
    			}
    		}
    	}
    	NBTTagCompound ret = new NBTTagCompound();
    	ret.setIntArray("chunks", data.toIntArray());
    	return ret;
    }
    
    @Override
    public void updateData(NBTTagCompound data) {
    	ITerrainScan newData = new TerrainScanData();
    	newData.deserializeNBT(data);
    	newData.getChunks().forEach(mapRenderer::addChunk);
    }
}
