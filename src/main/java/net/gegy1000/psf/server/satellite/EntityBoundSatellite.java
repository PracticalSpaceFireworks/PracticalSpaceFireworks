package net.gegy1000.psf.server.satellite;

import lombok.Setter;
import net.gegy1000.psf.api.client.IVisualData;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.server.block.remote.entity.EntityListedSpacecraft;
import net.gegy1000.psf.server.block.remote.visual.VisualData;
import net.gegy1000.psf.server.block.remote.visual.VisualProperties;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.PacketLaunchCraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBuilder;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftStage;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityBoundSatellite extends AbstractSatellite {

    private final EntitySpacecraft spacecraft;
    private UUID uuid;

    private final List<IModule> modules = new ArrayList<>();
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
        this.modules.addAll(bodyData.collectModules());
        this.modules.forEach(module -> module.setOwner(this));
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
    public Collection<IModule> getModules() {
        return this.modules;
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.spacecraft.getPosition();
    }

    @Override
    public IListedSpacecraft toListedCraft() {
        return new EntityListedSpacecraft(spacecraft, uuid);
    }

    @Override
    public ISpacecraftBodyData getBodyData() {
        return spacecraft.getBody().getData();
    }

    @Override
    public IVisualData buildVisual() {
        ISpacecraftBodyData bodyData = getBodyData();
        double mass = spacecraft.getPhysics().getMass();
        double thrust = 0.0;

        for (SpacecraftStage stage : spacecraft.getActiveStages()) {
            thrust += stage.getMetadata().getTotalForce();
        }

        return VisualData.builder()
                .with(VisualProperties.BODY_DATA, bodyData)
                .with(VisualProperties.MASS, mass)
                .with(VisualProperties.THRUST, thrust)
                .build();
    }

    @Override
    public World getWorld() {
        return spacecraft.getEntityWorld();
    }

    @Override
    public Optional<LaunchHandle> getLaunchHandle() {
        if (this.spacecraft.getState().getType() == EntitySpacecraft.StateType.STATIC) {
            return Optional.of(() -> {
                PSFNetworkHandler.network.sendToServer(new PacketLaunchCraft(spacecraft.getEntityId()));
            });
        } else {
            return Optional.empty();
        }
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
        // TODO: Drop all other remaining stages at this point
        ISpacecraftBodyData payload = buildPayload();
        return new OrbitingSatellite(getWorld(), name, getId(), getPosition(), payload, getTrackingPlayers());
    }

    private ISpacecraftBodyData buildPayload() {
        DelegatedWorld bodyWorld = spacecraft.getBody().getWorld();
        SpacecraftStage upperStage = spacecraft.getStageTree().getUpperStage();
        BlockPos origin = upperStage.getOrigin();
        Collection<BlockPos> positions = upperStage.getGraph().getPositions();

        SpacecraftBuilder builder = new SpacecraftBuilder().copyFrom(bodyWorld, origin, positions);
        return builder.buildBodyData(origin, bodyWorld);
    }
}
