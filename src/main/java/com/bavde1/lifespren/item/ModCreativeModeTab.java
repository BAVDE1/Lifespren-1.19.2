package com.bavde1.lifespren.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCreativeModeTab {
    public static final CreativeModeTab LIFESPREN_TAB = new CreativeModeTab("lifespren_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.ACACIA_BUTTON);
        }
    };
}
