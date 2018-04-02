package net.gegy1000.psf.client.model;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.block.PSFBlockRegistry;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.module.BlockMultiblockModule;
import net.gegy1000.psf.server.block.module.StrutType;
import net.gegy1000.psf.server.item.PSFItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID, value = Side.CLIENT)
public class PSFModelRegistry {
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent event) {
        OBJLoader.INSTANCE.addDomain(PracticalSpaceFireworks.MODID);

        Set<Item> registeredItems = PSFItemRegistry.getRegisteredItems();

        for (Item item : registeredItems) {
            if (item instanceof RegisterItemModel) {
                String location = ((RegisterItemModel) item).getResource(item.getRegistryName());
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(location, "inventory"));
            }
        }

        Set<Block> registeredBlocks = PSFBlockRegistry.getRegisteredBlocks();
        for (Block block : registeredBlocks) {
            if (block instanceof RegisterItemModel) {
                String location = ((RegisterItemModel) block).getResource(block.getRegistryName());
                Item itemBlock = Item.getItemFromBlock(block);
                if (itemBlock != Items.AIR) {
                    ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(location, "inventory"));
                } else {
                    PracticalSpaceFireworks.LOGGER.error("Tried to register item model for block without item!");
                }
            }
        }
        
        IStateMapper controllerMapper = new StateMap.Builder().ignore(BlockController.TYPE).build();
        ModelLoader.setCustomStateMapper(PSFBlockRegistry.basicController, controllerMapper);
        
        ModelLoader.setCustomStateMapper(PSFBlockRegistry.strut, new StateMap.Builder().ignore(BlockModule.DIRECTION).build());
        ModelLoader.setCustomStateMapper(PSFBlockRegistry.fuelTank, new StateMap.Builder().ignore(BlockModule.DIRECTION).build());

        ModelLoader.setCustomStateMapper(PSFBlockRegistry.kerosene, new StateMap.Builder().ignore(BlockFluidFinite.LEVEL).build());
        ModelLoader.setCustomStateMapper(PSFBlockRegistry.liquidOxygen, new StateMap.Builder().ignore(BlockFluidFinite.LEVEL).build());
        
        IStateMapper dummyMapper = new StateMap.Builder().ignore(BlockMultiblockModule.DUMMY).build();
        ModelLoader.setCustomStateMapper(PSFBlockRegistry.solarPanel, dummyMapper);
        ModelLoader.setCustomStateMapper(PSFBlockRegistry.laser, dummyMapper);

        Item strut = Item.getItemFromBlock(PSFBlockRegistry.strut);
        for (StrutType type : StrutType.values()) {
            ModelLoader.setCustomModelResourceLocation(strut, type.ordinal(), new ModelResourceLocation(PSFBlockRegistry.strut.getRegistryName(), "type=" + type.getName()));
        }
    }
}
