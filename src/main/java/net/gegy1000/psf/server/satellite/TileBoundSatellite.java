package net.gegy1000.psf.server.satellite;

import com.google.common.collect.Lists;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.spacecraft.IController;
import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.block.remote.tile.TileListedSpacecraft;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.entity.spacecraft.PacketLaunchTile;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBuilder;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@RequiredArgsConstructor
public class TileBoundSatellite extends AbstractSatellite {
    
    private final TileController controller;
    
    @Getter
    private UUID id = UUID.randomUUID();
    
    @Setter
    @Nonnull
    private String name;

    @Override
    public IController getController() {
        return controller.getCapability(CapabilityController.INSTANCE, null);
    }

    @Override
    public Collection<IModule> getModules() {
        return Lists.newArrayList(controller.getModules());
    }

    @Override
    public boolean isInvalid() {
        return controller.isInvalid();
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return controller.getPos();
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        controller.markDirty();
    }

    @Override
    public ISpacecraftBodyData buildBodyData(@Nonnull World world) {
        BlockPos origin = controller.getPos();
        SpacecraftBuilder builder = new SpacecraftBuilder();
        builder.copyFrom(world, origin, controller.getModules());
        return builder.buildBodyData(origin, world);
    }

    @Override
    public IListedSpacecraft toListedCraft() {
        return new TileListedSpacecraft(this);
    }

    @Override
    public World getWorld() {
        return controller.getWorld();
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setLong("uuid_msb", getId().getMostSignificantBits());
        tag.setLong("uuid_lsb", getId().getLeastSignificantBits());
        tag.setString("name", name);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);
        this.id = new UUID(tag.getLong("uuid_msb"), tag.getLong("uuid_lsb"));
        this.name = tag.getString("name");
    }

    @Override
    public void launch() {
        PSFNetworkHandler.network.sendToServer(new PacketLaunchTile(controller.getPos()));
    }

    @Override
    public boolean canLaunch() {
        return true;
    }
    
    @Override
    public boolean isDestroyed() {
        return controller.isInvalid();
    }
}
