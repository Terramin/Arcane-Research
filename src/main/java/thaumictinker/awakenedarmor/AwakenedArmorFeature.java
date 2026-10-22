package thaumictinker.awakenedarmor;


import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import arcaneresearch.api.ArcaneresearchMaterials;
import thaumictinker.gas.GasFeature;
import thaumictinker.kami.KamiFeature;
import thaumictinker.registry.TtRegistry;

 






 
public final class AwakenedArmorFeature {

    public static final String HELM_RESEARCH = "ICHORCLOTH_HELM_GEM";
    public static final String CHEST_RESEARCH = "ICHORCLOTH_CHEST_GEM";
    public static final String LEGS_RESEARCH = "ICHORCLOTH_LEGS_GEM";
    public static final String BOOTS_RESEARCH = "ICHORCLOTH_BOOTS_GEM";

    public static final ArmorMaterial AWAKENED_ARMOR = new ArmorMaterial() {
        private static final int[] DEFENSE = {3, 8, 6, 3};
         
 
        private static final int[] DURABILITY = {11, 16, 15, 13};

        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[type.ordinal()];
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return DEFENSE[type.ordinal()];
        }

        @Override
        public int getEnchantmentValue() {
            return 20;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public SoundEvent getEquipSound() {
            return ArcaneresearchMaterials.ARMORMAT_THAUMIUM.getEquipSound();
        }

        @Override
        public float getToughness() {
            return 0.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0F;
        }

        @Override
        public String getName() {
            
            
            return "thaumictinker:ichorcloth_gem";
        }
    };

    public static final ArmorItem COWL = (AwakenedCowlItem) TtRegistry.itemWithTab("cowl_of_the_abyssal_depths",
            () -> new AwakenedCowlItem(kamiProperties(), KamiFeature.HELMET_CHESTPLATE_LEGGINGS_DISCOUNT));

    public static final ArmorItem ROBES = (AwakenedArmorItem) TtRegistry.itemWithTab("robes_of_the_stratosphere",
            () -> new AwakenedArmorItem(ArmorItem.Type.CHESTPLATE, kamiProperties(),
                    KamiFeature.HELMET_CHESTPLATE_LEGGINGS_DISCOUNT));

    public static final ArmorItem LEGGINGS = (AwakenedArmorItem) TtRegistry.itemWithTab("leggings_of_the_burning_mantle",
            () -> new AwakenedArmorItem(ArmorItem.Type.LEGGINGS, kamiProperties(),
                    KamiFeature.HELMET_CHESTPLATE_LEGGINGS_DISCOUNT));

    public static final ArmorItem BOOTS = (AwakenedArmorItem) TtRegistry.itemWithTab("boots_of_the_horizontal_shield",
            () -> new AwakenedArmorItem(ArmorItem.Type.BOOTS, kamiProperties(), KamiFeature.BOOTS_DISCOUNT));

    private AwakenedArmorFeature() {
    }

    private static net.minecraft.world.item.Item.Properties kamiProperties() {
        return new net.minecraft.world.item.Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC);
    }

    public static void load() {
        
        
        GasFeature.registerLightCarrier(AwakenedArmor::carriesLight);
    }
}
