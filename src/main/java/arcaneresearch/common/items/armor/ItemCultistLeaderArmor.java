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
public class ItemCultistLeaderArmor extends Item implements IRunicArmor {

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

    public ItemCultistLeaderArmor(Item.Properties props, ArmorType type) {
        // NEOFORGE_PORT: super(material, type, props) -> super(props.humanoidArmor(material, type)).
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_CULTIST_LEADER, type)
                // NEOFORGE_PORT (26.3): humanoidArmor() uses vanilla per-slot units (11/16/15/13);
                // Forge indexed {13,15,16,11} by ArmorItem.Type ordinal -> restore Forge durability.
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_CULTIST_LEADER, type))
                .stacksTo(1)
                .rarity(Rarity.RARE));
        this.armorType = type;
    }

    public static ItemCultistLeaderArmor createHelmet(Item.Properties props) {
        return new ItemCultistLeaderArmor(props, ArmorType.HELMET);
    }

    public static ItemCultistLeaderArmor createChestplate(Item.Properties props) {
        return new ItemCultistLeaderArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemCultistLeaderArmor createLeggings(Item.Properties props) {
        return new ItemCultistLeaderArmor(props, ArmorType.LEGGINGS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    // NEOFORGE_PORT: Forge isValidRepairItem accepted Items.IRON_INGOT (or the material match).
    // Item.isValidRepairItem was removed in 26.x (verified via javap - repair is now data-driven
    // via the ArmorMaterial repairIngredient tag); cultist materials intentionally map to the
    // repairs_cultist data tag (iron ingot, as Forge used), so no override is needed.

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.
}
