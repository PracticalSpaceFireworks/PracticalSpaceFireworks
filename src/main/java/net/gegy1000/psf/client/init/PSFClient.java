package net.gegy1000.psf.client.init;

import lombok.val;
import net.gegy1000.psf.server.api.RegisterItemModel;
import net.gegy1000.psf.server.block.BlockPSFFluid;
import net.gegy1000.psf.server.block.controller.BlockController;
import net.gegy1000.psf.server.block.module.BlockModule;
import net.gegy1000.psf.server.block.module.BlockMultiblockModule;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.gegy1000.psf.server.init.PSFItems;
import net.gegy1000.psf.server.item.ItemCraftingMaterial.CraftingMaterial;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

import static net.gegy1000.psf.PracticalSpaceFireworks.MODID;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(value = Side.CLIENT, modid = MODID)
public final class PSFClient {
    private PSFClient() {
        throw new UnsupportedOperationException();
    }

    @SubscribeEvent
    static void registerModels(final ModelRegistryEvent event) {
        OBJLoader.INSTANCE.addDomain(MODID);
        registerStateMappers();
        registerItemModels();
    }

    private static void registerStateMappers() { // fixme cleanup
        //noinspection ConstantConditions
        ModelLoader.setCustomStateMapper(PSFBlocks.CONTROLLER_SIMPLE,
            new StateMap.Builder().ignore(BlockController.TYPE).build()
        );

        for (val block : PSFBlocks.allBlocks()) {
            if (block == PSFBlocks.CONTROLLER_SIMPLE) continue;

            val builder = new StateMap.Builder();
            if (block instanceof BlockModule) {
                if (!((BlockModule) block).isDirectional()) {
                    builder.ignore(BlockModule.DIRECTION);
                }
                if (block instanceof BlockMultiblockModule) {
                    builder.ignore(BlockMultiblockModule.DUMMY);
                }
            }
            if (block instanceof BlockPSFFluid) {
                builder.ignore(BlockFluidFinite.LEVEL);
            }
            ModelLoader.setCustomStateMapper(block, builder.build());
        }
    }

    private static void registerItemModels() {
        for (val item : PSFItems.allItems()) {
            if (!(item instanceof RegisterItemModel)) continue;
            val path = ((RegisterItemModel) item).getResource(item.getRegistryName());
            val model = new ModelResourceLocation(path, "inventory");
            ModelLoader.setCustomModelResourceLocation(item, 0, model);
        }

        for (val material : CraftingMaterial.values()) {
            val path = new ResourceLocation(MODID, material.getName());
            val model = new ModelResourceLocation(path, "inventory");
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(PSFItems.CRAFTING_MATERIAL, material.ordinal(), model);
        }

        for (val item : PSFItems.allBlockItems()) {
            val path = Objects.requireNonNull(item.getRegistryName());
            val model = new ModelResourceLocation(path, "inventory");
            ModelLoader.setCustomModelResourceLocation(item, 0, model);
        }
    }
}
