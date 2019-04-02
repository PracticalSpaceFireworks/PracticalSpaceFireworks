package net.gegy1000.psf.server.init;

import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.fluid.BlockPSFFluid;
import net.gegy1000.psf.server.block.module.BlockBattery;
import net.gegy1000.psf.server.block.module.BlockFuelTank;
import net.gegy1000.psf.server.block.module.BlockFuelValve;
import net.gegy1000.psf.server.block.module.BlockLargeSolarPanel;
import net.gegy1000.psf.server.block.module.BlockLaser;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.module.BlockMultiblockModule;
import net.gegy1000.psf.server.block.module.BlockPayloadSeparator;
import net.gegy1000.psf.server.block.module.BlockSmallSolarPanel;
import net.gegy1000.psf.server.block.module.BlockStrutFixed;
import net.gegy1000.psf.server.block.module.BlockStrutOrientable;
import net.gegy1000.psf.server.block.module.BlockThruster;
import net.gegy1000.psf.server.block.module.TileDummyModule;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.block.production.BlockAirCompressor;
import net.gegy1000.psf.server.block.production.BlockAirIntake;
import net.gegy1000.psf.server.block.production.BlockAirSeparator;
import net.gegy1000.psf.server.block.production.BlockKeroseneExtractor;
import net.gegy1000.psf.server.block.remote.BlockRemoteControlSystem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;
import static net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity;

@SuppressWarnings("unused")
@ObjectHolder(PracticalSpaceFireworks.MODID)
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public final class PSFBlocks {
    public static final BlockFluidFinite KEROSENE = null;
    public static final BlockFluidFinite LIQUID_OXYGEN = null;
    public static final BlockFluidFinite LIQUID_NITROGEN = null;
    public static final BlockFluidFinite FILTERED_AIR = null;
    public static final BlockFluidFinite COMPRESSED_AIR = null;
    public static final BlockStrutFixed STRUT_CUBE = null;
    public static final BlockStrutOrientable STRUT_SLOPE = null;
    public static final BlockController CONTROLLER_SIMPLE = null;
    public static final BlockRemoteControlSystem REMOTE_CONTROL_SYSTEM = null;
    public static final BlockPayloadSeparator PAYLOAD_SEPARATOR = null;
    public static final BlockFuelValve FUEL_VALVE = null;
    public static final BlockFuelTank FUEL_TANK = null;
    public static final BlockModule THRUSTER_SIMPLE = null;
    public static final BlockKeroseneExtractor KEROSENE_EXTRACTOR = null;
    public static final BlockAirIntake AIR_INTAKE = null;
    public static final BlockAirCompressor AIR_COMPRESSOR = null;
    public static final BlockAirSeparator AIR_SEPARATOR = null;
    public static final BlockModule SOLAR_PANEL_SMALL = null;
    public static final BlockMultiblockModule SOLAR_PANEL_LARGE = null;
    public static final BlockBattery BATTERY_SIMPLE = null;
    public static final BlockModule ENTITY_DETECTOR_SIMPLE = null;
    public static final BlockModule ENTITY_MARKER = null;
    public static final BlockModule TERRAIN_SCANNER = null;
    public static final BlockModule WEATHER_SCANNER = null;
    public static final BlockMultiblockModule LASER = null;

    private static final Set<Block> ALL_BLOCKS = new LinkedHashSet<>();

    private PSFBlocks() {
        throw new UnsupportedOperationException();
    }

    public static Set<Block> allBlocks() {
        return Collections.unmodifiableSet(ALL_BLOCKS);
    }

    @SubscribeEvent
    static void registerBlocks(final RegistryEvent.Register<Block> event) {
        @Nonnull val registry = event.getRegistry();

        register(registry, "kerosene", new BlockPSFFluid(PSFFluids.kerosene(), Material.WATER));
        register(registry, "liquid_oxygen", new BlockPSFFluid(PSFFluids.liquidOxygen(), Material.WATER));
        register(registry, "liquid_nitrogen", new BlockPSFFluid(PSFFluids.liquidNitrogen(), Material.WATER));
        register(registry, "filtered_air", new BlockPSFFluid(PSFFluids.filteredAir(), Material.WATER));
        register(registry, "compressed_air", new BlockPSFFluid(PSFFluids.compressedAir(), Material.WATER));

        register(registry, "strut_cube", new BlockStrutFixed("strut_cube"));
        register(registry, "strut_slope", new BlockStrutOrientable.Slope("strut_slope"));

        register(registry, "controller_simple", new BlockController());
        register(registry, "remote_control_system", new BlockRemoteControlSystem());
        register(registry, "payload_separator", new BlockPayloadSeparator());

        register(registry, "fuel_valve", new BlockFuelValve());
        register(registry, "fuel_tank", new BlockFuelTank("fuel_tank"));
        register(registry, "thruster_simple", new BlockThruster("thruster_simple"));

        register(registry, "kerosene_extractor", new BlockKeroseneExtractor());
        register(registry, "air_intake", new BlockAirIntake());
        register(registry, "air_compressor", new BlockAirCompressor());
        register(registry, "air_separator", new BlockAirSeparator());

        register(registry, "solar_panel_small", new BlockSmallSolarPanel());
        register(registry, "solar_panel_large", new BlockLargeSolarPanel("solar_panel_large"));
        register(registry, "battery_simple", new BlockBattery("battery_simple"));

        registerModule(registry, "entity_detector_simple");
        registerModule(registry, "entity_marker");
        registerModule(registry, "terrain_scanner");
//        registerModule(registry, "weather_scanner");

        register(registry, "laser", new BlockLaser("laser"));

        // Register module TE only once
        registerTileEntity(TileModule.class, namespace("module"));
        registerTileEntity(TileDummyModule.class, namespace("dummy_module"));
    }

    private static void registerModule(final IForgeRegistry<Block> registry, final String name) {
        registerModule(registry, Material.IRON, name);
    }

    private static void registerModule(final IForgeRegistry<Block> registry, final Material material, final String name) {
        register(registry, name, new BlockModule(material, name));
    }

    private static <T extends Block> void register(final IForgeRegistry<Block> registry, final String name, final T block) {
        val id = namespace(name);
        block.setRegistryName(id).setTranslationKey(namespace(name, '.'));
        checkState(ALL_BLOCKS.add(block), "Already registered [%s: %s]", id, block);
        registry.register(block);
        if (block instanceof RegisterTileEntity) {
            registerTileEntity(((RegisterTileEntity) block).getEntityClass(), id);
        }
    }
}
