package net.gegy1000.psf.server.block.remote.entity;

import net.gegy1000.psf.server.block.remote.GuiControlSystem;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.satellite.EntityBoundSatellite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class EntityListedSpacecraft implements IListedSpacecraft {
    private final EntitySpacecraft spacecraft;

    public EntityListedSpacecraft(EntitySpacecraft spacecraft) {
        this.spacecraft = spacecraft;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.spacecraft.getSatellite().getName();
    }

    @Override
    public void setName(@Nonnull String name) {
        PSFNetworkHandler.network.sendToServer(new PacketSetNameEntity(spacecraft.getEntityId(), name));
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
        if (currentScreen instanceof GuiControlSystem) {
            EntityBoundSatellite satellite = this.spacecraft.getSatellite();
            Visual visual = new Visual(satellite.buildBlockAccess(this.spacecraft.getEntityWorld()), satellite.getModules());
            ((GuiControlSystem) currentScreen).setVisual(visual);
        }
    }
}
