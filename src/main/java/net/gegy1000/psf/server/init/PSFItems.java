package net.gegy1000.psf.server.init;

import lombok.val;
import net.gegy1000.psf.server.api.RegisterItemBlock;
import net.gegy1000.psf.server.item.ItemCraftingMaterial;
import net.gegy1000.psf.server.item.ItemTargetSelector;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.gegy1000.psf.PracticalSpaceFireworks.MODID;
import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

@ObjectHolder(MODID)
@EventBusSubscriber(modid = MODID)
public final class PSFItems {
    private static final String TARGET_SELECTOR = "target_selector";
    private static final String CRAFTING_MATERIAL = "crafting_material";

    private static final Set<Item> REGISTERED_ITEMS = new LinkedHashSet<>();
    private static final Set<ItemBlock> REGISTERED_ITEM_BLOCKS = new LinkedHashSet<>();

    @Nullable
    @ObjectHolder(TARGET_SELECTOR)
    private static ItemTargetSelector targetSelector;

    @Nullable
    @ObjectHolder(CRAFTING_MATERIAL)
    private static ItemCraftingMaterial craftingMaterial;

    private PSFItems() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    public static ItemTargetSelector targetSelector() {
        return checkNotNull(targetSelector);
    }

    @Nonnull
    public static ItemCraftingMaterial craftingMaterial() {
        return checkNotNull(craftingMaterial);
    }

    @Nonnull
    public static Set<Item> allItems() {
        return Collections.unmodifiableSet(REGISTERED_ITEMS);
    }

    @Nonnull
    public static Set<ItemBlock> allBlockItems() {
        return Collections.unmodifiableSet(REGISTERED_ITEM_BLOCKS);
    }

    @SubscribeEvent
    static void registerItems(final RegistryEvent.Register<Item> event) {
        val registry = event.getRegistry();
        register(registry, TARGET_SELECTOR, new ItemTargetSelector());
        register(registry, CRAFTING_MATERIAL, new ItemCraftingMaterial());
        registerBlockItems(registry);
    }

    private static void registerBlockItems(final IForgeRegistry<Item> registry) {
        for (val block : PSFBlocks.allBlocks()) {
            if (!(block instanceof RegisterItemBlock)) continue;
            val item = ((RegisterItemBlock) block).createItemBlock(block);
            if (REGISTERED_ITEM_BLOCKS.add(item)) {
                val name = block.getRegistryName();
                checkState(name != null, "Missing registry name: %s", block);
                registry.register(item.setRegistryName(name));
            }
        }
    }

    private static void register(final IForgeRegistry<Item> registry, final String name, final Item item) {
        val id = namespace(name);
        item.setRegistryName(id).setTranslationKey(namespace(name, '.'));
        checkState(REGISTERED_ITEMS.add(item), "Already registered [%s: %s]", id, item);
        registry.register(item);
    }
}
