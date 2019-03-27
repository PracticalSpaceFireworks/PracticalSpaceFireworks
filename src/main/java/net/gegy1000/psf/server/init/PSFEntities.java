package net.gegy1000.psf.server.init;

import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

import javax.annotation.Nonnull;

import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

@SuppressWarnings("unused")
@ObjectHolder(PracticalSpaceFireworks.MODID)
@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public final class PSFEntities {
    public static final EntityEntry SPACECRAFT = null;

    private static int nextId = 0;

    private PSFEntities() {
        throw new UnsupportedOperationException();
    }

    @SubscribeEvent
    static void registerEntities(final RegistryEvent.Register<EntityEntry> event) {
        @Nonnull val registry = event.getRegistry();

        registry.register(EntityEntryBuilder.create()
            .entity(EntitySpacecraft.class)
            .factory(EntitySpacecraft::new)
            .id(namespace("spacecraft"), nextId())
            .name(namespace("spacecraft", '.'))
            .tracker(1024, 3, true)
            .build()
        );
    }

    private static int nextId() {
        return nextId++;
    }
}
