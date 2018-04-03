package net.gegy1000.psf.server.util;

import com.google.common.collect.Sets;
import lombok.Value;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class ContiguousBlockIterator implements Iterator<BlockPos> {
    private final BlockPos origin;
    private final int range;
    private final Predicate<BlockPos> predicate;

    private final Set<BlockPos> seen;
    private final Queue<Node> search;

    public ContiguousBlockIterator(BlockPos origin, int range, Predicate<BlockPos> predicate) {
        this.origin = origin;
        this.range = range;

        this.seen = Sets.newHashSet(origin);
        this.predicate = predicate;
        this.search = new ArrayDeque<>();
        this.search.add(new Node(origin, 0));
    }

    @Override
    public boolean hasNext() {
        return !search.isEmpty();
    }

    @Override
    public BlockPos next() {
        Node ret = search.poll();
        if (ret.getDistance() < range) {
            for (EnumFacing face : EnumFacing.VALUES) {
                BlockPos bp = ret.getPos().offset(face);
                if (!seen.contains(bp) && predicate.test(bp)) {
                    search.offer(new Node(bp, ret.getDistance() + 1));
                }
                seen.add(bp);
            }
        }
        return ret.getPos();
    }

    @Value
    private static class Node {
        private final BlockPos pos;
        private final int distance;
    }
}
