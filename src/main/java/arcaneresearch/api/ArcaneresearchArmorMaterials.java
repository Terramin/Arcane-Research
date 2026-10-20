package arcaneresearch.api;

import net.minecraft.world.item.equipment.ArmorMaterial;

// NEOFORGE_PORT: armor-phase alias holder. The canonical 1:1 conversions of the Forge
// ArcaneresearchMaterials armor values (durability factor / defense map / enchantability /
// equip sound / toughness / knockback resistance / repair tag / equipment-asset id) already
// live in ArcaneresearchMaterials (ported 1:1 from Forge 1.20.1
// api/ArcaneresearchMaterials.java); the fields below alias them so armor item classes
// keep stable, single-purpose references. Field names kept identical to Forge.
public class ArcaneresearchArmorMaterials {

    public static final ArmorMaterial ARMORMAT_THAUMIUM = ArcaneresearchMaterials.ARMORMAT_THAUMIUM;

    public static final ArmorMaterial ARMORMAT_SPECIAL = ArcaneresearchMaterials.ARMORMAT_SPECIAL;

    public static final ArmorMaterial ARMORMAT_VOID = ArcaneresearchMaterials.ARMORMAT_VOID;

    public static final ArmorMaterial ARMORMAT_VOIDROBE = ArcaneresearchMaterials.ARMORMAT_VOIDROBE;

    public static final ArmorMaterial ARMORMAT_FORTRESS = ArcaneresearchMaterials.ARMORMAT_FORTRESS;

    public static final ArmorMaterial ARMORMAT_CULTIST_PLATE = ArcaneresearchMaterials.ARMORMAT_CULTIST_PLATE;

    public static final ArmorMaterial ARMORMAT_CULTIST_ROBE = ArcaneresearchMaterials.ARMORMAT_CULTIST_ROBE;

    public static final ArmorMaterial ARMORMAT_CULTIST_LEADER = ArcaneresearchMaterials.ARMORMAT_CULTIST_LEADER;
}
