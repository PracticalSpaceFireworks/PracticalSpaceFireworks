package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.entity.world.FixedSizeWorldData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SpacecraftBodyData extends FixedSizeWorldData implements ISpacecraftBodyData {
    protected SpacecraftBodyData(int[] blockData, int[] lightData, Long2ObjectMap<TileEntity> entities, BlockPos minPos, BlockPos maxPos) {
        super(blockData, lightData, entities, minPos, maxPos);
    }

    private SpacecraftBodyData() {
        super();
    }

    public static SpacecraftBodyData empty() {
        return new SpacecraftBodyData();
    }

    @Override
    @Nonnull
    public List<IModule> collectModules() {
        List<IModule> modules = new ArrayList<>();
        for (TileEntity entity : entities.values()) {
            IModule module = entity.getCapability(CapabilityModule.INSTANCE, null);
            if (module != null) {
                modules.add(module);
            }
        }
        return modules;
    }

    public static SpacecraftBodyData deserializeCraft(NBTTagCompound compound) {
        SpacecraftBodyData bodyData = new SpacecraftBodyData();
        bodyData.deserializeNBT(compound);
        return bodyData;
    }

    public static SpacecraftBodyData deserializeCraft(ByteBuf buffer) {
        SpacecraftBodyData bodyData = new SpacecraftBodyData();
        bodyData.deserialize(buffer);
        return bodyData;
    }
}
