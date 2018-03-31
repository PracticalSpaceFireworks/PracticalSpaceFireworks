package net.gegy1000.psf.server.block.controller;

import com.google.common.collect.Sets;
import lombok.Value;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.modules.EmptyModule;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public class TileController extends TileEntity {
    
    private final IModule module = new EmptyModule("controller");
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityController.INSTANCE || capability == CapabilityModule.INSTANCE || super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityController.INSTANCE) {
            // TODO
            return null;
        }
        if (capability == CapabilityModule.INSTANCE) {
            return CapabilityModule.INSTANCE.cast(module);
        }
        return super.getCapability(capability, facing);
    }

    public Map<BlockPos, IModule> scanStructure() {
        Iterator<BlockPos> struts = getContiguousIterator(getPos());
        Map<BlockPos, IModule> ret = new HashMap<>();
        while (struts.hasNext()) {
            BlockPos pos = struts.next();
            for (EnumFacing dir : EnumFacing.VALUES) {
                BlockPos pos2 = pos.offset(dir);
                TileEntity te = getWorld().getTileEntity(pos2);
                if (te != null && te.hasCapability(CapabilityModule.INSTANCE, dir)) {
                    IModule module = te.getCapability(CapabilityModule.INSTANCE, dir);
                    ret.put(pos2, module);
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
