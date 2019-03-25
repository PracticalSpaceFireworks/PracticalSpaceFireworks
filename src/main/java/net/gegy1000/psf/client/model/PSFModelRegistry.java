package net.gegy1000.psf.client.model;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.block.BlockPSFFluid;
import net.gegy1000.psf.server.block.PSFBlockRegistry;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.module.BlockMultiblockModule;
import net.gegy1000.psf.server.block.module.BlockPayloadSeparator;
import net.gegy1000.psf.server.item.ItemCraftingMaterial.CraftingMaterial;
import net.gegy1000.psf.server.item.PSFItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID, value = Side.CLIENT)
public final class PSFModelRegistry {
    private PSFModelRegistry() {}

    @SubscribeEvent
    static void onRegisterModels(ModelRegistryEvent event) {
        OBJLoader.INSTANCE.addDomain(PracticalSpaceFireworks.MODID);

        ignoreProperty(PSFBlockRegistry.basicController, BlockController.TYPE);

        for (Block block : PSFBlockRegistry.getRegisteredBlocks()) {
            if (block instanceof BlockModule) {
                if (!((BlockModule) block).isDirectional()) {
                    ignoreProperty(block, BlockModule.DIRECTION);
                }
                if (block instanceof BlockMultiblockModule) {
                    ignoreProperty(block, BlockMultiblockModule.DUMMY);
                }
            }
            if (block instanceof BlockPSFFluid) {
                ignoreProperty(block, BlockFluidFinite.LEVEL);
            }
        }

        for (Item item : PSFItemRegistry.getRegisteredItems()) {
            if (!(item instanceof RegisterItemModel)) continue;
            String path = ((RegisterItemModel) item).getResource(item.getRegistryName());
            ModelResourceLocation model = new ModelResourceLocation(path, "inventory");
            ModelLoader.setCustomModelResourceLocation(item, 0, model);
        }

        for (CraftingMaterial mat : CraftingMaterial.values()) {
            ResourceLocation path = new ResourceLocation(PracticalSpaceFireworks.MODID, mat.getName());
            ModelResourceLocation model = new ModelResourceLocation(path, "inventory");
            ModelLoader.setCustomModelResourceLocation(PSFItemRegistry.craftingMaterial, mat.ordinal(), model);
        }

        for (ItemBlock item : PSFBlockRegistry.getRegisteredItemBlocks()) {
            ResourceLocation path = Objects.requireNonNull(item.getRegistryName());
            ModelResourceLocation model = new ModelResourceLocation(path, "inventory");
            ModelLoader.setCustomModelResourceLocation(item, 0, model);
        }
    }

    private static void ignoreProperty(Block block, IProperty<?> property) {
        ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(property).build());
    }
}
