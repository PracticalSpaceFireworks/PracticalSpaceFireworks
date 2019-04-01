package net.gegy1000.psf.server.item;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Locale;

import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemCraftingMaterial extends Item {
    public ItemCraftingMaterial() {
        setCreativeTab(PracticalSpaceFireworks.TAB);
        setHasSubtypes(true);
    }

    public String getTranslationKey(ItemStack stack) {
        val meta = stack.getMetadata();
        if (meta >= 0 && meta < CraftingMaterial.VALUES.length) {
            return "item." + CraftingMaterial.VALUES[meta].getTranslationKey();
        }
        return super.getTranslationKey(stack);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (val material : CraftingMaterial.MATERIALS) {
                items.add(getItemStack(material));
            }
        }
    }

    public ItemStack getItemStack(CraftingMaterial material) {
        return new ItemStack(this, 1, material.ordinal());
    }

    public enum CraftingMaterial {
        MIRROR,
        LIGHT_FILAMENT,
        ;

        private static final CraftingMaterial[] VALUES = values();

        private static final ImmutableSet<CraftingMaterial> MATERIALS =
            Arrays.stream(VALUES).collect(Sets.toImmutableEnumSet());

        @Getter private final String name = toString().toLowerCase(Locale.ROOT);
        @Getter private final String translationKey = namespace(name, '.');
    }
}
