package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.entity.world.FixedSizeWorldHandler;
import net.gegy1000.psf.server.modules.ModuleThruster;
import net.gegy1000.psf.server.util.BlockMassHandler;
import net.gegy1000.psf.server.util.PointUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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

public class SpacecraftWorldHandler extends FixedSizeWorldHandler {
    protected SpacecraftWorldHandler(int[] blockData, int[] lightData, Long2ObjectMap<TileEntity> entities, Biome biome, BlockPos minPos, BlockPos maxPos) {
        super(blockData, lightData, entities, biome, minPos, maxPos);
    }

    protected SpacecraftWorldHandler() {
        super();
    }

    @Nullable
    public IController findController() {
        for (TileEntity entity : entities.values()) {
            if (entity.hasCapability(CapabilityController.INSTANCE, null)) {
                return entity.getCapability(CapabilityController.INSTANCE, null);
            }
        }
        return null;
    }

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

    public SpacecraftWorldHandler[] splitVertically(World world, Block separator) {
        List<SpacecraftWorldHandler> split = new ArrayList<>();
        int startY = minPos.getY();
        for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
            for (BlockPos pos : BlockPos.getAllInBox(minPos.getX(), y, minPos.getZ(), maxPos.getX(), y, maxPos.getZ())) {
                if (getBlockState(pos).getBlock() == separator) {
                    split.add(splitSection(world, startY, y));
                    startY = y + 1;
                    break;
                }
            }
        }
        if (maxPos.getY() >= startY) {
            split.add(splitSection(world, startY, maxPos.getY()));
        }
        return split.toArray(new SpacecraftWorldHandler[0]);
    }

    private SpacecraftWorldHandler splitSection(World world, int startY, int endY) {
        BlockPos fittedMinPos = this.maxPos;
        BlockPos fittedMaxPos = this.minPos;
        for (BlockPos pos : BlockPos.getAllInBoxMutable(this.minPos.getX(), startY, this.minPos.getZ(), this.maxPos.getX(), endY, this.maxPos.getZ())) {
            if (getBlockState(pos).getBlock() != Blocks.AIR) {
                fittedMinPos = PointUtils.min(fittedMinPos, pos);
                fittedMaxPos = PointUtils.max(fittedMaxPos, pos);
            }
        }

        BlockPos minPos = fittedMinPos.down(startY);
        BlockPos maxPos = fittedMaxPos.down(startY);

        BlockPos offset = new BlockPos(
                (this.minPos.getX() + this.maxPos.getX() - (minPos.getX() + maxPos.getX())) / 2,
                0,
                (this.minPos.getZ() + this.maxPos.getZ() - (minPos.getZ() + maxPos.getZ())) / 2
        );

        // TODO: Extract copy method into FixedSizeWorldHandler
        int dataSize = getDataSize(minPos, maxPos);
        int[] blockData = new int[dataSize];
        int[] lightData = new int[dataSize];

        for (BlockPos pos : BlockPos.getAllInBoxMutable(minPos, maxPos)) {
            int localIndex = getPosIndex(pos, minPos, maxPos);
            int globalIndex = getPosIndex(pos.subtract(offset), this.minPos, this.maxPos);
            if (localIndex != -1 && globalIndex != -1) {
                blockData[localIndex] = this.blockData[globalIndex];
                lightData[localIndex] = this.lightData[globalIndex];
            }
        }

        Long2ObjectMap<TileEntity> entities = new Long2ObjectOpenHashMap<>();
        for (TileEntity entity : this.entities.values()) {
            int entityY = entity.getPos().getY();
            if (entityY >= startY && entityY <= endY) {
                BlockPos localPos = entity.getPos().add(offset);
                NBTTagCompound tag = entity.serializeNBT();
                // TE must get the proper position during readFromNBT
                // TODO unify with SpacecraftBuilder.deconstruct
                tag.setInteger("x", localPos.getX());
                tag.setInteger("y", localPos.getY());
                tag.setInteger("z", localPos.getZ());
                TileEntity copiedEntity = TileEntity.create(world, tag);
                if (copiedEntity == null) {
                    PracticalSpaceFireworks.LOGGER.warn("Failed to copy TE for spacecraft");
                    continue;
                }
                entities.put(localPos.toLong(), copiedEntity);
            }
        }

        return new SpacecraftWorldHandler(blockData, lightData, entities, biome, minPos, maxPos);
    }

    public SpacecraftMetadata buildSpacecraftMetadata() {
        double mass = 0.0;
        Point3d com = new Point3d(0.0, 0.0, 0.0);

        for (BlockPos pos : BlockPos.getAllInBoxMutable(this.minPos, this.maxPos)) {
            double blockMass = BlockMassHandler.getMass(parent, pos, this.getBlockState(pos));
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

    public static SpacecraftWorldHandler deserializeCraft(NBTTagCompound compound) {
        SpacecraftWorldHandler worldHandler = new SpacecraftWorldHandler();
        worldHandler.deserialize(compound);
        return worldHandler;
    }

    public static SpacecraftWorldHandler deserializeCraft(ByteBuf buffer) {
        SpacecraftWorldHandler worldHandler = new SpacecraftWorldHandler();
        worldHandler.deserialize(buffer);
        return worldHandler;
    }
}
