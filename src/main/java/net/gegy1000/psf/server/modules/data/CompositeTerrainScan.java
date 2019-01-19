package net.gegy1000.psf.server.modules.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.data.IScannedChunk;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.gegy1000.psf.server.modules.ModuleTerrainScanner;
import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * Convenience wrapper for multiple {@link ITerrainScan} instances. <strong>Cannot be serialized!</strong>
 */
@RequiredArgsConstructor
public class CompositeTerrainScan implements ITerrainScan {
    
    private final Collection<ITerrainScan> scans;
    
    private int cachedMinHeight = -1, cachedMaxHeight = -1;

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (ITerrainScan scan : scans) {
            list.appendTag(scan.serializeNBT());
        }
        NBTTagCompound ret = new NBTTagCompound();
        ret.setTag("scans", list);
        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        scans.clear();
        NBTTagList list = nbt.getTagList("scans", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            ITerrainScan scan = new TerrainScanData();
            scan.deserializeNBT(tag);
            scans.add(scan);
        }
    }

    @Override
    @Nullable
    public MapColor getMapColor(int x, int y, int z) {
        return scans.stream().map(scan -> scan.getMapColor(x, y, z)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @Override
    @Nonnull
    public Collection<IScannedChunk> getChunks() {
        return scans.stream().flatMap(scan -> scan.getChunks().stream()).collect(Collectors.toList());
    }

    @Override
    public int getMinHeight() {
        if (cachedMinHeight < 0) {
            cachedMinHeight = scans.stream().mapToInt(ITerrainScan::getMinHeight).min().orElse(0);
        }
        return cachedMinHeight;
    }

    @Override
    public int getMaxHeight() {
        if (cachedMaxHeight < 0) {
            cachedMaxHeight = scans.stream().mapToInt(ITerrainScan::getMaxHeight).max().orElse(0);
        }
        return cachedMaxHeight;
    }

	@Override
	public void addChunk(IScannedChunk chunk) {
		ITerrainScan scan;
		if (scans.isEmpty()) {
			scan = new TerrainScanData();
			scans.add(scan);
		} else {
			scan = scans.iterator().next();
		}
		scan.addChunk(chunk);
	}
}
