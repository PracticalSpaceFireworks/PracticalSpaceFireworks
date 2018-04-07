package net.gegy1000.psf.server.block.controller;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class CraftGraph implements Iterable<IModule> {
    
    @ToString
    @RequiredArgsConstructor
    @Getter
    private static class Vertex {
        private final IModule module;
        private final BlockPos pos;

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            
            Vertex other = (Vertex) obj;

            if (!module.getId().equals(other.module.getId())) {
                return false;
            }
            
            if (!pos.equals(other.pos)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + module.getId().hashCode();
            result = prime * result + pos.hashCode();
            return result;
        }
    }

    @Value
    private static class SearchNode {
        private final Vertex vertex;
        private final int distance;
    }
    
    public static final int RANGE = 32;
    
    @Getter
    private final ISatellite satellite;
    
    private final Multimap<Vertex, Vertex> adjacencies = HashMultimap.create();
    
    public void scan(BlockPos root, World world) {
        scan(root, world, Predicates.alwaysTrue());
    }
    
    public void scan(BlockPos root, World world, Predicate<IBlockState> filter) {
        // Clear owners
        adjacencies.values().forEach(v -> {
            IModule m = TileModule.getModule(world.getTileEntity(v.getPos()));
            if (m != null) {
                m.setOwner(null);
            }
        });
        
        // Empty graph
        adjacencies.clear();

        // Set up BFS
        Set<BlockPos> seen = new HashSet<>();
        seen.add(root);

        IModule module = TileModule.getModule(world.getTileEntity(root));
        Queue<SearchNode> search = new ArrayDeque<>();
        search.add(new SearchNode(new Vertex(module, root), 0));
        
        // Find all modules
        while (!search.isEmpty()) {
            SearchNode ret = search.poll();
            if (ret.getDistance() < 10) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    BlockPos bp = ret.getVertex().getPos().offset(face);
                    IBlockState state = world.getBlockState(bp);
                    TileEntity te = world.getTileEntity(bp);
                    IModule cap = te != null ? te.getCapability(CapabilityModule.INSTANCE, null) : null;
                    ISatellite owner = cap == null ? null : cap.getOwner();
                    boolean haveSeen = seen.contains(bp);
                    if (cap != null && (haveSeen || ((owner == null || owner.isInvalid() || owner.equals(getSatellite())) && filter.test(state)))) {
                        Vertex newVertex = new Vertex(cap, bp);
                        if (!haveSeen) {
                            // Only search through this node if it's not been seen before
                            search.offer(new SearchNode(newVertex, ret.getDistance() + 1));
                            cap.setOwner(getSatellite());
                        }
                        adjacencies.get(ret.getVertex()).add(newVertex);
                    }
                    seen.add(bp);
                }
            }
        }
    }

    public Iterable<BlockPos> getPositions() {
        return adjacencies.keySet().stream().map(Vertex::getPos)::iterator;
    }

    @Override
    public Iterator<IModule> iterator() {
        return adjacencies.keySet().stream().map(Vertex::getModule).iterator();
    }
}
