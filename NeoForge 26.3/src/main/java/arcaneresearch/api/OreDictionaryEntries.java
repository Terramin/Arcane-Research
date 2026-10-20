package arcaneresearch.api;

import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class OreDictionaryEntries {

    public static final Map<String, Identifier> ORE_TAGS;

    static {
        // NEOFORGE_PORT: Forge "forge:" tags -> NeoForge "c:" (TagConvention); paths 1:1.
        Map<String, Identifier> tags = new LinkedHashMap<>();
        tags.put("oreAmber", Identifier.fromNamespaceAndPath("c", "ores/amber"));
        tags.put("oreCinnabar", Identifier.fromNamespaceAndPath("c", "ores/cinnabar"));
        tags.put("oreQuartz", Identifier.fromNamespaceAndPath("c", "ores/quartz"));
        tags.put("oreCrystalAir", Identifier.fromNamespaceAndPath("c", "ores/crystal_air"));
        tags.put("oreCrystalEarth", Identifier.fromNamespaceAndPath("c", "ores/crystal_earth"));
        tags.put("oreCrystalWater", Identifier.fromNamespaceAndPath("c", "ores/crystal_water"));
        tags.put("oreCrystalFire", Identifier.fromNamespaceAndPath("c", "ores/crystal_fire"));
        tags.put("oreCrystalOrder", Identifier.fromNamespaceAndPath("c", "ores/crystal_order"));
        tags.put("oreCrystalEntropy", Identifier.fromNamespaceAndPath("c", "ores/crystal_entropy"));
        tags.put("oreCrystalTaint", Identifier.fromNamespaceAndPath("c", "ores/crystal_taint"));
        tags.put("logWood", Identifier.fromNamespaceAndPath("minecraft", "logs"));
        tags.put("plankWood", Identifier.fromNamespaceAndPath("minecraft", "planks"));
        tags.put("slabWood", Identifier.fromNamespaceAndPath("minecraft", "wooden_slabs"));
        tags.put("treeSapling", Identifier.fromNamespaceAndPath("minecraft", "saplings"));
        tags.put("treeLeaves", Identifier.fromNamespaceAndPath("minecraft", "leaves"));
        tags.put("nitor", Identifier.fromNamespaceAndPath("arcaneresearch", "nitor"));
        tags.put("gemAmber", Identifier.fromNamespaceAndPath("c", "gems/amber"));
        tags.put("quicksilver", Identifier.fromNamespaceAndPath("c", "quicksilver"));
        tags.put("nuggetIron", Identifier.fromNamespaceAndPath("c", "nuggets/iron"));
        tags.put("nuggetCopper", Identifier.fromNamespaceAndPath("c", "nuggets/copper"));
        tags.put("nuggetTin", Identifier.fromNamespaceAndPath("c", "nuggets/tin"));
        tags.put("nuggetSilver", Identifier.fromNamespaceAndPath("c", "nuggets/silver"));
        tags.put("nuggetLead", Identifier.fromNamespaceAndPath("c", "nuggets/lead"));
        tags.put("nuggetQuicksilver", Identifier.fromNamespaceAndPath("c", "nuggets/quicksilver"));
        tags.put("nuggetThaumium", Identifier.fromNamespaceAndPath("c", "nuggets/thaumium"));
        tags.put("nuggetVoid", Identifier.fromNamespaceAndPath("c", "nuggets/void_metal"));
        tags.put("nuggetBrass", Identifier.fromNamespaceAndPath("c", "nuggets/brass"));
        tags.put("nuggetQuartz", Identifier.fromNamespaceAndPath("c", "nuggets/quartz"));
        tags.put("nuggetMeat", Identifier.fromNamespaceAndPath("c", "nuggets/meat"));
        tags.put("ingotThaumium", Identifier.fromNamespaceAndPath("c", "ingots/thaumium"));
        tags.put("ingotVoid", Identifier.fromNamespaceAndPath("c", "ingots/void_metal"));
        tags.put("ingotBrass", Identifier.fromNamespaceAndPath("c", "ingots/brass"));
        tags.put("plateIron", Identifier.fromNamespaceAndPath("c", "plates/iron"));
        tags.put("plateBrass", Identifier.fromNamespaceAndPath("c", "plates/brass"));
        tags.put("plateThaumium", Identifier.fromNamespaceAndPath("c", "plates/thaumium"));
        tags.put("plateVoid", Identifier.fromNamespaceAndPath("c", "plates/void"));
        tags.put("clusterIron", Identifier.fromNamespaceAndPath("c", "clusters/iron"));
        tags.put("clusterGold", Identifier.fromNamespaceAndPath("c", "clusters/gold"));
        tags.put("clusterCopper", Identifier.fromNamespaceAndPath("c", "clusters/copper"));
        tags.put("clusterTin", Identifier.fromNamespaceAndPath("c", "clusters/tin"));
        tags.put("clusterSilver", Identifier.fromNamespaceAndPath("c", "clusters/silver"));
        tags.put("clusterLead", Identifier.fromNamespaceAndPath("c", "clusters/lead"));
        tags.put("clusterCinnabar", Identifier.fromNamespaceAndPath("c", "clusters/cinnabar"));
        tags.put("clusterQuartz", Identifier.fromNamespaceAndPath("c", "clusters/quartz"));
        tags.put("blockThaumium", Identifier.fromNamespaceAndPath("c", "storage_blocks/thaumium"));
        tags.put("blockVoid", Identifier.fromNamespaceAndPath("c", "storage_blocks/void"));
        tags.put("blockBrass", Identifier.fromNamespaceAndPath("c", "storage_blocks/brass"));
        tags.put("trapdoorWood", Identifier.fromNamespaceAndPath("minecraft", "wooden_trapdoors"));
        ORE_TAGS = Collections.unmodifiableMap(tags);
    }

    public static void initializeOreDictionary() {
    }
}
