package arcaneresearch.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT: new file (the one allowed new file in this batch). Holds the two tool
// materials that Forge defined inline in item classes (ItemCrimsonBlade.CRIMSON_VOID_TIER,
// ItemPrimalCrusher.PRIMAL_VOID_TIER). Forge Tier -> ToolMaterial RECORD
// (incorrectBlocksForDrops, durability = uses, speed, attackDamageBonus, enchantmentValue,
// repairItems:TagKey<Item>); all numeric values copied VERBATIM from the Forge sources.
// Harvest level (getLevel 4/5) -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL (top vanilla tier),
// the same mapping the already-ported ArcaneresearchMaterials uses for TOOLMAT_VOID (level 4).
// TOOLMAT_THAUMIUM/VOID/ELEMENTAL already exist in ArcaneresearchMaterials and are NOT
// duplicated here. Repair Ingredient.of(VOID_METAL_INGOT) -> repairs_void tag (same target
// item; the tag JSON itself is a resources-phase TODO, as noted in ArcaneresearchMaterials).
public class ArcaneresearchToolMaterials {

    private static TagKey<Item> repairTag(String path) {
        return TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, path));
    }

    public static final ToolMaterial TOOLMAT_CRIMSON_VOID = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            200, 8.0F, 3.5F, 20,
            repairTag("repairs_void"));

    public static final ToolMaterial TOOLMAT_PRIMAL_VOID = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            500, 8.0F, 4.0F, 20,
            repairTag("repairs_void"));
}
