package arcaneresearch.common.items.armor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IRunicArmor;

// NEOFORGE_PORT: ArmorItem REMOVED in 26.x (verified via javap) -> plain Item with
// .humanoidArmor(material, type). ArmorItem.Type -> ArmorType; the worn piece is tracked in
// the armorType field (Item no longer knows its own slot).
public class ItemThaumiumArmor extends Item implements IRunicArmor {

    /**
     * Паритет TC4: вся таум-броня — IRunicArmor с нулевым базовым зарядом
     * (пул собирается из TC.RUNIC от рунического аугмента).
     */
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    // NEOFORGE_PORT: replaces the removed ArmorItem.getType() for per-piece logic.
    private final ArmorType armorType;

    public ItemThaumiumArmor(Item.Properties props, ArmorType type) {
        // NEOFORGE_PORT: super(material, type, props) -> super(props.humanoidArmor(material, type)).
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_THAUMIUM, type)
                // NEOFORGE_PORT (26.3): humanoidArmor() uses vanilla per-slot units (11/16/15/13);
                // Forge indexed {13,15,16,11} by ArmorItem.Type ordinal -> restore Forge durability.
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_THAUMIUM, type))
                .rarity(Rarity.UNCOMMON));
        this.armorType = type;
    }

    public static ItemThaumiumArmor createHelmet(Item.Properties props) {
        return new ItemThaumiumArmor(props, ArmorType.HELMET);
    }

    public static ItemThaumiumArmor createChestplate(Item.Properties props) {
        return new ItemThaumiumArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemThaumiumArmor createLeggings(Item.Properties props) {
        return new ItemThaumiumArmor(props, ArmorType.LEGGINGS);
    }

    public static ItemThaumiumArmor createBoots(Item.Properties props) {
        return new ItemThaumiumArmor(props, ArmorType.BOOTS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    // NEOFORGE_PORT: Forge isValidRepairItem accepted the thaumium ingot item (or the material match).
    // Item.isValidRepairItem was removed in 26.x (verified via javap - repair is now data-driven
    // via the ArmorMaterial repairIngredient tag); ARMORMAT_THAUMIUM maps to the repairs_thaumium
    // data tag (thaumium ingot, as Forge used), so no override is needed (or possible).

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.
}
