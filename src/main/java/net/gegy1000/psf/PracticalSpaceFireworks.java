package net.gegy1000.psf;

import lombok.Getter;
import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.api.PSFAPIProps;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.server.ServerProxy;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static net.gegy1000.psf.PracticalSpaceFireworks.DEPENDENCIES;
import static net.gegy1000.psf.PracticalSpaceFireworks.MODID;
import static net.gegy1000.psf.PracticalSpaceFireworks.NAME;
import static net.gegy1000.psf.PracticalSpaceFireworks.VERSION;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod(modid = MODID, name = NAME, version = VERSION, dependencies = DEPENDENCIES, acceptedMinecraftVersions = "[1.12]")
public class PracticalSpaceFireworks {
    public static final String MODID = PSFAPIProps.MODID;
    public static final String VERSION = "0.2.0";
    public static final String NAME = "Practical Space Fireworks";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2807,15);required-after-client:ctm@[MC1.12.2-0.3.2.18,)";

    public static final String CLIENT_PROXY = "net.gegy1000.psf.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.psf.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final CreativeTabs TAB = new CreativeTabs(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(PSFBlocks.STRUT_CUBE);
        }
    };

    private static final ModFixs DATA_FIXER =
        FMLCommonHandler.instance().getDataFixer().init(MODID, 1);

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY; // todo encapsulate

    @Getter
    @Instance
    private static PracticalSpaceFireworks instance;

    @Getter
    private static boolean deobfuscatedEnvironment;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public static ResourceLocation namespace(String path) {
        checkArgument(!isNullOrEmpty(path));
        return new ResourceLocation(MODID, path);
    }

    public static String namespace(String path, char delimiter) {
        checkArgument(!isNullOrEmpty(path));
        return MODID + delimiter + path;
    }

    @EventHandler
    static void onPreInitialization(FMLPreInitializationEvent event) {
        deobfuscatedEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        initDataFixers();
        PROXY.onPreInit();
    }

    @EventHandler 
    static void onInitialization(FMLInitializationEvent event) {
        PROXY.onInit();
    }

    @EventHandler 
    static void onPostInitialization(FMLPostInitializationEvent event) {
        PROXY.onPostInit();
    }

    @EventHandler
    static void onServerStopped(FMLServerStoppedEvent event) {
        PROXY.getSatellites().flush();
    }

    private static void initDataFixers() {
        DATA_FIXER.registerFix(FixTypes.BLOCK_ENTITY, new IFixableData() {
            @Override
            public int getFixVersion() {
                return 1;
            }

            @Override
            public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
                val id = compound.getString("id");
                if (id.startsWith(MODID + '.')) {
                    compound.setString("id", id.replace(MODID + '.', MODID + ':'));
                } else if (id.equals(MODID + ":controller.simple")) {
                    compound.setString("id", MODID + ":controller_simple");
                }
                return compound;
            }
        });
    }
}
