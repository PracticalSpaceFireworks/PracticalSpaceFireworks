package net.gegy1000.psf.server.satellite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import lombok.Setter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IListedSpacecraft;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.ISpacecraftBodyData;
import net.gegy1000.psf.server.block.controller.CraftGraph;
import net.gegy1000.psf.server.block.controller.CraftGraph.SearchFilter;
import net.gegy1000.psf.server.block.remote.entity.EntityListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.PacketLaunchCraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBodyData;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBuilder;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityBoundSatellite extends AbstractSatellite {

    private final EntitySpacecraft spacecraft;
    private UUID uuid;

    private final List<IModule> modules = new ArrayList<>();
    private IController controller;
    @Setter
    @Nonnull
    private String name;

    public EntityBoundSatellite(EntitySpacecraft spacecraft, UUID uuid, @Nonnull String name) {
        this.spacecraft = spacecraft;
        this.uuid = uuid;
        this.name = name;
    }

    public void detectModules() {
        ISpacecraftBodyData bodyData = this.spacecraft.getBody().getData();

        this.modules.clear();
        this.modules.addAll(bodyData.findModules());
        this.modules.forEach(module -> module.setOwner(this));

        this.controller = bodyData.findController();
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
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
    public boolean isInvalid() {
        return !spacecraft.isEntityAlive();
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.spacecraft.getPosition();
    }

    @Override
    public ISpacecraftBodyData buildBodyData(@Nonnull World world) {
        return this.spacecraft.getBody().getData();
    }

    @Override
    public IListedSpacecraft toListedCraft() {
        return new EntityListedSpacecraft(spacecraft, uuid);
    }

    @Override
    public World getWorld() {
        return spacecraft.getEntityWorld();
    }

    @Override
    public boolean canLaunch() {
        return this.spacecraft.getState().getType() == EntitySpacecraft.StateType.STATIC;
    }

    @Override
    public void launch() {
        PSFNetworkHandler.network.sendToServer(new PacketLaunchCraft(spacecraft.getEntityId()));
    }
    
    @Override
    public boolean isDestroyed() {
        return spacecraft.isDead;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ISatellite && ((ISatellite) obj).getId().equals(this.getId());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setString("name", name);
        tag.setUniqueId("uuid", uuid);
        return tag;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {
        super.deserializeNBT(tag);
        if (tag != null) {
            this.name = tag.getString("name");
            this.uuid = tag.getUniqueId("uuid");
        }
    }

    public ISatellite toOrbiting() {
        CraftGraph craft = new CraftGraph(this);
        SearchFilter filter = d -> !d.getModule().getRegistryName().equals(new ResourceLocation(PracticalSpaceFireworks.MODID, "payload_separator"));
        DelegatedWorld spacecraftWorld = this.spacecraft.getBody().getWorld();
        craft.scan(BlockPos.ORIGIN, spacecraftWorld, filter);
        SpacecraftBodyData payload = new SpacecraftBuilder().copyFrom(spacecraftWorld, BlockPos.ORIGIN, craft).buildBodyData(BlockPos.ORIGIN, spacecraftWorld);
        return new OrbitingSatellite(this.getWorld(), this.name, this.getId(), this.getPosition(), payload, getTrackingPlayers());
    }
}
