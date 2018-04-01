package net.gegy1000.psf.server.util;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.gegy1000.psf.server.api.CustomMass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockMassHandler {
    private static final Object2DoubleMap<Material> MATERIAL_MASS = new Object2DoubleOpenHashMap<>();

    public static void register() {
        registerMass(Material.AIR, 0.0);
        registerMass(Material.GRASS, 1.6e+3);
        registerMass(Material.GROUND, 1.6e+3);
        registerMass(Material.WOOD, 0.5e+3);
        registerMass(Material.ROCK, 2.65e+3);
        registerMass(Material.IRON, 7.874e+3);
        registerMass(Material.ANVIL, 7.874e+3);
        registerMass(Material.WATER, 1e+3);
        registerMass(Material.LAVA, 2.65e+3);
        registerMass(Material.LEAVES, 0.15e+3);
        registerMass(Material.PLANTS, 0.15e+3);
        registerMass(Material.VINE, 0.15e+3);
        registerMass(Material.SPONGE, 0.1e+3);
        registerMass(Material.CLOTH, 0.1e+3);
        registerMass(Material.FIRE, 0.0);
        registerMass(Material.SAND, 1.6e+3);
        registerMass(Material.CIRCUITS, 0.2e+3);
        registerMass(Material.CARPET, 0.1e+3);
        registerMass(Material.GLASS, 2.579e+3);
        registerMass(Material.REDSTONE_LIGHT, 2.5e+3);
        registerMass(Material.CORAL, 0.15e+3);
        registerMass(Material.ICE, 0.91e+3);
        registerMass(Material.PACKED_ICE, 0.91e+3);
        registerMass(Material.SNOW, 0.91e+3);
        registerMass(Material.CRAFTED_SNOW, 0.91e+3);
        registerMass(Material.CACTUS, 0.15e+3);
        registerMass(Material.CLAY, 1.33e+3);
        registerMass(Material.GOURD, 0.5e+3);
        registerMass(Material.CAKE, 0.15e+3);
        registerMass(Material.WEB, 0.1e+3);
    }

    public static void registerMass(Material material, double mass) {
        if (MATERIAL_MASS.containsKey(material)) {
            throw new IllegalArgumentException("Cannot double-register material mass!");
        }
        MATERIAL_MASS.put(material, mass);
    }

    public static double getMass(IBlockState state) {
        if (state.getBlock() instanceof CustomMass) {
            return ((CustomMass) state.getBlock()).getMass(state);
        }

        double mass = getMaterialMass(state.getMaterial());
        if (state.isFullBlock()) {
            mass /= 2.0;
        }

        return mass;
    }

    public static double getMaterialMass(Material material) {
        return MATERIAL_MASS.getOrDefault(material, 0.5e+3);
    }
}
