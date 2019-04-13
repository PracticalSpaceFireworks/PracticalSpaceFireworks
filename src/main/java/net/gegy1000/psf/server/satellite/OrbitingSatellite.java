package net.gegy1000.psf.server.satellite;

import lombok.Getter;
import lombok.Setter;
import net.gegy1000.psf.api.client.IVisualData;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.server.block.remote.orbiting.OrbitingListedSpacecraft;
import net.gegy1000.psf.server.block.remote.visual.VisualData;
import net.gegy1000.psf.server.block.remote.visual.VisualProperties;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBodyData;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftPhysics;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OrbitingSatellite extends AbstractSatellite {
    private final World world;

    @Getter
    @Setter
    private String name;
    private final UUID uuid;

    private final BlockPos position;
    private final ISpacecraftBodyData bodyData;

    private final List<IModule> modules;

    public OrbitingSatellite(World world, String name, UUID uuid, BlockPos position, ISpacecraftBodyData bodyData, Collection<EntityPlayerMP> trackingPlayers) {
        this.world = world;
        this.name = name;
        this.uuid = uuid;
        this.position = position;
        this.bodyData = bodyData;

        this.modules = bodyData.collectModules();

        modules.forEach(module -> module.setOwner(this));
        trackingPlayers.forEach(this::track);
    }

    @Nonnull
    @Override
    public UUID getId() {
        return this.uuid;
    }

    @Override
    public ISpacecraftBodyData getBodyData() {
        return bodyData;
    }

    @Override
    public IVisualData buildVisual() {
        SpacecraftPhysics physics = SpacecraftPhysics.build(bodyData);
        return VisualData.builder()
                .with(VisualProperties.BODY_DATA, bodyData)
                .with(VisualProperties.MASS, physics.getMass())
                .with(VisualProperties.THRUST, 0.0)
                .build();
    }

    @Override
    public Collection<IModule> getModules() {
        return this.modules;
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.position;
    }

    @Override
    public IListedSpacecraft toListedCraft() {
        return new OrbitingListedSpacecraft(this.name, this.position, this.uuid);
    }

    @Override
    public World getWorld() {
        return world;
    }
    
    @Override
    public boolean isOrbiting() {
        return true;
    }
    
    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("name", this.name);
        compound.setUniqueId("uuid", this.uuid);

        compound.setInteger("x", this.position.getX());
        compound.setInteger("y", this.position.getY());
        compound.setInteger("z", this.position.getZ());

        compound.setTag("body_data", this.bodyData.serializeNBT());

        return compound;
    }

    public static OrbitingSatellite deserialize(World world, NBTTagCompound compound) {
        String name = compound.getString("name");
        UUID uuid = compound.getUniqueId("uuid");

        BlockPos pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
        SpacecraftBodyData bodyData = SpacecraftBodyData.deserializeCraft(compound.getCompoundTag("body_data"));

        return new OrbitingSatellite(world, name, uuid, pos, bodyData, Collections.emptyList());
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
