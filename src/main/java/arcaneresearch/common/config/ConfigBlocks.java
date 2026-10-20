package arcaneresearch.common.config;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.utils.BlockUtils;
public class ConfigBlocks {
    public static void initMisc() {
        BlockUtils.portableHoleBlackList.add("minecraft:bed");
        BlockUtils.portableHoleBlackList.add("minecraft:piston");
        BlockUtils.portableHoleBlackList.add("minecraft:piston_head");
        BlockUtils.portableHoleBlackList.add("minecraft:sticky_piston");
        BlockUtils.portableHoleBlackList.add("minecraft:piston_extension");
        BlockUtils.portableHoleBlackList.add("minecraft:wooden_door");
        BlockUtils.portableHoleBlackList.add("minecraft:spruce_door");
        BlockUtils.portableHoleBlackList.add("minecraft:birch_door");
        BlockUtils.portableHoleBlackList.add("minecraft:jungle_door");
        BlockUtils.portableHoleBlackList.add("minecraft:acacia_door");
        BlockUtils.portableHoleBlackList.add("minecraft:dark_oak_door");
        BlockUtils.portableHoleBlackList.add("minecraft:iron_door");
        BlockUtils.portableHoleBlackList.add("arcaneresearch:infernal_furnace");
        Arcaneresearch.LOGGER.info("Registered {} portable hole blacklist entries",
                BlockUtils.portableHoleBlackList.size());
    }
}
