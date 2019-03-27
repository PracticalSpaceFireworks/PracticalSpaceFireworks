package net.gegy1000.psf.server.init;

import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.item.ItemCraftingMaterial;
import net.gegy1000.psf.server.item.ItemTargetSelector;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
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

@SuppressWarnings("unused")
@ObjectHolder(PracticalSpaceFireworks.MODID)
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public final class PSFItems {
    public static final ItemTargetSelector TARGET_SELECTOR = null;
    public static final ItemCraftingMaterial CRAFTING_MATERIAL = null;

    private static final Set<Item> ALL_ITEMS = new LinkedHashSet<>();
    private static final Set<ItemBlock> ALL_BLOCK_ITEMS = new LinkedHashSet<>();

    private PSFItems() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static Set<Item> allItems() {
        return Collections.unmodifiableSet(ALL_ITEMS);
    }

    @Nonnull
    public static Set<ItemBlock> allBlockItems() {
        return Collections.unmodifiableSet(ALL_BLOCK_ITEMS);
    }

    @SubscribeEvent
    static void registerItems(final RegistryEvent.Register<Item> event) {
        val registry = event.getRegistry();
        register(registry, "target_selector", new ItemTargetSelector());
        register(registry, "crafting_material", new ItemCraftingMaterial());
        registerBlockItems(registry);
    }

    private static void registerBlockItems(final IForgeRegistry<Item> registry) {
        for (val block : PSFBlocks.allBlocks()) {
            if (!(block instanceof RegisterItemBlock)) continue;
            val item = ((RegisterItemBlock) block).createItemBlock(block);
            if (ALL_BLOCK_ITEMS.add(item)) {
                val name = block.getRegistryName();
                checkState(name != null, "Missing registry name: %s", block);
                registry.register(item.setRegistryName(name));
            }
        }
    }

    private static void register(final IForgeRegistry<Item> registry, final String name, final Item item) {
        val id = namespace(name);
        item.setRegistryName(id).setTranslationKey(namespace(name, '.'));
        checkState(ALL_ITEMS.add(item), "Already registered [%s: %s]", id, item);
        registry.register(item);
    }
}
