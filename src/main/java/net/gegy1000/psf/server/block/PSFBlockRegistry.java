package net.gegy1000.psf.server.block;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterBlockEntity;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class PSFBlockRegistry {
    private static final Set<Block> REGISTERED_BLOCKS = new LinkedHashSet<>();
    private static final Set<ItemBlock> REGISTERED_ITEM_BLOCKS = new LinkedHashSet<>();

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        register(event, "controller", new BlockController());
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        for (Block block : REGISTERED_BLOCKS) {
            if (block instanceof RegisterItemBlock) {
                if (block.getRegistryName() == null) {
                    PracticalSpaceFireworks.LOGGER.warn("Tried to register ItemBlock for block without registry name!");
                    continue;
                }
                ItemBlock itemBlock = ((RegisterItemBlock) block).createItemBlock();
                event.getRegistry().register(itemBlock.setRegistryName(block.getRegistryName()));
            }
        }
    }

    private static void register(RegistryEvent.Register<Block> event, @Nonnull String identifier, Block block) {
        event.getRegistry().register(block.setRegistryName(new ResourceLocation(PracticalSpaceFireworks.MODID, identifier)));
        REGISTERED_BLOCKS.add(block);

        if (block instanceof RegisterBlockEntity) {
            String blockEntityKey = PracticalSpaceFireworks.MODID + "." + identifier;
            GameRegistry.registerTileEntity(((RegisterBlockEntity) block).getEntityClass(), blockEntityKey);
        }
    }

    public static Set<Block> getRegisteredBlocks() {
        return Collections.unmodifiableSet(REGISTERED_BLOCKS);
    }

    public static Set<ItemBlock> getRegisteredItemBlocks() {
        return Collections.unmodifiableSet(REGISTERED_ITEM_BLOCKS);
    }
}
