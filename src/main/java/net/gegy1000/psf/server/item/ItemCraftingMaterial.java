package net.gegy1000.psf.server.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemCraftingMaterial extends Item {
    
    @RequiredArgsConstructor
    public enum CraftingMaterial {
        MIRROR("mirror"),
        LIGHT_SOURCE("light_source"),
        ;
        
        @Getter
        private final String name;
    }

    public ItemCraftingMaterial() {
        setCreativeTab(PracticalSpaceFireworks.TAB);
        setHasSubtypes(true);
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!isInCreativeTab(tab)) return;
        for (CraftingMaterial mat : CraftingMaterial.values()) {
            items.add(new ItemStack(this, 1, mat.ordinal()));
        }
    }
    
    public String getTranslationKey(ItemStack stack) {
        int meta = stack.getMetadata();
        if (meta < 0 || meta >= CraftingMaterial.values().length) {
            return super.getTranslationKey(stack);
        }
        return "item." + PracticalSpaceFireworks.MODID + "." + CraftingMaterial.values()[meta].getName();
    }
    
    public ItemStack ofMaterial(CraftingMaterial mat) {
        return new ItemStack(this, 1, mat.ordinal());
    }
}
