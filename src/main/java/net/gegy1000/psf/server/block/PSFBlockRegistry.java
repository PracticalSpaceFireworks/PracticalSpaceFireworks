package net.gegy1000.psf.server.block;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.controller.ControllerType;
import net.gegy1000.psf.server.block.fueler.BlockFuelLoader;
import net.gegy1000.psf.server.block.module.BlockBattery;
import net.gegy1000.psf.server.block.module.BlockFuelTank;
import net.gegy1000.psf.server.block.module.BlockFuelValve;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.module.BlockMultiblockModule;
import net.gegy1000.psf.server.block.module.BlockPayloadSeparator;
import net.gegy1000.psf.server.block.module.BlockStrut;
import net.gegy1000.psf.server.block.module.TileDummyModule;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.block.remote.BlockRemoteControlSystem;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class PSFBlockRegistry {
    private static final Set<Block> REGISTERED_BLOCKS = new LinkedHashSet<>();
    private static final Set<ItemBlock> REGISTERED_ITEM_BLOCKS = new LinkedHashSet<>();

    public static BlockFluidFinite kerosene;
    public static BlockFluidFinite liquidOxygen;

    public static BlockStrut strut;

    public static BlockController basicController;
    public static BlockModule thruster;

    public static BlockRemoteControlSystem remoteControlSystem;
    public static BlockFuelLoader fuelLoader;

    public static BlockFuelTank fuelTank;
    public static BlockPayloadSeparator payloadSeparator;
    public static BlockMultiblockModule solarPanel;
    public static BlockMultiblockModule laser;

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        register(event, "controller.simple", basicController = new BlockController(ControllerType.BASIC));

        // Modules
        strut = register(event, "strut", new BlockStrut());
        register(event, "battery.simple", new BlockBattery("battery_simple"));
        thruster = register(event, "thruster.simple", new BlockModule(Material.IRON, "thruster_simple") {
            @Override
            protected boolean canAttachOnSide(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull IBlockState on, @Nonnull EnumFacing side) {
                return side == EnumFacing.DOWN;
            }
        });
//        registerModuleBlock(event, "antenna");
        registerModuleBlock(event, "entity_detector.simple");
        registerModuleBlock(event, "entity_marker");
        fuelTank = register(event, "fuel_tank", new BlockFuelTank());
        payloadSeparator = register(event, "payload_separator", new BlockPayloadSeparator());
        registerModuleBlock(event, "terrain_scanner");
        solarPanel = register(event, "solar_panel", new BlockMultiblockModule(Material.IRON, "solar_panel"));
        laser = register(event, "laser", new BlockMultiblockModule(Material.IRON, "laser") {
            
            @Override
            protected int getHeight() {
                return 2;
            }
        });

        kerosene = register(event, "kerosene", new BlockPSFFluid(PSFFluidRegistry.KEROSENE, Material.WATER));
        liquidOxygen = register(event, "liquid_oxygen", new BlockPSFFluid(PSFFluidRegistry.LIQUID_OXYGEN, Material.WATER));

        remoteControlSystem = register(event, "remote_control_system", new BlockRemoteControlSystem());
        fuelLoader = register(event, "fuel_loader", new BlockFuelLoader());

        register(event, "fuel_valve", new BlockFuelValve());

        // Register module TE only once
        GameRegistry.registerTileEntity(TileModule.class, PracticalSpaceFireworks.MODID + ":" + "module");
        GameRegistry.registerTileEntity(TileDummyModule.class, PracticalSpaceFireworks.MODID + ":" + "dummy_module");
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        for (Block block : REGISTERED_BLOCKS) {
            if (block instanceof RegisterItemBlock) {
                if (block.getRegistryName() == null) {
                    PracticalSpaceFireworks.LOGGER.warn("Tried to register ItemBlock for block without registry name!");
                    continue;
                }
                ItemBlock itemBlock = ((RegisterItemBlock) block).createItemBlock(block);
                event.getRegistry().register(itemBlock.setRegistryName(block.getRegistryName()));
                REGISTERED_ITEM_BLOCKS.add(itemBlock);
            }
        }
    }

    private static BlockModule registerModuleBlock(RegistryEvent.Register<Block> event, @Nonnull String identifier) {
        return registerModuleBlock(event, Material.IRON, identifier);
    }

    private static BlockModule registerModuleBlock(RegistryEvent.Register<Block> event, Material material, @Nonnull String identifier) {
        return register(event, identifier, new BlockModule(material, identifier.replace('.', '_')));
    }

    private static <T extends Block> T register(RegistryEvent.Register<Block> event, @Nonnull String identifier, T block) {
        event.getRegistry().register(block.setRegistryName(new ResourceLocation(PracticalSpaceFireworks.MODID, identifier.replace('.', '_'))));
        block.setUnlocalizedName(PracticalSpaceFireworks.MODID + "." + identifier);
        REGISTERED_BLOCKS.add(block);

        if (block instanceof RegisterTileEntity) {
            String blockEntityKey = PracticalSpaceFireworks.MODID + "." + identifier;
            GameRegistry.registerTileEntity(((RegisterTileEntity) block).getEntityClass(), blockEntityKey);
        }

        return block;
    }

    public static Set<Block> getRegisteredBlocks() {
        return Collections.unmodifiableSet(REGISTERED_BLOCKS);
    }

    public static Set<ItemBlock> getRegisteredItemBlocks() {
        return Collections.unmodifiableSet(REGISTERED_ITEM_BLOCKS);
    }
}
