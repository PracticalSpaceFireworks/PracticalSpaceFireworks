package net.gegy1000.psf.server.block.controller;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Value;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.modules.ModuleController;
import net.gegy1000.psf.server.modules.Modules;
import net.gegy1000.psf.server.satellite.TileBoundSatellite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class TileController extends TileEntity implements ITickable {

    @Value
    public class ScanValue {
        IBlockState state;
        IModule module;
    }

    @Nonnull
    private final ISatellite satellite = new TileBoundSatellite(this, "Unnamed Craft #" + hashCode() % 1000);
    private final ModuleController controller = (ModuleController) Modules.get().getValue(new ResourceLocation(PracticalSpaceFireworks.MODID, "controller_simple")).get();

    private boolean converted;

    private CraftGraph craft = new CraftGraph(satellite);

    @Override
    public void update() {
        if (!world.isRemote) {
            satellite.tickSatellite(getWorld().getTotalWorldTime());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!getWorld().isRemote) {
            PracticalSpaceFireworks.PROXY.getSatellites().register(satellite);
        }
        controller.setPos(getPos());
        scanStructure();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        unregister();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        unregister();
    }

    private void unregister() {
        if (!getWorld().isRemote && !converted) {
            PracticalSpaceFireworks.PROXY.getSatellites().remove(satellite);
        }
    }

    public void converted() {
        this.converted = true;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilitySatellite.INSTANCE ||
                capability == CapabilityController.INSTANCE ||
                capability == CapabilityModule.INSTANCE ||
                super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilitySatellite.INSTANCE) {
            return CapabilitySatellite.INSTANCE.cast(satellite);
        }
        if (capability == CapabilityController.INSTANCE) {
            return CapabilityController.INSTANCE.cast(controller);
        }
        if (capability == CapabilityModule.INSTANCE) {
            return CapabilityModule.INSTANCE.cast(controller);
        }
        return super.getCapability(capability, facing);
    }

    public CraftGraph getModules() {
        return craft;
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("satellite_data", satellite.serializeNBT());
        compound.setTag("module_data", controller.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        satellite.deserializeNBT(compound.getCompoundTag("satellite_data"));
        this.controller.deserializeNBT(compound.getCompoundTag("module_data"));
        this.controller.setOwner(satellite);
    }

    public void scanStructure() {
        craft.scan(getPos(), getWorld());
    }
}
