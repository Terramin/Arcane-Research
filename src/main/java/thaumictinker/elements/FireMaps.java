package thaumictinker.elements;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

 



 
public final class FireMaps {

    private static final Block[] DIRTS = {Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL};
    private static final Block[] SANDS = {Blocks.SAND, Blocks.RED_SAND};
    private static final Block[] STONES = {
            Blocks.STONE, Blocks.GRANITE, Blocks.POLISHED_GRANITE, Blocks.DIORITE,
            Blocks.POLISHED_DIORITE, Blocks.ANDESITE, Blocks.POLISHED_ANDESITE
    };
    private static final Block[] RED_FLOWERS = {
            Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
            Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP, Blocks.OXEYE_DAISY
    };
    private static final Block[] ORES = {
            Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE,
            Blocks.GOLD_ORE, Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE,
            Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE
    };

    private static Map<Block, Block> air;
    private static Map<Block, Block> earth;
    private static Map<Block, Block> fire;
    private static Map<Block, Block> water;
    private static Map<Block, Block> chaos;

    private FireMaps() {
    }

    public static synchronized Map<Block, Block> air() {
        if (air == null) {
            air = new HashMap<>();
            put(air, Blocks.SAND, BlockTags.LOGS_THAT_BURN);
            put(air, Blocks.SANDSTONE, BlockTags.LEAVES);
            put(air, Blocks.GLASS, Blocks.ICE);
            put(air, Blocks.SAND, DIRTS);
            put(air, Blocks.SAND, Blocks.GRASS_BLOCK);
        }
        return air;
    }

    public static synchronized Map<Block, Block> earth() {
        if (earth == null) {
            earth = new HashMap<>();
            put(earth, Blocks.DIRT, SANDS);
            put(earth, Blocks.CLAY, Blocks.GRAVEL);
            put(earth, Blocks.OAK_PLANKS, Blocks.NETHER_BRICKS);
            put(earth, Blocks.OAK_FENCE, Blocks.NETHER_BRICK_FENCE);
            put(earth, Blocks.OAK_STAIRS, Blocks.NETHER_BRICK_STAIRS);
            put(earth, Blocks.OAK_LOG, Blocks.CACTUS);
            put(earth, Blocks.GRASS, Blocks.SNOW);
            put(earth, Blocks.DIRT, STONES);
            put(earth, Blocks.IRON_BLOCK, Blocks.SPAWNER);
            put(earth, Blocks.DIRT, BlockTags.LOGS_THAT_BURN);
            put(earth, Blocks.DIRT, BlockTags.LEAVES);
            put(earth, Blocks.DIRT, Blocks.COBBLESTONE);
            put(earth, Blocks.DIRT, BlockTags.PLANKS);
            put(earth, Blocks.DIRT, Blocks.GLASS);
        }
        return earth;
    }

    public static synchronized Map<Block, Block> fire() {
        if (fire == null) {
            fire = new HashMap<>();
            put(fire, Blocks.NETHERRACK, Blocks.GRASS_BLOCK);
            put(fire, Blocks.NETHERRACK, DIRTS);
            put(fire, Blocks.SOUL_SAND, SANDS);
            put(fire, Blocks.SOUL_SAND, Blocks.GRAVEL);
            put(fire, Blocks.GLOWSTONE, Blocks.CLAY);
            put(fire, Blocks.NETHER_QUARTZ_ORE, ORES);
            put(fire, Blocks.LAVA, Blocks.WATER);
            put(fire, Blocks.BROWN_MUSHROOM, Blocks.WHEAT, Blocks.POTATOES, Blocks.CARROTS);
            put(fire, Blocks.RED_MUSHROOM, RED_FLOWERS);
        }
        return fire;
    }

    public static synchronized Map<Block, Block> water() {
        if (water == null) {
            water = new HashMap<>();
            put(water, Blocks.ICE, SANDS);
            put(water, Blocks.SNOW_BLOCK, Blocks.NETHERRACK);
            put(water, Blocks.ICE, Blocks.SOUL_SAND, Blocks.GLOWSTONE);
            put(water, Blocks.OBSIDIAN, Blocks.LAVA);
        }
        return water;
    }

    public static synchronized Map<Block, Block> chaos() {
        if (chaos == null) {
            chaos = new HashMap<>();
            put(chaos, Blocks.FIRE,
                    ElementsFeature.AER_FIRE, ElementsFeature.AQUA_FIRE,
                    ElementsFeature.TERRA_FIRE, ElementsFeature.IGNIS_FIRE,
                    ElementsFeature.ORDO_FIRE);
        }
        return chaos;
    }

    public static synchronized void reset() {
        air = null;
        earth = null;
        fire = null;
        water = null;
        chaos = null;
    }

    private static void put(Map<Block, Block> map, Block result, Block... sources) {
        for (Block source : sources) {
            map.put(source, result);
        }
    }

    private static void put(Map<Block, Block> map, Block result, TagKey<Block> tag) {
        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            map.put(holder.value(), result);
        }
    }
}
