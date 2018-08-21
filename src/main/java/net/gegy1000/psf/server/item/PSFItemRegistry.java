package net.gegy1000.psf.server.item;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class PSFItemRegistry {
    private static final Set<Item> REGISTERED_ITEMS = new LinkedHashSet<>();

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        register(event, "target_selector", new ItemTargetSelector());
    }

    private static void register(RegistryEvent.Register<Item> event, String identifier, Item item) {
        item.setTranslationKey(PracticalSpaceFireworks.MODID + "." + identifier);
        event.getRegistry().register(item.setRegistryName(identifier));
        REGISTERED_ITEMS.add(item);
    }

    public static Set<Item> getRegisteredItems() {
        return Collections.unmodifiableSet(REGISTERED_ITEMS);
    }
}
