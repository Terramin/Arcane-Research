package arcaneresearch.api;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Tier;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.init.ModItems;
import java.util.function.Supplier;
public class ArcaneresearchMaterials {
    public static final Tier TOOLMAT_THAUMIUM = new Tier() {
        @Override public int getUses() { return 500; }
        @Override public float getSpeed() { return 7.0F; }
        @Override public float getAttackDamageBonus() { return 2.5F; }
        @Override public int getLevel() { return 3; }
        @Override public int getEnchantmentValue() { return 22; }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.THAUMIUM_INGOT.get());
        }
    };
    public static final Tier TOOLMAT_VOID = new Tier() {
        @Override public int getUses() { return 150; }
        @Override public float getSpeed() { return 8.0F; }
        @Override public float getAttackDamageBonus() { return 3.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 10; }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.VOID_METAL_INGOT.get());
        }
    };
    public static final Tier TOOLMAT_ELEMENTAL = new Tier() {
        @Override public int getUses() { return 1500; }
        @Override public float getSpeed() { return 9.0F; }
        @Override public float getAttackDamageBonus() { return 3.0F; }
        @Override public int getLevel() { return 3; }
        @Override public int getEnchantmentValue() { return 18; }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.THAUMIUM_INGOT.get());
        }
    };
    public static final ArmorMaterial ARMORMAT_THAUMIUM = new ArmorMaterial() {
        private static final int[] DURABILITY = {13, 15, 16, 11};
        private static final int[] PROTECTION = {2, 5, 6, 2};
        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()] * 25;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION[type.ordinal()];
        }
        @Override public int getEnchantmentValue() { return 25; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_IRON;
        }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.THAUMIUM_INGOT.get());
        }
        @Override public String getName() { return Arcaneresearch.MODID + ":thaumium"; }
        @Override public float getToughness() { return 1.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    };
    public static final ArmorMaterial ARMORMAT_SPECIAL = new ArmorMaterial() {
        private static final int[] DURABILITY = {13, 15, 16, 11};
        private static final int[] PROTECTION = {1, 2, 3, 1};
        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()] * 25;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION[type.ordinal()];
        }
        @Override public int getEnchantmentValue() { return 25; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.ENCHANTED_FABRIC.get());
        }
        @Override public String getName() { return Arcaneresearch.MODID + ":special"; }
        @Override public float getToughness() { return 0.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    };
    public static final ArmorMaterial ARMORMAT_VOID = new ArmorMaterial() {
        private static final int[] DURABILITY = {13, 15, 16, 11};
        private static final int[] PROTECTION = {3, 6, 8, 3};
        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()] * 10;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION[type.ordinal()];
        }
        @Override public int getEnchantmentValue() { return 10; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_CHAIN;
        }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.VOID_METAL_INGOT.get());
        }
        @Override public String getName() { return Arcaneresearch.MODID + ":void"; }
        @Override public float getToughness() { return 1.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    };
    public static final ArmorMaterial ARMORMAT_VOIDROBE = new ArmorMaterial() {
        private static final int[] DURABILITY = {13, 15, 16, 11};
        private static final int[] PROTECTION = {4, 7, 9, 4};
        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()] * 18;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION[type.ordinal()];
        }
        @Override public int getEnchantmentValue() { return 10; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.VOID_METAL_INGOT.get());
        }
        @Override public String getName() { return Arcaneresearch.MODID + ":void_robe"; }
        @Override public float getToughness() { return 2.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    };
    public static final ArmorMaterial ARMORMAT_FORTRESS = new ArmorMaterial() {
        private static final int[] DURABILITY = {13, 15, 16, 11};
        private static final int[] PROTECTION = {3, 6, 7, 3};
        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()] * 40;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION[type.ordinal()];
        }
        @Override public int getEnchantmentValue() { return 25; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_IRON;
        }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.THAUMIUM_INGOT.get());
        }
        @Override public String getName() { return Arcaneresearch.MODID + ":fortress"; }
        @Override public float getToughness() { return 3.0F; }
        @Override public float getKnockbackResistance() { return 0.1F; }
    };
    public static final ArmorMaterial ARMORMAT_CULTIST_PLATE = new ArmorMaterial() {
        private static final int[] DURABILITY = {13, 15, 16, 11};
        private static final int[] PROTECTION = {2, 5, 6, 2};
        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()] * 18;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION[type.ordinal()];
        }
        @Override public int getEnchantmentValue() { return 13; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_IRON;
        }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
        @Override public String getName() { return Arcaneresearch.MODID + ":cultist_plate"; }
        @Override public float getToughness() { return 0.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    };
    public static final ArmorMaterial ARMORMAT_CULTIST_ROBE = new ArmorMaterial() {
        private static final int[] DURABILITY = {13, 15, 16, 11};
        private static final int[] PROTECTION = {2, 4, 5, 2};
        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()] * 17;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION[type.ordinal()];
        }
        @Override public int getEnchantmentValue() { return 13; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_CHAIN;
        }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
        @Override public String getName() { return Arcaneresearch.MODID + ":cultist_robe"; }
        @Override public float getToughness() { return 0.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    };
    public static final ArmorMaterial ARMORMAT_CULTIST_LEADER = new ArmorMaterial() {
        private static final int[] DURABILITY = {13, 15, 16, 11};
        private static final int[] PROTECTION = {3, 6, 7, 3};
        @Override public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()] * 30;
        }
        @Override public int getDefenseForType(ArmorItem.Type type) {
            return PROTECTION[type.ordinal()];
        }
        @Override public int getEnchantmentValue() { return 20; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_IRON;
        }
        @Override public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
        @Override public String getName() { return Arcaneresearch.MODID + ":cultist_leader"; }
        @Override public float getToughness() { return 1.0F; }
        @Override public float getKnockbackResistance() { return 0.0F; }
    };
}
