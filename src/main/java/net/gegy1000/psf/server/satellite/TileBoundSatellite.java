package net.gegy1000.psf.server.satellite;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.gegy1000.psf.api.client.IVisualData;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.block.remote.tile.TileListedSpacecraft;
import net.gegy1000.psf.server.block.remote.visual.VisualData;
import net.gegy1000.psf.server.block.remote.visual.VisualProperties;
import net.gegy1000.psf.server.entity.spacecraft.PacketLaunchTile;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBuilder;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftPhysics;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftStageTree;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class TileBoundSatellite extends AbstractSatellite {
    
    private final TileController controller;
    
    @Getter
    private UUID id = UUID.randomUUID();
    
    @Setter
    @Nonnull
    private String name;

    @Override
    public Collection<IModule> getModules() {
        return Lists.newArrayList(controller.getModules());
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
    public IVisualData buildVisual() {
        ISpacecraftBodyData bodyData = getBodyData();
        SpacecraftPhysics physics = SpacecraftPhysics.build(bodyData);
        SpacecraftStageTree stageTree = SpacecraftStageTree.scan(this);
        double thrust = stageTree.leaves().mapToDouble(stage -> stage.getMetadata().getTotalForce()).sum();

        return VisualData.builder()
                .with(VisualProperties.BODY_DATA, bodyData)
                .with(VisualProperties.MASS, physics.getMass())
                .with(VisualProperties.THRUST, thrust)
                .build();
    }

    @Override
    public ISpacecraftBodyData getBodyData() {
        World world = controller.getWorld();
        BlockPos origin = controller.getPos();
        SpacecraftBuilder builder = new SpacecraftBuilder();
        builder.copyFrom(world, origin, controller.getModules().getPositions());
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
    public Optional<LaunchHandle> getLaunchHandle() {
        return Optional.of(() -> {
            PSFNetworkHandler.network.sendToServer(new PacketLaunchTile(controller.getPos()));
        });
    }
    
    @Override
    public boolean isDestroyed() {
        return controller.isInvalid();
    }
}
