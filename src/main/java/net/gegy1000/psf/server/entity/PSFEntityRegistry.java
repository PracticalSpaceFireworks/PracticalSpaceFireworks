package net.gegy1000.psf.server.entity;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public class PSFEntityRegistry {
    private static final int CRAFT_ID = 0;

    @SubscribeEvent
    public static void onRegisterEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntitySpacecraft.class)
                .name(PracticalSpaceFireworks.MODID + ".spacecraft")
                .id(new ResourceLocation(PracticalSpaceFireworks.MODID, "spacecraft"), CRAFT_ID)
                .tracker(1024, 30, false)
                .build());
    }
}
