package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.module.ISeparator;
import net.gegy1000.psf.api.module.IThruster;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.api.spacecraft.IStageMetadata;
import net.gegy1000.psf.server.block.controller.CraftGraph;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

import java.util.Collection;

public class StageMetadata implements IStageMetadata {
    private final ImmutableList<Thruster> thrusters;
    private final ImmutableList<Separator> separators;

    private final IFluidHandler fuelHandler;

    private final double totalForce;
    private final int totalDrain;

    private StageMetadata(
            ImmutableList<IFluidHandler> fuelTanks,
            ImmutableList<Thruster> thrusters,
            ImmutableList<Separator> separators
    ) {
        this.thrusters = thrusters;
        this.separators = separators;

        double totalForce = 0.0;
        for (Thruster thruster : this.thrusters) {
            totalForce += thruster.getForce();
        }
        this.totalForce = totalForce;

        int totalDrain = 0;
        for (Thruster thruster : this.thrusters) {
            totalDrain += thruster.getDrain();
        }
        this.totalDrain = totalDrain;

        this.fuelHandler = new FluidHandlerConcatenate(fuelTanks);
    }

    public static StageMetadata build(ISpacecraftBodyData body, CraftGraph graph) {
        ImmutableList.Builder<IFluidHandler> fuelTanks = ImmutableList.builder();
        ImmutableList.Builder<Thruster> thrusters = ImmutableList.builder();
        ImmutableList.Builder<Separator> separators = ImmutableList.builder();

        for (BlockPos pos : graph.getPositions()) {
            TileEntity entity = body.getTileEntity(pos);
            if (entity == null) continue;

            IModule module = entity.getCapability(CapabilityModule.INSTANCE, null);
            if (module == null) continue;

            IFluidHandler fluidHandler = module.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (fluidHandler != null) {
                fuelTanks.add(fluidHandler);
            }

            IThruster thruster = module.getCapability(ModuleCapabilities.THRUSTER, null);
            if (thruster != null) {
                thrusters.add(new Thruster(entity.getPos(), thruster.getThrustPerTick(), thruster.getDrainPerTick()));
            }

            ISeparator separator = module.getCapability(ModuleCapabilities.SEPARATOR, null);
            if (separator != null) {
                separators.add(new Separator(entity.getPos(), EnumFacing.DOWN));
            }
        }

        return new StageMetadata(fuelTanks.build(), thrusters.build(), separators.build());
    }

    @Override
    public Collection<Thruster> getThrusters() {
        return thrusters;
    }

    @Override
    public Collection<Separator> getSeparators() {
        return separators;
    }

    @Override
    public double getTotalForce() {
        return totalForce;
    }

    @Override
    public int getTotalFuelDrain() {
        return totalDrain;
    }

    @Override
    public IFluidHandler getFuelHandler() {
        return fuelHandler;
    }
}
