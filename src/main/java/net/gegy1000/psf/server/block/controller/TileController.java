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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import lombok.Value;
import lombok.val;
import lombok.experimental.Delegate;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IController;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.modules.EmptyModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;


public class TileController extends TileEntity {
    
    private final IModule module = new EmptyModule("controller").setRegistryName(new ResourceLocation(PracticalSpaceFireworks.MODID, "controller"));
    
    private class Controller implements IController {
        
        @Delegate
        private final IModule delegate = TileController.this.module;
        
        @Override
        public @Nonnull Optional<BlockPos> getPosition() {
            return Optional.of(getPos());
        }
    }
    
    @Value
    public class ScanValue {
        IBlockState state;
        IModule module;
    }
    
    private final IController controller = new Controller();
    
    private int lastScanTime = Integer.MIN_VALUE;
    
    private Map<BlockPos, ScanValue> modules = Collections.emptyMap();
    
    @Override
    public void onLoad() {
        super.onLoad();
        PracticalSpaceFireworks.PROXY.getControllerManager(getWorld().isRemote).registerController(this);
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
        PracticalSpaceFireworks.PROXY.getControllerManager(getWorld().isRemote).unregisterController(this);
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
            if (lastScanTime + 20 <= getWorld().getTotalWorldTime()) {
                modules = scanStructure();
            }
            return CapabilitySatellite.INSTANCE.cast(new ISatellite() {
                
                @Override
                public Collection<IModule> getModules() {
                    return modules.values().stream().map(ScanValue::getModule).collect(Collectors.toList());
                }
                
                @Override
                public Map<BlockPos, IBlockState> getComponents() {
                    Map<BlockPos, IBlockState> ret = new HashMap<>();
                    for (val e : modules.entrySet()) {
                        ret.put(e.getKey(), e.getValue().getState());
                    }
                    return ret;
                }
                
                @Override
                public IController getController() {
                    return getCapability(CapabilityController.INSTANCE, facing);
                }
                
                @Override
                public String toString() {
                    return "Craft #" + hashCode();
                }
            });
        }
        if (capability == CapabilityController.INSTANCE) {
            return CapabilityController.INSTANCE.cast(controller);
        }
        if (capability == CapabilityModule.INSTANCE) {
            return CapabilityModule.INSTANCE.cast(module);
        }
        return super.getCapability(capability, facing);
    }

    public Map<BlockPos, ScanValue> scanStructure() {
        Iterator<BlockPos> struts = getContiguousIterator(getPos());
        Map<BlockPos, ScanValue> ret = new HashMap<>();
        while (struts.hasNext()) {
            BlockPos pos = struts.next();
            for (EnumFacing dir : EnumFacing.VALUES) {
                BlockPos pos2 = pos.offset(dir);
                TileEntity te = getWorld().getTileEntity(pos2);
                if (te != null && te.hasCapability(CapabilityModule.INSTANCE, dir)) {
                    IModule module = te.getCapability(CapabilityModule.INSTANCE, dir);
                    ret.put(pos2, new ScanValue(getWorld().getBlockState(pos2), module));
                }
            }
        }
        
        return ret;
    }

    @Value
    private static class Node {
        private BlockPos pos;
        int distance;
    }
    
    public static final int CONTIGUOUS_RANGE = 10;
    
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
                        if (!seen.contains(bp) && BlockModule.isStructuralModule(world.getBlockState(bp))) {
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
