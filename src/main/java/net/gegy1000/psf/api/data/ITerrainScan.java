package net.gegy1000.psf.api.data;

import net.minecraft.block.material.MapColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface ITerrainScan extends IModuleData {
	
	void addChunk(IScannedChunk chunk);
	
    void removeChunk(IScannedChunk chunk);

    @Nullable
    MapColor getMapColor(int x, int y, int z);

    @Nonnull
    Collection<IScannedChunk> getChunks();

    int getMinHeight();

    int getMaxHeight();

}
