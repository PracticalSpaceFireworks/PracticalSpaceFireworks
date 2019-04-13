package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpacecraftStageTree implements Iterable<SpacecraftStage> {
    @Getter
    private final SpacecraftStage upperStage;
    private final Collection<SpacecraftStage> stages;
    private final Multimap<SpacecraftStage, SpacecraftStage> tree;

    public static SpacecraftStageTree scan(ISatellite satellite) {
        Scanner scanner = new Scanner(satellite);
        scanner.scanBranch(BlockPos.ORIGIN, null);
        return scanner.build();
    }

    public Collection<SpacecraftStage> drop(SpacecraftStage stage) {
        Collection<SpacecraftStage> dropped = new ArrayList<>();
        dropped.add(stage);

        stages.remove(stage);

        // drop the mappings from parent -> child
        parents(stage).forEach(parent -> tree.remove(parent, stage));

        // drop all children of this stage
        Collection<SpacecraftStage> children = tree.removeAll(stage);
        for (SpacecraftStage child : children) {
            dropped.addAll(drop(child));
        }

        return dropped;
    }

    public Collection<SpacecraftStage> getChildren(SpacecraftStage stage) {
        return tree.get(stage);
    }

    public Stream<SpacecraftStage> leaves() {
        return stages.stream().filter(stage -> getChildren(stage).isEmpty());
    }

    private Stream<SpacecraftStage> parents(SpacecraftStage stage) {
        return tree.entries().stream()
                .filter(e -> e.getValue().equals(stage))
                .map(Map.Entry::getKey);
    }

    @Override
    public Iterator<SpacecraftStage> iterator() {
        return stages.iterator();
    }

    public Stream<SpacecraftStage> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @RequiredArgsConstructor
    private static class Scanner {
        private final ISatellite satellite;

        private SpacecraftStage upperStage;

        private final Collection<SpacecraftStage> stages = new ArrayList<>();
        private final Multimap<SpacecraftStage, SpacecraftStage> tree = HashMultimap.create();

        private final Set<BlockPos> seenSeparators = new HashSet<>();

        void scanBranch(BlockPos origin, @Nullable SpacecraftStage parent) {
            Optional<SpacecraftStage> scan = SpacecraftStage.scanStageFrom(satellite, origin);
            if (!scan.isPresent()) {
                return;
            }

            SpacecraftStage stage = scan.get();
            putStage(stage, parent);

            for (StageMetadata.Separator separator : stage.getMetadata().getSeparators()) {
                if (seenSeparators.add(separator.getPos())) {
                    scanBranch(separator.getConnectedPos(), stage);
                }
            }
        }

        private void putStage(SpacecraftStage stage, @Nullable SpacecraftStage parent) {
            stages.add(stage);

            if (parent != null) {
                tree.put(parent, stage);
            } else {
                if (upperStage != null) {
                    PracticalSpaceFireworks.LOGGER.warn("Stage tree had more than one root!");
                }
                upperStage = stage;
            }
        }

        SpacecraftStageTree build() {
            Preconditions.checkNotNull(upperStage, "Root stage was null, this shouldn't be possible");
            return new SpacecraftStageTree(upperStage, stages, tree);
        }
    }
}
