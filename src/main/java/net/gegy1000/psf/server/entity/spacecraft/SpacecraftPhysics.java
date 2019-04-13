package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import net.gegy1000.psf.api.module.IAdditionalMass;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.api.spacecraft.ISpacecraftPhysics;
import net.gegy1000.psf.server.util.BlockMassHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Point3d;

public class SpacecraftPhysics implements ISpacecraftPhysics {
    private final ImmutableList<IAdditionalMass> additionalMasses;
    private final double mass;
    private final Point3d com;

    private SpacecraftPhysics(
            ImmutableList<IAdditionalMass> additionalMasses,
            double mass,
            Point3d com
    ) {
        this.additionalMasses = additionalMasses;
        this.mass = mass;
        this.com = com;
    }

    public static SpacecraftPhysics build(ISpacecraftBodyData body) {
        Point3d com = new Point3d(0.0, 0.0, 0.0);
        double mass = 0.0;

        World world = body.asWorld();
        for (BlockPos pos : BlockPos.getAllInBox(body.getMinPos(), body.getMaxPos())) {
            IBlockState state = world.getBlockState(pos);

            double blockMass = BlockMassHandler.getMass(world, pos, state);
            mass += blockMass;

            com.x += (pos.getX() + 0.5) * blockMass;
            com.y += (pos.getY() + 0.5) * blockMass;
            com.z += (pos.getZ() + 0.5) * blockMass;
        }

        com.x /= mass;
        com.y /= mass;
        com.z /= mass;

        ImmutableList.Builder<IAdditionalMass> additionalMasses = ImmutableList.builder();
        for (IModule module : body.collectModules()) {
            IAdditionalMass additionalMass = module.getCapability(ModuleCapabilities.ADDITIONAL_MASS, null);
            if (additionalMass != null) {
                additionalMasses.add(additionalMass);
            }
        }

        return new SpacecraftPhysics(additionalMasses.build(), mass, com);
    }

    @Override
    public double getMass() {
        double mass = this.mass;
        for (IAdditionalMass module : this.additionalMasses) {
            mass += module.getAdditionalMass();
        }
        return mass;
    }

    @Override
    public Point3d getCoM() {
        return com;
    }
}
