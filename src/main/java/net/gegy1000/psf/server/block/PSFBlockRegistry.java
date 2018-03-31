package net.gegy1000.psf.server.block;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterTileEntity;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.controller.ControllerType;
import net.gegy1000.psf.server.block.module.BlockFuelTank;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.module.BlockPayloadAttacher;
import net.gegy1000.psf.server.block.module.BlockStrut;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.block.remote.BlockRemoteControlSystem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
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

    public static BlockStrut strut;

    public static BlockController basicController;
    public static BlockModule thruster;

    public static BlockRemoteControlSystem remoteControlSystem;

    public static BlockFuelTank fuelTank;
    public static BlockPayloadAttacher payloadAttacher;

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        register(event, "controller_basic", basicController = new BlockController(ControllerType.BASIC));

        // Modules
        strut = register(event, "strut", new BlockStrut());
        registerModuleBlock(event, "battery_simple");
        thruster = registerModuleBlock(event, "thruster");
        registerModuleBlock(event, "antenna");
        registerModuleBlock(event, "entity_detector");
        fuelTank = register(event, "fuel_tank", new BlockFuelTank());
        payloadAttacher = register(event, "payload_attacher", new BlockPayloadAttacher());

        remoteControlSystem = register(event, "remote_control_system", new BlockRemoteControlSystem());

        // Register module TE only once
        GameRegistry.registerTileEntity(TileModule.class, PracticalSpaceFireworks.MODID + "." + "module");
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
            }
        }
    }

    private static BlockModule registerModuleBlock(RegistryEvent.Register<Block> event, @Nonnull String identifier) {
        return registerModuleBlock(event, Material.IRON, identifier);
    }

    private static BlockModule registerModuleBlock(RegistryEvent.Register<Block> event, Material material, @Nonnull String identifier) {
        return register(event, identifier, new BlockModule(material, identifier));
    }

    private static <T extends Block> T register(RegistryEvent.Register<Block> event, @Nonnull String identifier, T block) {
        event.getRegistry().register(block.setRegistryName(new ResourceLocation(PracticalSpaceFireworks.MODID, identifier)));
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
