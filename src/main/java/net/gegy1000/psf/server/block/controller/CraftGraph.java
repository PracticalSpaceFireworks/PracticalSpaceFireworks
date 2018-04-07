package net.gegy1000.psf.server.block.controller;

import com.google.common.base.Predicates;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class CraftGraph implements Iterable<IModule> {
    
    @ToString
    @RequiredArgsConstructor
    @Getter
    public static class Vertex {
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
    
    private final Map<Vertex, List<Vertex>> adjacencies = new HashMap<>();
    
    private List<Vertex> getAdjacent(Vertex vertex) {
        return adjacencies.computeIfAbsent(vertex, v -> new ArrayList<>());
    }
    
    public void scan(BlockPos root, World world) {
        scan(root, world, Predicates.alwaysTrue());
    }
    
    public void scan(BlockPos root, World world, Predicate<Vertex> filter) {
        // Clear owners
        adjacencies.values().stream().flatMap(List::stream).forEach(v -> {
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
        
        Vertex rootVertex = new Vertex(module, root);
        search.add(new SearchNode(rootVertex, 0));
        getAdjacent(rootVertex); // Add empty mapping for root so it's in the keyset
        
        // Find all modules
        while (!search.isEmpty()) {
            SearchNode ret = search.poll();
            if (ret.getDistance() < RANGE) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    BlockPos bp = ret.getVertex().getPos().offset(face);
                    TileEntity te = world.getTileEntity(bp);
                    IModule cap = te != null ? te.getCapability(CapabilityModule.INSTANCE, null) : null;
                    ISatellite owner = cap == null ? null : cap.getOwner();
                    boolean haveSeen = seen.contains(bp);
                    if (cap != null) {
                        Vertex newVertex = new Vertex(cap, bp);
                        if (haveSeen) {
                            getAdjacent(ret.getVertex()).add(newVertex);
                        } else if ((owner == null || owner.isInvalid() || owner.equals(getSatellite())) && filter.test(newVertex)) {
                            // Only search through this node if it's not been seen before
                            search.offer(new SearchNode(newVertex, ret.getDistance() + 1));
                            cap.setOwner(getSatellite());
                        }
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
