package net.gegy1000.psf.server.block.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.spacecraft.IController;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
            } else if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Vertex other = (Vertex) obj;

            if (!module.getId().equals(other.module.getId())) {
                return false;
            }

            return pos.equals(other.pos);
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

    @Value
    public static class SearchData {
        private final IModule module;
        private final BlockPos pos, from;
        private final EnumFacing dir;
    }

    @FunctionalInterface
    public interface SearchFilter extends Predicate<SearchData> {

        public static final SearchFilter TRUE = d -> true;

        boolean test(SearchData data);
    }

    public static final int RANGE = 32;

    @Getter
    private final ISatellite satellite;

    private final Map<Vertex, List<Vertex>> adjacencies = new HashMap<>();

    private List<Vertex> getAdjacent(Vertex vertex) {
        return adjacencies.computeIfAbsent(vertex, v -> new ArrayList<>());
    }

    public void scan(BlockPos root, IBlockAccess world) {
        scan(root, world, SearchFilter.TRUE);
    }

    public void scan(BlockPos root, IBlockAccess world, SearchFilter terminationFilter) {
        // Compose filter argument with default filtering logic
        SearchFilter filter = data -> {
            if (!terminationFilter.test(data)) {
                return false;
            }
            if (data.getModule() instanceof IController) {
                return false; // Never connect to other controllers
            }
            ISatellite owner = data.getModule().getOwner();
            // Make sure this module is either unowned or owned by us
            if (owner == null || owner.isDestroyed() || owner.equals(getSatellite())) {
                IBlockState state = world.getBlockState(data.getFrom());
                // Check that connecting these states is valid, don't form edges through non-solid adjacent modules
                return BlockModule.isConnectedTo(state, data.getDir());
            }
            return false;
        };

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
                    if (cap == null) continue;

                    Vertex newVertex = new Vertex(cap, bp);
                    SearchData searchData = new SearchData(newVertex.getModule(), newVertex.getPos(), ret.getVertex().getPos(), face);

                    if (filter.test(searchData)) {
                        // Only search through this node if it's not been seen before
                        if (!seen.contains(bp)) {
                            search.offer(new SearchNode(newVertex, ret.getDistance() + 1));
                            cap.setOwner(getSatellite());
                        }
                        getAdjacent(ret.getVertex()).add(newVertex);
                        getAdjacent(newVertex).add(ret.getVertex());
                        seen.add(bp);
                    }
                }
            }
        }

        Collection<IModule> modules = getModules();
        for (IModule cap : modules) {
            cap.handleModuleChange(modules);
        }
    }

    public Collection<BlockPos> getPositions() {
        return adjacencies.keySet().stream().map(Vertex::getPos).collect(Collectors.toList());
    }

    public Collection<IModule> getModules() {
        return adjacencies.keySet().stream().map(Vertex::getModule).distinct().collect(Collectors.toList());
    }

    @Override
    public Iterator<IModule> iterator() {
        return getModules().iterator();
    }

    public boolean isEmpty() {
        return adjacencies.isEmpty();
    }
}
