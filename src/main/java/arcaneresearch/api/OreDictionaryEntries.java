package arcaneresearch.api;
import net.minecraft.resources.ResourceLocation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
public class OreDictionaryEntries {
    public static final Map<String, ResourceLocation> ORE_TAGS;
    static {
        Map<String, ResourceLocation> tags = new LinkedHashMap<>();
        tags.put("oreAmber", ResourceLocation.fromNamespaceAndPath("forge", "ores/amber"));
        tags.put("oreCinnabar", ResourceLocation.fromNamespaceAndPath("forge", "ores/cinnabar"));
        tags.put("oreQuartz", ResourceLocation.fromNamespaceAndPath("forge", "ores/quartz"));
        tags.put("oreCrystalAir", ResourceLocation.fromNamespaceAndPath("forge", "ores/crystal_air"));
        tags.put("oreCrystalEarth", ResourceLocation.fromNamespaceAndPath("forge", "ores/crystal_earth"));
        tags.put("oreCrystalWater", ResourceLocation.fromNamespaceAndPath("forge", "ores/crystal_water"));
        tags.put("oreCrystalFire", ResourceLocation.fromNamespaceAndPath("forge", "ores/crystal_fire"));
        tags.put("oreCrystalOrder", ResourceLocation.fromNamespaceAndPath("forge", "ores/crystal_order"));
        tags.put("oreCrystalEntropy", ResourceLocation.fromNamespaceAndPath("forge", "ores/crystal_entropy"));
        tags.put("oreCrystalTaint", ResourceLocation.fromNamespaceAndPath("forge", "ores/crystal_taint"));
        tags.put("logWood", ResourceLocation.fromNamespaceAndPath("minecraft", "logs"));
        tags.put("plankWood", ResourceLocation.fromNamespaceAndPath("minecraft", "planks"));
        tags.put("slabWood", ResourceLocation.fromNamespaceAndPath("minecraft", "wooden_slabs"));
        tags.put("treeSapling", ResourceLocation.fromNamespaceAndPath("minecraft", "saplings"));
        tags.put("treeLeaves", ResourceLocation.fromNamespaceAndPath("minecraft", "leaves"));
        tags.put("nitor", ResourceLocation.fromNamespaceAndPath("arcaneresearch", "nitor"));
        tags.put("gemAmber", ResourceLocation.fromNamespaceAndPath("forge", "gems/amber"));
        tags.put("quicksilver", ResourceLocation.fromNamespaceAndPath("forge", "quicksilver"));
        tags.put("nuggetIron", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/iron"));
        tags.put("nuggetCopper", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/copper"));
        tags.put("nuggetTin", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/tin"));
        tags.put("nuggetSilver", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/silver"));
        tags.put("nuggetLead", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/lead"));
        tags.put("nuggetQuicksilver", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/quicksilver"));
        tags.put("nuggetThaumium", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/thaumium"));
        tags.put("nuggetVoid", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/void_metal"));
        tags.put("nuggetBrass", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/brass"));
        tags.put("nuggetQuartz", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/quartz"));
        tags.put("nuggetMeat", ResourceLocation.fromNamespaceAndPath("forge", "nuggets/meat"));
        tags.put("ingotThaumium", ResourceLocation.fromNamespaceAndPath("forge", "ingots/thaumium"));
        tags.put("ingotVoid", ResourceLocation.fromNamespaceAndPath("forge", "ingots/void_metal"));
        tags.put("ingotBrass", ResourceLocation.fromNamespaceAndPath("forge", "ingots/brass"));
        tags.put("plateIron", ResourceLocation.fromNamespaceAndPath("forge", "plates/iron"));
        tags.put("plateBrass", ResourceLocation.fromNamespaceAndPath("forge", "plates/brass"));
        tags.put("plateThaumium", ResourceLocation.fromNamespaceAndPath("forge", "plates/thaumium"));
        tags.put("plateVoid", ResourceLocation.fromNamespaceAndPath("forge", "plates/void"));
        tags.put("clusterIron", ResourceLocation.fromNamespaceAndPath("forge", "clusters/iron"));
        tags.put("clusterGold", ResourceLocation.fromNamespaceAndPath("forge", "clusters/gold"));
        tags.put("clusterCopper", ResourceLocation.fromNamespaceAndPath("forge", "clusters/copper"));
        tags.put("clusterTin", ResourceLocation.fromNamespaceAndPath("forge", "clusters/tin"));
        tags.put("clusterSilver", ResourceLocation.fromNamespaceAndPath("forge", "clusters/silver"));
        tags.put("clusterLead", ResourceLocation.fromNamespaceAndPath("forge", "clusters/lead"));
        tags.put("clusterCinnabar", ResourceLocation.fromNamespaceAndPath("forge", "clusters/cinnabar"));
        tags.put("clusterQuartz", ResourceLocation.fromNamespaceAndPath("forge", "clusters/quartz"));
        tags.put("blockThaumium", ResourceLocation.fromNamespaceAndPath("forge", "storage_blocks/thaumium"));
        tags.put("blockVoid", ResourceLocation.fromNamespaceAndPath("forge", "storage_blocks/void"));
        tags.put("blockBrass", ResourceLocation.fromNamespaceAndPath("forge", "storage_blocks/brass"));
        tags.put("trapdoorWood", ResourceLocation.fromNamespaceAndPath("minecraft", "wooden_trapdoors"));
        ORE_TAGS = Collections.unmodifiableMap(tags);
    }
    public static void initializeOreDictionary() {
    }
}
