package arcaneresearch.common.items.armor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IWarpingGear;

// NEOFORGE_PORT: ArmorItem REMOVED in 26.x (verified via javap) -> plain Item with
// .humanoidArmor(material, type). ArmorItem.Type -> ArmorType; the worn piece is tracked in
// the armorType field (Item no longer knows its own slot).
public class ItemVoidArmor extends Item implements IWarpingGear, IRunicArmor {

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

    public ItemVoidArmor(Item.Properties props, ArmorType type) {
        // NEOFORGE_PORT: super(material, type, props) -> super(props.humanoidArmor(material, type)).
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_VOID, type)
                // NEOFORGE_PORT (26.3): humanoidArmor() uses vanilla per-slot units (11/16/15/13);
                // Forge indexed {13,15,16,11} by ArmorItem.Type ordinal -> restore Forge durability.
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_VOID, type))
                .rarity(Rarity.UNCOMMON));
        this.armorType = type;
    }

    public static ItemVoidArmor createHelmet(Item.Properties props) {
        return new ItemVoidArmor(props, ArmorType.HELMET);
    }

    public static ItemVoidArmor createChestplate(Item.Properties props) {
        return new ItemVoidArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemVoidArmor createLeggings(Item.Properties props) {
        return new ItemVoidArmor(props, ArmorType.LEGGINGS);
    }

    public static ItemVoidArmor createBoots(Item.Properties props) {
        return new ItemVoidArmor(props, ArmorType.BOOTS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    // NEOFORGE_PORT: Forge isValidRepairItem accepted the void metal ingot item (or the material match).
    // Item.isValidRepairItem was removed in 26.x (verified via javap - repair is now data-driven
    // via the ArmorMaterial repairIngredient tag); ARMORMAT_VOID maps to the repairs_void data tag
    // (void metal ingot, as Forge used), so no override is needed (or possible).

    @Override
    // NEOFORGE_PORT: inventoryTick(ItemStack, Level, Entity, int, boolean) REMOVED in 26.x ->
    // inventoryTick(ItemStack, ServerLevel, Entity, EquipmentSlot) (server-only hook, so the old
    // !level.isClientSide() guard is implied).
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (stack.isDamaged() && entity != null
                && entity.tickCount % 20 == 0
                // NEOFORGE_PORT (26.3): Forge only ticked the player inventory (mob gear never
                // reached inventoryTick); 26.3 EntityEquipment.tick also ticks mob equipment.
                && entity instanceof Player) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.
}
