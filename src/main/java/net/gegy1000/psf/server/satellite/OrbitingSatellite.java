package net.gegy1000.psf.server.satellite;

import com.google.common.collect.ImmutableList;
import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class OrbitingSatellite implements ISatellite {
    private final World world;

    private final String name;
    private final UUID uuid;

    private final BlockPos position;
    private final SpacecraftBlockAccess blockAccess;

    private final IController controller;
    private final List<IModule> modules;

    public OrbitingSatellite(World world, String name, UUID uuid, BlockPos position, SpacecraftBlockAccess blockAccess) {
        this.world = world;
        this.name = name;
        this.uuid = uuid;
        this.position = position;
        this.blockAccess = blockAccess;

        IController controller = null;
        ImmutableList.Builder<IModule> modules = ImmutableList.builder();
        for (TileEntity entity : blockAccess.getEntities()) {
            if (entity.hasCapability(CapabilityController.INSTANCE, null)) {
                controller = entity.getCapability(CapabilityController.INSTANCE, null);
            } else if (entity.hasCapability(CapabilityModule.INSTANCE, null)) {
                modules.add(entity.getCapability(CapabilityModule.INSTANCE, null));
            }
        }

        this.controller = controller;
        this.modules = modules.build();
    }

    @Override
    public UUID getId() {
        return this.uuid;
    }

    @Override
    public IController getController() {
        return this.controller;
    }

    @Override
    public Collection<IModule> getModules() {
        return this.modules;
    }

    @Override
    public BlockPos getPosition() {
        return this.position;
    }

    @Override
    public SpacecraftBlockAccess buildBlockAccess(BlockPos origin, World world) {
        return blockAccess;
    }

    @Override
    public void requestModules() {
        // TODO
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("name", this.name);
        compound.setUniqueId("uuid", this.uuid);

        compound.setInteger("x", this.position.getX());
        compound.setInteger("y", this.position.getY());
        compound.setInteger("z", this.position.getZ());

        compound.setTag("block_data", this.blockAccess.serialize(new NBTTagCompound()));

        return compound;
    }

    public static OrbitingSatellite deserialize(World world, NBTTagCompound compound) {
        String name = compound.getString("name");
        UUID uuid = compound.getUniqueId("uuid");

        BlockPos pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
        SpacecraftBlockAccess blockAccess = SpacecraftBlockAccess.deserialize(world, compound);

        return new OrbitingSatellite(world, name, uuid, pos, blockAccess);
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ISatellite && ((ISatellite) obj).getId().equals(this.uuid);
    }
}
