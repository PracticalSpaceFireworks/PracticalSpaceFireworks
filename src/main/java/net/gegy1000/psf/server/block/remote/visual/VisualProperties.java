package net.gegy1000.psf.server.block.remote.visual;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.client.IVisualProperty;
import net.gegy1000.psf.api.event.RegisterVisualPropertiesEvent;
import net.gegy1000.psf.api.spacecraft.ISpacecraftBodyData;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBodyData;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public final class VisualProperties {
    // TODO 1.13: Use a synced forge registry for integer ids
    private static final BiMap<ResourceLocation, IVisualProperty<?>> REGISTRY = HashBiMap.create();

    // TODO: Use properties for fuel & energy level
    //  Note: when implementing this, we need to make sure to not cause excessive overhead by requesting this every tick.
    //  (each time it is requested in some states, the whole craft is scanned)
    //  a general consideration is whether updates should just be sent whenever they occur, or if they should be done
    //  on a request basis, as it is currently
    public static final IVisualProperty<ISpacecraftBodyData> BODY_DATA = SimpleVisualProperty.of(ISpacecraftBodyData::serialize, SpacecraftBodyData::deserializeCraft);
    public static final IVisualProperty<Double> MASS = SimpleVisualProperty.ofDouble();
    public static final IVisualProperty<Double> THRUST = SimpleVisualProperty.ofDouble();
    public static final IVisualProperty<Double> KEROSENE = SimpleVisualProperty.ofDouble();
    public static final IVisualProperty<Double> LIQUID_OXYGEN = SimpleVisualProperty.ofDouble();
    public static final IVisualProperty<Double> ENERGY = SimpleVisualProperty.ofDouble();

    public static void register() {
        MinecraftForge.EVENT_BUS.post(new RegisterVisualPropertiesEvent(REGISTRY));
    }

    @SubscribeEvent
    public static void registerProperties(RegisterVisualPropertiesEvent event) {
        event.register("body_data", BODY_DATA);
        event.register("mass", MASS);
        event.register("thrust", THRUST);
        event.register("kerosene", KEROSENE);
        event.register("liquid_oxygen", LIQUID_OXYGEN);
        event.register("energy", ENERGY);
    }

    public static boolean isRegistered(IVisualProperty<?> property) {
        return REGISTRY.containsValue(property);
    }

    @Nullable
    public static IVisualProperty<?> byId(ResourceLocation identifier) {
        return REGISTRY.get(identifier);
    }

    @Nullable
    public static ResourceLocation getId(IVisualProperty<?> property) {
        return REGISTRY.inverse().get(property);
    }
}
