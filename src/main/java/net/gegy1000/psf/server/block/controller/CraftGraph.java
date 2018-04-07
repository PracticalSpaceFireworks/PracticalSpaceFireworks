package net.gegy1000.psf.server.block.controller;

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

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
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
public class CraftGraph implements Iterable<IModule> {
    
    @Value
    private static class Vertex {
        int id;
        IModule module;
        BlockPos pos;
    }

    @Value
    private static class SearchNode {
        private final BlockPos pos;
        private final int distance;
    }
    
    public static final int RANGE = 32;
    
    @Getter
    @Nonnull
    private final ISatellite satellite;
    
    private final Int2ObjectMap<List<Vertex>> adjacencies = new Int2ObjectArrayMap<>();
    private final Map<BlockPos, Vertex> vertices = new HashMap<>();
    
    protected final List<Vertex> getAdjacent(int id) {
        return adjacencies.computeIfAbsent(id, i -> new ArrayList<>());
    }
    
    public void scan(BlockPos root, World world) {
        scan(root, world, Predicates.alwaysTrue());
    }
    
    public void scan(BlockPos root, World world, Predicate<IBlockState> filter) {
        // Clear owners
        vertices.forEach((p, v) -> {
            IModule m = TileModule.getModule(world.getTileEntity(v.getPos()));
            if (m != null) {
                m.setOwner(null);
            }
        });
        
        // Empty graph
        adjacencies.clear();
        vertices.clear();
        
        // Add root vertex
        IModule rootModule = world.getTileEntity(root).getCapability(CapabilityModule.INSTANCE, null);
        vertices.put(root, new Vertex(0, rootModule, root));
        
        // Set up BFS
        Set<BlockPos> seen = new HashSet<>();
        seen.add(root);

        Queue<SearchNode> search = new ArrayDeque<>();
        search.add(new SearchNode(root, 0));
        
        int id = 1;

        // Find all modules
        while (!search.isEmpty()) {
            SearchNode ret = search.poll();
            Vertex v = vertices.get(ret.getPos());
            if (ret.getDistance() < 10) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    BlockPos bp = ret.getPos().offset(face);
                    IBlockState state = world.getBlockState(bp);
                    TileEntity te = world.getTileEntity(bp);
                    IModule cap = te != null ? te.getCapability(CapabilityModule.INSTANCE, null) : null;
                    ISatellite owner = cap == null ? null : cap.getOwner();
                    if (cap != null && seen.contains(bp)) {
                        // If this is an already seen position, then we don't want to add a new vertex, only an edge
                        Vertex existing = vertices.get(bp);
                        getAdjacent(v.getId()).add(existing);
                    } else if (cap != null && (owner == null || owner.isInvalid() || owner.equals(getSatellite())) && filter.test(state)) {
                        // Otherwise, this is a newly discovered module, so add it to the search queue and create a new vertex/edge
                        search.offer(new SearchNode(bp, ret.getDistance() + 1));
                        Vertex newVertex = new Vertex(id++, cap, bp);
                        getAdjacent(v.getId()).add(newVertex);
                        vertices.put(bp, newVertex);
                        cap.setOwner(getSatellite());
                    }
                    seen.add(bp);
                }
            }
        }
    }

    public Iterable<BlockPos> getPositions() {
        return vertices.keySet();
    }

    @Override
    public Iterator<IModule> iterator() {
        return vertices.values().stream().map(Vertex::getModule).iterator();
    }
}
