package net.gegy1000.psf.server.block.controller;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import lombok.Value;
import lombok.experimental.Delegate;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.data.IModuleData;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.modules.EmptyModule;
import net.gegy1000.psf.server.satellite.TileBoundSatellite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;


public class TileController extends TileEntity implements ITickable {
    
    private final IModule module = new EmptyModule("controller").setRegistryName(new ResourceLocation(PracticalSpaceFireworks.MODID, "controller"));
    
    private interface Exclusions {
        <T extends IModuleData> Collection<T> getConnectedCaps(ISatellite satellite, Capability<T> capability);
    }
    
    private class Controller implements IController {
        
        @Delegate(excludes = Exclusions.class)
        private final IModule delegate = TileController.this.module;
        
        @Override
        public @Nonnull Optional<BlockPos> getPosition() {
            return Optional.of(getPos());
        }
        
        @Override
        public <T extends IModuleData> Collection<T> getConnectedCaps(@Nonnull ISatellite satellite, @Nonnull Capability<T> capability) {
            return delegate.getConnectedCaps(satellite, capability);
        }
    }
    
    @Value
    public class ScanValue {
        IBlockState state;
        IModule module;
    }
    
    private final ISatellite satellite = new TileBoundSatellite(this);
    private final IController controller = new Controller();
    
    private long lastScanTime = Long.MIN_VALUE;
    
    private boolean converted;
    
    private Map<BlockPos, ScanValue> modules = Collections.emptyMap();
    
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
            return CapabilityModule.INSTANCE.cast(module);
        }
        return super.getCapability(capability, facing);
    }
    
    public Map<BlockPos, ScanValue> getModules() {
        if (lastScanTime + 20 <= getWorld().getTotalWorldTime()) {
            modules = scanStructure();
            lastScanTime = getWorld().getTotalWorldTime();
        }
        return modules;
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
    }

    public Map<BlockPos, ScanValue> scanStructure() {
        Iterator<BlockPos> modules = getContiguousIterator(getPos());
        Map<BlockPos, ScanValue> ret = new HashMap<>();
        while (modules.hasNext()) {
            BlockPos pos = modules.next();
            TileEntity te = getWorld().getTileEntity(pos);
            if (te != null && te.hasCapability(CapabilityModule.INSTANCE, null)) {
                IModule module = te.getCapability(CapabilityModule.INSTANCE, null);
                ret.put(pos, new ScanValue(getWorld().getBlockState(pos), module));
            }
        }
        
        return ret;
    }

    @Value
    private static class Node {
        private BlockPos pos;
        int distance;
    }
    
    public static final int CONTIGUOUS_RANGE = 32;
    
    private Iterator<BlockPos> getContiguousIterator(final @Nonnull BlockPos origin) {
        return new Iterator<BlockPos>() {

            private Set<BlockPos> seen = Sets.newHashSet(origin);
            private Queue<Node> search = new ArrayDeque<>();
            { search.add(new Node(origin, 0)); }

            @Override
            public boolean hasNext() {
                return !search.isEmpty();
            }

            @Override
            public BlockPos next() {
                Node ret = search.poll();
                if (ret.getDistance() < CONTIGUOUS_RANGE) {
                    for (EnumFacing face : EnumFacing.VALUES) {
                        BlockPos bp = ret.getPos().offset(face);
                        TileEntity te = getWorld().getTileEntity(bp);
                        if (!seen.contains(bp) && te != null && te.hasCapability(CapabilityModule.INSTANCE, null)) {
                            search.offer(new Node(bp, ret.getDistance() + 1));
                        }
                        seen.add(bp);
                    }
                }
                return ret.getPos();
            }
        };
    }
}
