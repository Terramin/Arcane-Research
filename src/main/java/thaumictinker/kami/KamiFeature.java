package thaumictinker.kami;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import thaumictinker.registry.TtRegistry;

 





 
public final class KamiFeature {

    public static final String DIMENSION_SHARDS_RESEARCH = "DIMENSION_SHARDS";
    public static final String ICHOR_RESEARCH = "ICHOR";
    public static final String ICHOR_CLOTH_RESEARCH = "ICHOR_CLOTH";
    public static final String ICHORIUM_RESEARCH = "ICHORIUM";
    public static final String ICHOR_CAP_RESEARCH = "CAP_ICHOR";
    public static final String ICHORCLOTH_ROD_RESEARCH = "ROD_ICHORCLOTH";
    public static final String ICHORCLOTH_ARMOR_RESEARCH = "ICHORCLOTH_ARMOR";
    public static final String ICHOR_TOOLS_RESEARCH = "ICHOR_TOOLS";

    public static final String CAP_TAG = "ICHOR";
    public static final String ROD_TAG = "ICHORCLOTH";
    public static final float CAP_DISCOUNT = 0.7F;
    public static final int CAP_CRAFT_COST = 10;
    public static final int ROD_CAPACITY = 1000;
    public static final int ROD_CRAFT_COST = 10;
    public static final int HELMET_CHESTPLATE_LEGGINGS_DISCOUNT = 4;
    public static final int BOOTS_DISCOUNT = 3;

      
    public static final Tier ICHOR_TOOL = new Tier() {
        @Override
        public int getUses() {
            return 1;
        }

        @Override
        public float getSpeed() {
            return 10.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 5.0F;
        }

        @Override
        public int getLevel() {
            return 4;
        }

        @Override
        public int getEnchantmentValue() {
            return 25;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

      
    public static final ArmorMaterial ICHORCLOTH_ARMOR = new ArmorMaterial() {
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
        public net.minecraft.sounds.SoundEvent getEquipSound() {
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
            return "thaumictinker:ichorcloth";
        }
    };

    public static final Item ICHOR = TtRegistry.itemWithTab("ichor", () -> new Item(kami(64)));
    public static final Item ICHOR_CLOTH = TtRegistry.itemWithTab("ichor_cloth", () -> new Item(kami(64)));
    public static final Item ICHORIUM_INGOT = TtRegistry.itemWithTab("ichorium_ingot", () -> new Item(kami(64)));
    public static final Item ICHORIUM_NUGGET = TtRegistry.itemWithTab("ichorium_nugget", () -> new Item(kami(64)));
    public static final Item ICHORIUM_CAP = TtRegistry.itemWithTab("ichorium_cap", () -> new Item(kami(64)));
    public static final Item ICHORCLOTH_ROD = TtRegistry.itemWithTab("ichorcloth_rod", () -> new Item(kami(64)));
    public static final Item NETHER_SHARD = TtRegistry.itemWithTab("nether_shard", () -> new Item(plain(64)));
    public static final Item ENDER_SHARD = TtRegistry.itemWithTab("ender_shard", () -> new Item(plain(64)));

    public static final IchoriumTools.Pickaxe ICHORIUM_PICKAXE =
            (IchoriumTools.Pickaxe) TtRegistry.itemWithTab("ichorium_pickaxe",
                    () -> new IchoriumTools.Pickaxe(ICHOR_TOOL, kami(1)));

    public static final IchoriumTools.Shovel ICHORIUM_SHOVEL =
            (IchoriumTools.Shovel) TtRegistry.itemWithTab("ichorium_shovel",
                    () -> new IchoriumTools.Shovel(ICHOR_TOOL, kami(1)));

    public static final IchoriumTools.Axe ICHORIUM_AXE =
            (IchoriumTools.Axe) TtRegistry.itemWithTab("ichorium_axe",
                    () -> new IchoriumTools.Axe(ICHOR_TOOL, kami(1)));

    public static final IchoriumTools.Sword ICHORIUM_SWORD =
            (IchoriumTools.Sword) TtRegistry.itemWithTab("ichorium_sword",
                    () -> new IchoriumTools.Sword(ICHOR_TOOL, kami(1)));

    public static final IchorclothArmorItem ICHORCLOTH_COWL =
            (IchorclothArmorItem) TtRegistry.itemWithTab("ichorcloth_cowl",
                    () -> new IchorclothArmorItem(ArmorItem.Type.HELMET, kami(1),
                            HELMET_CHESTPLATE_LEGGINGS_DISCOUNT));

    public static final IchorclothArmorItem ICHORCLOTH_ROBE =
            (IchorclothArmorItem) TtRegistry.itemWithTab("ichorcloth_robe",
                    () -> new IchorclothArmorItem(ArmorItem.Type.CHESTPLATE, kami(1),
                            HELMET_CHESTPLATE_LEGGINGS_DISCOUNT));

    public static final IchorclothArmorItem ICHORCLOTH_LEGGINGS =
            (IchorclothArmorItem) TtRegistry.itemWithTab("ichorcloth_leggings",
                    () -> new IchorclothArmorItem(ArmorItem.Type.LEGGINGS, kami(1),
                            HELMET_CHESTPLATE_LEGGINGS_DISCOUNT));

    public static final IchorclothArmorItem ICHORCLOTH_BOOTS =
            (IchorclothArmorItem) TtRegistry.itemWithTab("ichorcloth_boots",
                    () -> new IchorclothArmorItem(ArmorItem.Type.BOOTS, kami(1), BOOTS_DISCOUNT));

    public static WandCap ichorCap;
    public static WandRod ichorclothRod;

    private KamiFeature() {
    }

    public static void load() {
    }

      
    public static void aspects() {
        AspectHelper.registerObjectTag(new net.minecraft.world.item.ItemStack(NETHER_SHARD), new AspectList()
                .add(Aspect.CRYSTAL, 1).add(Aspect.MAGIC, 1).add(Aspect.FIRE, 2).add(Aspect.ENTROPY, 2));
        AspectHelper.registerObjectTag(new net.minecraft.world.item.ItemStack(ENDER_SHARD), new AspectList()
                .add(Aspect.CRYSTAL, 1).add(Aspect.MAGIC, 1).add(Aspect.ELDRITCH, 2).add(Aspect.MOTION, 2));
    }

      
    public static WandCap ichorCap() {
        if (ichorCap == null) {
            try {
                ichorCap = new WandCap(CAP_TAG, CAP_DISCOUNT, 0,
                        new net.minecraft.world.item.ItemStack(ICHORIUM_CAP), CAP_CRAFT_COST,
                        new ResourceLocation("thaumictinker", "textures/model/capichor.png"));
            } catch (Throwable t) {
                thaumictinker.ThaumicTinker.LOGGER.warn("KamiFeature: ichorCap creation failed: {}", t.toString());
            }
        }
        return ichorCap;
    }

      
    public static WandRod ichorclothRod() {
        if (ichorclothRod == null) {
            try {
                ichorclothRod = new WandRod(ROD_TAG, ROD_CAPACITY,
                        new net.minecraft.world.item.ItemStack(ICHORCLOTH_ROD), ROD_CRAFT_COST,
                        new ResourceLocation("thaumictinker", "textures/model/rodichorcloth.png"));
            } catch (Throwable t) {
                thaumictinker.ThaumicTinker.LOGGER.warn("KamiFeature: ichorclothRod creation failed: {}", t.toString());
            }
        }
        return ichorclothRod;
    }

    private static Item.Properties kami(int stackSize) {
        return new Item.Properties().stacksTo(stackSize).rarity(Rarity.EPIC);
    }

    private static Item.Properties plain(int stackSize) {
        return new Item.Properties().stacksTo(stackSize);
    }
}
