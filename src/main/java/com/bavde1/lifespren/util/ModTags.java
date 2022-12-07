package com.bavde1.lifespren.util;

import com.bavde1.lifespren.LifesprenMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {

        //TAGS HERE
        public static final TagKey<Block> LIFESPREN_ATTRACTING_BLOCKS
                = tag("lifespren_attracting_blocks");

        public static final TagKey<Block> LIFESPREN_LANTERN_BONEMEALABLE_CROPS
                = tag("lifespren_lantern_bonemealable_crops");

        //tag stuff
        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(LifesprenMod.MOD_ID, name));
        }
        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class Items {

        //TAGS HERE

        //tag stuff
        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(LifesprenMod.MOD_ID, name));
        }
        private static TagKey<Item> forgeTag(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }
    }
}
