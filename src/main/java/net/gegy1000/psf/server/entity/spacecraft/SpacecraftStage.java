package net.gegy1000.psf.server.entity.spacecraft;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.api.spacecraft.IStageMetadata;
import net.gegy1000.psf.server.block.controller.CraftGraph;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpacecraftStage {
    @Getter
    private final BlockPos origin;

    @Getter
    private final CraftGraph graph;
    @Getter
    private final IStageMetadata metadata;

    public static Optional<SpacecraftStage> scanStageFrom(ISatellite satellite, BlockPos origin) {
        CraftGraph graph = new CraftGraph(satellite);
        ISpacecraftBodyData body = satellite.getBodyData();
        graph.scan(origin, body, d -> !d.getModule().hasCapability(ModuleCapabilities.SEPARATOR, null));

        if (graph.isEmpty()) {
            return Optional.empty();
        }

        IStageMetadata metadata = StageMetadata.build(body, graph);
        return Optional.of(new SpacecraftStage(origin, graph, metadata));
    }

    @Override
    public int hashCode() {
        return origin.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SpacecraftStage && ((SpacecraftStage) obj).origin.equals(origin);
    }
}
