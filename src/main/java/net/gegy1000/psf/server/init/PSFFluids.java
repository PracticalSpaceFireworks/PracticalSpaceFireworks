package net.gegy1000.psf.server.init;

import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.awt.Color;
import java.util.Objects;

import lombok.Getter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.fluid.BlockPSFFluid;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = PracticalSpaceFireworks.MODID)
public enum PSFFluids {

    KEROSENE("kerosene", Material.WATER, new Color(248, 190, 74)) {

        @Override
        protected Fluid init(@Nonnull Fluid fluid) {
            return fluid.setDensity(810).setViscosity(810);
        }
    },
    LIQUID_OXYGEN("liquid_oxygen", Material.WATER, new Color(103, 175, 188)) {

        @Override
        protected Fluid init(@Nonnull Fluid fluid) {
            return fluid.setDensity(1141).setViscosity(1141);
        }
    },
    LIQUID_NITROGEN("liquid_nitrogen", Material.WATER, new Color(103, 175, 188)) {

        @Override
        protected Fluid init(@Nonnull Fluid fluid) {
            return fluid.setDensity(1141).setViscosity(1141);
        }
    },
    FILTERED_AIR("filtered_air", new Color(212, 227, 248)) {

        @Override
        protected Fluid init(@Nonnull Fluid fluid) {
            return fluid.setDensity(1).setViscosity(1).setGaseous(true);
        }
        
        @Override
        protected boolean hasBucket() {
            return false;
        }
    },
    COMPRESSED_AIR("compressed_air", new Color(212, 227, 248)) {

        @Override
        protected Fluid init(@Nonnull Fluid fluid) {
            return fluid.setDensity(1).setViscosity(1).setGaseous(true);
        }
        
        @Override
        protected boolean hasBucket() {
            return false;
        }
    },
    ;

    @Getter
    private @Nullable BlockPSFFluid block = null;
    @Getter
    private final @Nonnull String name;
    private final @Nonnull Color color;
    private final boolean hasBlock;
    protected final @Nonnull Material material;
    /**
     * INTERNAL ONLY - PERIOD
     */
    @Deprecated
    private Fluid fluidUnsafe;

    private PSFFluids(@Nonnull String name, @Nonnull Color color) {
        this(name, null, color);
    }

    private PSFFluids(@Nonnull String name, @Nullable Material material, @Nonnull Color color) {
        this.name = name;
        this.color = color;
        this.hasBlock = material != null;
        this.material = material == null ? Material.AIR : material;
    }

    protected abstract Fluid init(@Nonnull Fluid fluid);
    
    protected boolean hasBucket() {
        return true;
    }

    protected @Nonnull BlockPSFFluid init() {
        return new BlockPSFFluid(getFluid(), material);
    }

    public @Nonnull ResourceLocation getStill() {
        return namespace("blocks/" + name + "_still");
    }

    public @Nonnull ResourceLocation getFlowing() {
        return namespace("blocks/" + name + "_flow");
    }

    public @Nonnull Fluid getFluid() {
        return Objects.requireNonNull(FluidRegistry.getFluid(name), "Fluid missing: " + name);
    }

    public @Nonnull ItemStack getBucket() {
        return getBucket(getFluid());
    }

    public @Nonnull BlockPSFFluid getBlockNN() {
        return Objects.requireNonNull(block, "Block missing");
    }

    public static @Nonnull ItemStack getBucket(@Nonnull Fluid fluid) {
        final FluidStack fluidStack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
        try {
            fluidStack.getFluid();
        } catch (NullPointerException e) {
            throw new RuntimeException(
                    "The fluid " + fluid + " (" + fluid.getUnlocalizedName() + ") is registered in the FluidRegistry, but the FluidRegistry has no delegate for it. This is impossible.", e);
        }
        try {
            return FluidUtil.getFilledBucket(fluidStack);
        } catch (Exception e) {
            throw new RuntimeException(
                    "The fluid " + fluid + " (" + fluid.getUnlocalizedName() + ") is registered in the FluidRegistry, but crashes when put into a bucket. This is a bug in the mod it belongs to.", e);
        }
    }

    public static @Nonnull NonNullList<ItemStack> getBuckets() {
        NonNullList<ItemStack> result = NonNullList.create();
        for (PSFFluids fluid : values()) {
            if (FluidRegistry.getBucketFluids().contains(fluid.getFluid())) {
                result.add(getBucket(fluid.getFluid()));
            }
        }
        return result;
    }

    public static @Nonnull NonNullList<ItemStack> getAllBuckets() {
        NonNullList<ItemStack> result = NonNullList.create();
        for (Fluid fluid : FluidRegistry.getBucketFluids()) {
            if (fluid != null) {
                result.add(getBucket(fluid));
            }
        }
        return result;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerFluids(@Nonnull RegistryEvent.Register<Block> event) {
        for (PSFFluids fluid : values()) {
            // We need a hard reference to the Fluid to make sure we register a bucket for it
            Fluid f = new Fluid(fluid.name, fluid.getStill(), fluid.getFlowing(), fluid.color);
            f.setUnlocalizedName(namespace(fluid.name, '.'));
            FluidRegistry.registerFluid(fluid.init(f));
            fluid.fluidUnsafe = f;
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void registerBlocks(@Nonnull RegistryEvent.Register<Block> event) {
        for (PSFFluids fluid : values()) {
            if (fluid.hasBlock) {
                BlockPSFFluid block = fluid.init();
                block.setRegistryName(fluid.name);
                PSFBlocks.ALL_BLOCKS.add(block);
                event.getRegistry().register(fluid.block = block);
            }
        }
    }

    @SubscribeEvent
    public static void registerItems(@Nonnull RegistryEvent.Register<Item> event) {
        for (PSFFluids fluid : values()) {
            if (fluid.hasBucket()) {
                FluidRegistry.addBucketForFluid(fluid.fluidUnsafe);
            }
        }
    }
}
