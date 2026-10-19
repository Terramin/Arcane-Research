package arcaneresearch.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import arcaneresearch.Arcaneresearch;

import java.util.Map;

// NEOFORGE_PORT: 1:1 value port from Forge 1.20.1 api/ArcaneresearchMaterials.java.
// Vanilla 26.x removed Tier/ArmorMaterial(interface)/ArmorItem:
//   Tier -> ToolMaterial RECORD (incorrectBlocksForDrops, durability=uses, speed,
//     attackDamageBonus, enchantmentValue, repairItems:TagKey<Item>).
//   Harvest level (getLevel 3/4) -> incorrect-blocks tag: 3 = diamond tier, 4 = netherite tier.
//   ArmorMaterial(interface) -> ArmorMaterial RECORD (durability factor, defense map,
//     enchantmentValue, equipSound:Holder<SoundEvent>, toughness, knockbackResistance,
//     repairIngredient:TagKey<Item>, assetId:ResourceKey<EquipmentAsset>).
//   Repair Ingredient.of(item) -> mod repair tags (data/arcaneresearch/tags/item/repairs_*.json,
//     values = the same items Forge used; cultist stays EMPTY via empty tag).
//   getName()/asset textures -> EquipmentAssets keys arcaneresearch:<name>;
//     equipment datapack JSONs are a resources-phase TODO (see TODO_State.md).
// All numeric values (uses/speed/damage/enchant/durability/protection/toughness/knockback)
// copied VERBATIM from Forge.
public class ArcaneresearchMaterials {

    private static TagKey<Item> repairTag(String path) {
        return TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, path));
    }

    public static final ToolMaterial TOOLMAT_THAUMIUM = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            500, 7.0F, 2.5F, 22,
            repairTag("repairs_thaumium"));

    public static final ToolMaterial TOOLMAT_VOID = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            150, 8.0F, 3.0F, 10,
            repairTag("repairs_void"));

    public static final ToolMaterial TOOLMAT_ELEMENTAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1500, 9.0F, 3.0F, 18,
            repairTag("repairs_thaumium"));

    private static ArmorMaterial armorMat(int durability, int[] protection, int enchantmentValue,
                                          net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> equipSound,
                                          float toughness, float knockbackResistance,
                                          TagKey<Item> repairIngredient, String assetPath) {
        return new ArmorMaterial(
                durability,
                Map.of(ArmorType.HELMET, protection[0],
                        ArmorType.CHESTPLATE, protection[1],
                        ArmorType.LEGGINGS, protection[2],
                        ArmorType.BOOTS, protection[3]),
                enchantmentValue,
                equipSound,
                toughness,
                knockbackResistance,
                repairIngredient,
                net.minecraft.resources.ResourceKey.create(EquipmentAssets.ROOT_ID,
                        Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, assetPath)));
    }

    public static final ArmorMaterial ARMORMAT_THAUMIUM = armorMat(
            25, new int[]{2, 5, 6, 2}, 25, SoundEvents.ARMOR_EQUIP_IRON,
            1.0F, 0.0F, repairTag("repairs_thaumium"), "thaumium");

    public static final ArmorMaterial ARMORMAT_SPECIAL = armorMat(
            25, new int[]{1, 2, 3, 1}, 25, SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F, 0.0F, repairTag("repairs_enchanted_fabric"), "special");

    public static final ArmorMaterial ARMORMAT_VOID = armorMat(
            10, new int[]{3, 6, 8, 3}, 10, SoundEvents.ARMOR_EQUIP_CHAIN,
            1.0F, 0.0F, repairTag("repairs_void"), "void");

    public static final ArmorMaterial ARMORMAT_VOIDROBE = armorMat(
            18, new int[]{4, 7, 9, 4}, 10, SoundEvents.ARMOR_EQUIP_LEATHER,
            2.0F, 0.0F, repairTag("repairs_void"), "void_robe");

    public static final ArmorMaterial ARMORMAT_FORTRESS = armorMat(
            40, new int[]{3, 6, 7, 3}, 25, SoundEvents.ARMOR_EQUIP_IRON,
            3.0F, 0.1F, repairTag("repairs_thaumium"), "fortress");

    public static final ArmorMaterial ARMORMAT_CULTIST_PLATE = armorMat(
            18, new int[]{2, 5, 6, 2}, 13, SoundEvents.ARMOR_EQUIP_IRON,
            0.0F, 0.0F, repairTag("repairs_cultist"), "cultist_plate");

    public static final ArmorMaterial ARMORMAT_CULTIST_ROBE = armorMat(
            17, new int[]{2, 4, 5, 2}, 13, SoundEvents.ARMOR_EQUIP_CHAIN,
            0.0F, 0.0F, repairTag("repairs_cultist"), "cultist_robe");

    public static final ArmorMaterial ARMORMAT_CULTIST_LEADER = armorMat(
            30, new int[]{3, 6, 7, 3}, 20, SoundEvents.ARMOR_EQUIP_IRON,
            1.0F, 0.0F, repairTag("repairs_cultist"), "cultist_leader");
}
