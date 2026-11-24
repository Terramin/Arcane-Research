package arcaneresearch.common.lib.events;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.research.KnowledgeFragmentDrops;
import arcaneresearch.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/**
 * Port of the Thaumcraft chest spawns for knowledge fragments: 3-6 in
 * stronghold libraries and 1-2 in the generic dungeon/mineshaft chests.
 * The original used WeightedRandomChestContent weights 20 and 4; the chances
 * below keep those weights relative to the vanilla chest pool.
 */
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KnowledgeFragmentLootEvents {

    private static final Set<ResourceLocation> GENERIC_CHESTS = Set.of(
            BuiltInLootTables.SIMPLE_DUNGEON,
            BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.JUNGLE_TEMPLE,
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.STRONGHOLD_CORRIDOR,
            BuiltInLootTables.STRONGHOLD_CROSSING);

    private KnowledgeFragmentLootEvents() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        boolean library = BuiltInLootTables.STRONGHOLD_LIBRARY.equals(name);
        boolean generic = GENERIC_CHESTS.contains(name);
        if ((!library && !generic) || ModItems.KNOWLEDGE_FRAGMENT == null) {
            return;
        }

        int min = library ? KnowledgeFragmentDrops.LIBRARY_MIN : KnowledgeFragmentDrops.OTHER_MIN;
        int max = library ? KnowledgeFragmentDrops.LIBRARY_MAX : KnowledgeFragmentDrops.OTHER_MAX;
        float chance = library
                ? KnowledgeFragmentDrops.STRONGHOLD_LIBRARY_CHANCE
                : KnowledgeFragmentDrops.OTHER_CHEST_CHANCE;

        // UniformGenerator is exclusive on its upper bound, so the dropped
        // count is inclusive [min,max] after the integer conversion.
        LootPool pool = LootPool.lootPool()
                .add(LootItem.lootTableItem(ModItems.KNOWLEDGE_FRAGMENT.get()))
                .setRolls(UniformGenerator.between(min, max + 1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .build();
        event.getTable().addPool(pool);
    }
}
