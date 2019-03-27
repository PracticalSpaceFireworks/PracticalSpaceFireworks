package net.gegy1000.psf.server.init;

import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.block.BlockPSFFluid;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.controller.ControllerType;
import net.gegy1000.psf.server.block.fueler.BlockFuelLoader;
import net.gegy1000.psf.server.block.module.*;
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
    public static final BlockFuelLoader FUEL_LOADER = null;
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

        register(registry, Name.KEROSENE, new BlockPSFFluid(PSFFluids.kerosene(), Material.WATER));
        register(registry, Name.LIQUID_OXYGEN, new BlockPSFFluid(PSFFluids.liquidOxygen(), Material.WATER));
        register(registry, Name.LIQUID_NITROGEN, new BlockPSFFluid(PSFFluids.liquidNitrogen(), Material.WATER));
        register(registry, Name.FILTERED_AIR, new BlockPSFFluid(PSFFluids.filteredAir(), Material.WATER));
        register(registry, Name.COMPRESSED_AIR, new BlockPSFFluid(PSFFluids.compressedAir(), Material.WATER));

        register(registry, Name.STRUT_CUBE, new BlockStrutFixed(Name.STRUT_CUBE));
        register(registry, Name.STRUT_SLOPE, new BlockStrutOrientable.Slope(Name.STRUT_SLOPE));

        register(registry, Name.CONTROLLER_SIMPLE, new BlockController(ControllerType.BASIC));
        register(registry, Name.REMOTE_CONTROL_SYSTEM, new BlockRemoteControlSystem());
        register(registry, Name.PAYLOAD_SEPARATOR, new BlockPayloadSeparator());

        register(registry, Name.FUEL_LOADER, new BlockFuelLoader());
        register(registry, Name.FUEL_VALVE, new BlockFuelValve());
        register(registry, Name.FUEL_TANK, new BlockFuelTank());
        register(registry, Name.THRUSTER_SIMPLE, new BlockThruster(Name.THRUSTER_SIMPLE));

        register(registry, Name.KEROSENE_EXTRACTOR, new BlockKeroseneExtractor());
        register(registry, Name.AIR_INTAKE, new BlockAirIntake());
        register(registry, Name.AIR_COMPRESSOR, new BlockAirCompressor());
        register(registry, Name.AIR_SEPARATOR, new BlockAirSeparator());

        register(registry, Name.SOLAR_PANEL_SMALL, new BlockSmallSolarPanel());
        register(registry, Name.SOLAR_PANEL_LARGE, new BlockLargeSolarPanel(Name.SOLAR_PANEL_LARGE));
        register(registry, Name.BATTERY_SIMPLE, new BlockBattery(Name.BATTERY_SIMPLE));

        registerModule(registry, Name.ENTITY_DETECTOR_SIMPLE);
        registerModule(registry, Name.ENTITY_MARKER);
        registerModule(registry, Name.TERRAIN_SCANNER);
        registerModule(registry, Name.WEATHER_SCANNER);

        register(registry, Name.LASER, new BlockLaser(Name.LASER));

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

    private static final class Name {
        static final String KEROSENE = "kerosene";
        static final String LIQUID_OXYGEN = "liquid_oxygen";
        static final String LIQUID_NITROGEN = "liquid_nitrogen";
        static final String FILTERED_AIR = "filtered_air";
        static final String COMPRESSED_AIR = "compressed_air";
        static final String STRUT_CUBE = "strut_cube";
        static final String STRUT_SLOPE = "strut_slope";
        static final String CONTROLLER_SIMPLE = "controller_simple";
        static final String REMOTE_CONTROL_SYSTEM = "remote_control_system";
        static final String PAYLOAD_SEPARATOR = "payload_separator";
        static final String FUEL_LOADER = "fuel_loader";
        static final String FUEL_VALVE = "fuel_valve";
        static final String FUEL_TANK = "fuel_tank";
        static final String THRUSTER_SIMPLE = "thruster_simple";
        static final String KEROSENE_EXTRACTOR = "kerosene_extractor";
        static final String AIR_INTAKE = "air_intake";
        static final String AIR_COMPRESSOR = "air_compressor";
        static final String AIR_SEPARATOR = "air_separator";
        static final String SOLAR_PANEL_SMALL = "solar_panel_small";
        static final String SOLAR_PANEL_LARGE = "solar_panel_large";
        static final String BATTERY_SIMPLE = "battery_simple";
        static final String ENTITY_DETECTOR_SIMPLE = "entity_detector_simple";
        static final String ENTITY_MARKER = "entity_marker";
        static final String TERRAIN_SCANNER = "terrain_scanner";
        static final String WEATHER_SCANNER = "weather_scanner";
        static final String LASER = "laser";
    }
}
