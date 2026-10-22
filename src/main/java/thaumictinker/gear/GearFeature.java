package thaumictinker.gear;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import thaumictinker.registry.TtRegistry;

 





 
public final class GearFeature {

    public static final String CLEANSING_TALISMAN_RESEARCH = "CLEANSING_TALISMAN";
    public static final String XP_TALISMAN_RESEARCH = "XP_TALISMAN";
    public static final String BLOOD_SWORD_RESEARCH = "BLOOD_SWORD";
    public static final String REVEALING_HELM_RESEARCH = "REVEALING_HELM";
    public static final String SHARE_TOME_RESEARCH = "SHARE_TOME";
    public static final String INFUSED_INKWELL_RESEARCH = "INFUSED_INKWELL";

      
    public static final Tier BLOOD_TOOL = new Tier() {
        @Override
        public int getUses() {
            return 950;
        }

        @Override
        public float getSpeed() {
            return 0.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.0F;
        }

        @Override
        public int getLevel() {
            return 1;
        }

        @Override
        public int getEnchantmentValue() {
            return 22;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    public static final CleansingTalismanItem CLEANSING_TALISMAN = (CleansingTalismanItem) TtRegistry.itemWithTab(
            "cleansing_talisman", () -> new CleansingTalismanItem(
                    new Item.Properties().durability(CleansingTalismanItem.USES).rarity(Rarity.UNCOMMON)));

    public static final XpTalismanItem XP_TALISMAN = (XpTalismanItem) TtRegistry.itemWithTab(
            "xp_talisman", () -> new XpTalismanItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final BloodSwordItem BLOOD_SWORD = (BloodSwordItem) TtRegistry.itemWithTab(
            "blood_sword", () -> new BloodSwordItem(BLOOD_TOOL,
                    new Item.Properties().durability(950).rarity(Rarity.UNCOMMON)));

    public static final RevealingHelmItem REVEALING_HELM = (RevealingHelmItem) TtRegistry.itemWithTab(
            "revealing_helm", () -> new RevealingHelmItem(
                    new Item.Properties().durability(500).rarity(Rarity.UNCOMMON)));

    public static final ShareTomeItem SHARE_TOME = (ShareTomeItem) TtRegistry.itemWithTab(
            "share_tome", () -> new ShareTomeItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final InfusedInkwellItem INFUSED_INKWELL = (InfusedInkwellItem) TtRegistry.item(
            "infused_inkwell", () -> new InfusedInkwellItem(new Item.Properties()));

    public static final net.minecraft.world.item.crafting.RecipeSerializer<net.minecraft.world.item.crafting.ShapedRecipe> SHARE_TOME_SERIALIZER =
            TtRegistry.recipeSerializer("share_tome", new ShareTomeRecipe.Serializer());

    private GearFeature() {
    }

    public static void load() {
    }
}
