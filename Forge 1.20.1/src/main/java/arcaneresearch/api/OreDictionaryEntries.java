package arcaneresearch.api;
import net.minecraft.resources.ResourceLocation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
public class OreDictionaryEntries {
    public static final Map<String, ResourceLocation> ORE_TAGS;
    static {
        Map<String, ResourceLocation> tags = new LinkedHashMap<>();
        tags.put("oreAmber", new ResourceLocation("forge", "ores/amber"));
        tags.put("oreCinnabar", new ResourceLocation("forge", "ores/cinnabar"));
        tags.put("oreQuartz", new ResourceLocation("forge", "ores/quartz"));
        tags.put("oreCrystalAir", new ResourceLocation("forge", "ores/crystal_air"));
        tags.put("oreCrystalEarth", new ResourceLocation("forge", "ores/crystal_earth"));
        tags.put("oreCrystalWater", new ResourceLocation("forge", "ores/crystal_water"));
        tags.put("oreCrystalFire", new ResourceLocation("forge", "ores/crystal_fire"));
        tags.put("oreCrystalOrder", new ResourceLocation("forge", "ores/crystal_order"));
        tags.put("oreCrystalEntropy", new ResourceLocation("forge", "ores/crystal_entropy"));
        tags.put("oreCrystalTaint", new ResourceLocation("forge", "ores/crystal_taint"));
        tags.put("logWood", new ResourceLocation("minecraft", "logs"));
        tags.put("plankWood", new ResourceLocation("minecraft", "planks"));
        tags.put("slabWood", new ResourceLocation("minecraft", "wooden_slabs"));
        tags.put("treeSapling", new ResourceLocation("minecraft", "saplings"));
        tags.put("treeLeaves", new ResourceLocation("minecraft", "leaves"));
        tags.put("nitor", new ResourceLocation("arcaneresearch", "nitor"));
        tags.put("gemAmber", new ResourceLocation("forge", "gems/amber"));
        tags.put("quicksilver", new ResourceLocation("forge", "quicksilver"));
        tags.put("nuggetIron", new ResourceLocation("forge", "nuggets/iron"));
        tags.put("nuggetCopper", new ResourceLocation("forge", "nuggets/copper"));
        tags.put("nuggetTin", new ResourceLocation("forge", "nuggets/tin"));
        tags.put("nuggetSilver", new ResourceLocation("forge", "nuggets/silver"));
        tags.put("nuggetLead", new ResourceLocation("forge", "nuggets/lead"));
        tags.put("nuggetQuicksilver", new ResourceLocation("forge", "nuggets/quicksilver"));
        tags.put("nuggetThaumium", new ResourceLocation("forge", "nuggets/thaumium"));
        tags.put("nuggetVoid", new ResourceLocation("forge", "nuggets/void_metal"));
        tags.put("nuggetBrass", new ResourceLocation("forge", "nuggets/brass"));
        tags.put("nuggetQuartz", new ResourceLocation("forge", "nuggets/quartz"));
        tags.put("nuggetMeat", new ResourceLocation("forge", "nuggets/meat"));
        tags.put("ingotThaumium", new ResourceLocation("forge", "ingots/thaumium"));
        tags.put("ingotVoid", new ResourceLocation("forge", "ingots/void_metal"));
        tags.put("ingotBrass", new ResourceLocation("forge", "ingots/brass"));
        tags.put("plateIron", new ResourceLocation("forge", "plates/iron"));
        tags.put("plateBrass", new ResourceLocation("forge", "plates/brass"));
        tags.put("plateThaumium", new ResourceLocation("forge", "plates/thaumium"));
        tags.put("plateVoid", new ResourceLocation("forge", "plates/void"));
        tags.put("clusterIron", new ResourceLocation("forge", "clusters/iron"));
        tags.put("clusterGold", new ResourceLocation("forge", "clusters/gold"));
        tags.put("clusterCopper", new ResourceLocation("forge", "clusters/copper"));
        tags.put("clusterTin", new ResourceLocation("forge", "clusters/tin"));
        tags.put("clusterSilver", new ResourceLocation("forge", "clusters/silver"));
        tags.put("clusterLead", new ResourceLocation("forge", "clusters/lead"));
        tags.put("clusterCinnabar", new ResourceLocation("forge", "clusters/cinnabar"));
        tags.put("clusterQuartz", new ResourceLocation("forge", "clusters/quartz"));
        tags.put("blockThaumium", new ResourceLocation("forge", "storage_blocks/thaumium"));
        tags.put("blockVoid", new ResourceLocation("forge", "storage_blocks/void"));
        tags.put("blockBrass", new ResourceLocation("forge", "storage_blocks/brass"));
        tags.put("trapdoorWood", new ResourceLocation("minecraft", "wooden_trapdoors"));
        ORE_TAGS = Collections.unmodifiableMap(tags);
    }
    public static void initializeOreDictionary() {
    }
}
