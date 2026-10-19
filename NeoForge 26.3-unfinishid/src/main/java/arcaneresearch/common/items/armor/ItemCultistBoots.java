package arcaneresearch.common.items.armor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;

// NEOFORGE_PORT: ArmorItem REMOVED in 26.x (verified via javap) -> plain Item with
// .humanoidArmor(material, type). Vanilla ArmorMaterials.IRON kept (now the
// net.minecraft.world.item.equipment record holder).
public class ItemCultistBoots extends Item implements IVisDiscountGear, IWarpingGear, IRunicArmor {

    /**
     * Паритет TC4: вся таум-броня — IRunicArmor с нулевым базовым зарядом
     * (пул собирается из TC.RUNIC от рунического аугмента).
     */
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    public ItemCultistBoots(Item.Properties props) {
        // NEOFORGE_PORT: super(material, Type.BOOTS, props) -> super(props.humanoidArmor(material, ArmorType.BOOTS)).
        super(props.humanoidArmor(ArmorMaterials.IRON, ArmorType.BOOTS)
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    // TODO_PORT: Forge isValidRepairItem accepted Items.IRON_INGOT (or the material match).
    // Item.isValidRepairItem was removed in 26.x (verified via javap - repair is now data-driven
    // via the ArmorMaterial repairIngredient tag); IRON's vanilla REPAIRS_IRON_ARMOR tag covers
    // iron ingots, so no override is needed (or possible).

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 1;
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
}
