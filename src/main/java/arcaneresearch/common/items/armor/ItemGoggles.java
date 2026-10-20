package arcaneresearch.common.items.armor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.api.items.IRevealer;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

// NEOFORGE_PORT: ArmorItem REMOVED in 26.x (verified via javap) -> plain Item with
// .humanoidArmor(material, type). Curios API (ICurioItem/SlotContext) kept as-is - signatures
// verified unchanged in curios-neoforge 17.0.0-beta.2+26.3.
public class ItemGoggles extends Item implements IVisDiscountGear, IRevealer, IGoggles, ICurioItem, IRunicArmor {

    /**
     * Паритет TC4: вся таум-броня — IRunicArmor с нулевым базовым зарядом
     * (пул собирается из TC.RUNIC от рунического аугмента).
     */
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    public ItemGoggles(Item.Properties props) {
        // NEOFORGE_PORT: super(material, Type.HELMET, props) -> super(props.humanoidArmor(material, ArmorType.HELMET)).
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_SPECIAL, ArmorType.HELMET)
                // Forge isValidRepairItem: brass ingot or the material's enchanted fabric.
                .repairable(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath(arcaneresearch.Arcaneresearch.MODID, "repairs_goggles")))
                .stacksTo(1)
                .durability(350)
                .rarity(Rarity.RARE));
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 5;
    }

    @Override
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
        return true;
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
        return true;
    }

    // NEOFORGE_PORT: Forge isValidRepairItem (brass ingot || material match) -> REPAIRABLE
    // component with the repairs_goggles tag (brass ingot + #repairs_enchanted_fabric).

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("head");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
