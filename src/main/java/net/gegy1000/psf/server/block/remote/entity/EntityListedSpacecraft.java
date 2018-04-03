package net.gegy1000.psf.server.block.remote.entity;

import net.gegy1000.psf.client.IVisualReceiver;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.packet.PacketSetName;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.PacketLaunchCraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.satellite.EntityBoundSatellite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.UUID;

public class EntityListedSpacecraft implements IListedSpacecraft {
    private final EntitySpacecraft spacecraft;
    private final UUID uuid;

    public EntityListedSpacecraft(EntitySpacecraft spacecraft, UUID uuid) {
        this.spacecraft = spacecraft;
        this.uuid = uuid;
    }
    
    @Override
    public UUID getId() {
        return uuid;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.spacecraft.getSatellite().getName();
    }

    @Override
    public void setName(@Nonnull String name) {
        PSFNetworkHandler.network.sendToServer(new PacketSetName(uuid, name));
        spacecraft.getSatellite().setName(name);
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.spacecraft.getPosition();
    }

    @Override
    public void requestVisualData() {
        if (this.spacecraft.getEntityWorld().isRemote) {
            this.respondVisualData();
        }
    }

    private void respondVisualData() {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen instanceof IVisualReceiver) {
            EntityBoundSatellite satellite = this.spacecraft.getSatellite();
            Visual visual = new Visual(satellite.buildBlockAccess(this.spacecraft.getEntityWorld()), satellite.getModules());
            ((IVisualReceiver) currentScreen).setVisual(visual);
        }
    }

    @Override
    public boolean canLaunch() {
        return true;
    }

    @Override
    public void launch() {
        PSFNetworkHandler.network.sendToServer(new PacketLaunchCraft(spacecraft.getEntityId()));
    }
}
