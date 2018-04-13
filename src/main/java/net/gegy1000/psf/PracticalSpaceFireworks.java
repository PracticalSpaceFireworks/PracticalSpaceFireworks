package net.gegy1000.psf;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.gegy1000.psf.server.ServerProxy;
import net.gegy1000.psf.server.block.PSFBlockRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

import static net.gegy1000.psf.PracticalSpaceFireworks.*;

@Mod(modid = MODID, name = NAME, version = VERSION, acceptedMinecraftVersions = "[1.12]")
@ParametersAreNonnullByDefault
public class PracticalSpaceFireworks {
    @Nonnull
    public static final String MODID = "psf";
    public static final String VERSION = "0.2.0";
    public static final String NAME = "Practical Space Fireworks";

    public static final String CLIENT_PROXY = "net.gegy1000.psf.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.psf.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY;
    
    @Instance
    public static PracticalSpaceFireworks instance;

    private static boolean deobfuscatedEnvironment;

    @Nonnull
    public static final CreativeTabs TAB = new CreativeTabs(MODID) {
        @Override
        public @Nonnull ItemStack getTabIconItem() {
            return new ItemStack(PSFBlockRegistry.strut);
        }
    };

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public static boolean isDeobfuscatedEnvironment() {
        return deobfuscatedEnvironment;
    }

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        PROXY.onPreInit();
        Object o = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        if (o instanceof Boolean) {
            deobfuscatedEnvironment = (boolean) o;
        } else LOGGER.error("Failed to retrieve environment state from launch blackboard!");
    }

    @Mod.EventHandler
    public static void onInit(FMLInitializationEvent event) {
        PROXY.onInit();
    }

    @Mod.EventHandler
    public static void onPostInit(FMLPostInitializationEvent event) {
        PROXY.onPostInit();
    }
    
    @Mod.EventHandler
    public static void onServerStopped(FMLServerStoppedEvent event) {
        PROXY.getSatellites().flush();
    }
}
