package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISpacecraftBodyData;
import net.gegy1000.psf.api.ISpacecraftMetadata;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.gegy1000.psf.server.entity.world.FixedSizeWorldData;
import net.gegy1000.psf.server.modules.ModuleThruster;
import net.gegy1000.psf.server.util.BlockMassHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpacecraftBodyData extends FixedSizeWorldData implements ISpacecraftBodyData {
    protected SpacecraftBodyData(int[] blockData, int[] lightData, Long2ObjectMap<TileEntity> entities, Biome biome, BlockPos minPos, BlockPos maxPos) {
        super(blockData, lightData, entities, biome, minPos, maxPos);
    }

    protected SpacecraftBodyData() {
        super();
    }

    @Override
    @Nullable
    public IController findController() {
        for (TileEntity entity : entities.values()) {
            if (entity.hasCapability(CapabilityController.INSTANCE, null)) {
                return entity.getCapability(CapabilityController.INSTANCE, null);
            }
        }
        return null;
    }

    @Override
    @Nonnull
    public List<IModule> findModules() {
        List<IModule> modules = new ArrayList<>();
        for (TileEntity entity : entities.values()) {
            if (entity.hasCapability(CapabilityModule.INSTANCE, null)) {
                modules.add(entity.getCapability(CapabilityModule.INSTANCE, null));
            }
        }
        return modules;
    }

    @Override
    public ISpacecraftMetadata buildSpacecraftMetadata(World parent) {
        double mass = 0.0;
        Point3d com = new Point3d(0.0, 0.0, 0.0);

        World world = buildWorld(parent);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(this.minPos, this.maxPos)) {
            double blockMass = BlockMassHandler.getMass(world, pos, this.getBlockState(pos));
            mass += blockMass;

            com.x += (pos.getX() + 0.5) * blockMass;
            com.y += (pos.getY() + 0.5) * blockMass;
            com.z += (pos.getZ() + 0.5) * blockMass;
        }

        com.x /= mass;
        com.y /= mass;
        com.z /= mass;

        ImmutableList.Builder<SpacecraftMetadata.Thruster> thrusters = ImmutableList.builder();

        for (TileEntity entity : this.entities.values()) {
            if (entity.hasCapability(CapabilityModule.INSTANCE, null)) {
                IModule module = entity.getCapability(CapabilityModule.INSTANCE, null);
                if (module instanceof ModuleThruster) {
                    ModuleThruster.ThrusterTier tier = ((ModuleThruster) module).getTier();
                    thrusters.add(new SpacecraftMetadata.Thruster(entity.getPos(), tier.getThrust(), tier.getDrain()));
                }
            }
        }

        List<IModule> modules = this.findModules();
        List<IFluidHandler> fuelTanks = modules.stream().filter(module -> module.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                .map(module -> module.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                .collect(Collectors.toList());

        return new SpacecraftMetadata(modules, fuelTanks, thrusters.build(), mass, com);
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
