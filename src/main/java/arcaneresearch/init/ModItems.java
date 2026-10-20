package arcaneresearch.init;
import arcaneresearch.common.items.resources.EldritchEyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.construct.ItemTurretPlacer;
import arcaneresearch.common.golems.ItemGolemBell;
import arcaneresearch.common.golems.ItemGolemPlacer;
import arcaneresearch.common.golems.seals.ItemSealPlacer;
import arcaneresearch.common.items.armor.*;
import arcaneresearch.common.items.baubles.*;
import arcaneresearch.common.items.casters.ItemCaster;
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.items.casters.ItemFocusPouch;
import arcaneresearch.common.items.consumables.*;
import arcaneresearch.common.items.curios.*;
import arcaneresearch.common.items.misc.ItemCreativeFluxSponge;
import arcaneresearch.common.items.misc.ItemCrimsonRites;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.common.items.resources.ItemMagicDust;
import arcaneresearch.common.items.resources.ItemMaterial;
import arcaneresearch.common.items.resources.ItemTallow;
import arcaneresearch.common.items.tools.*;
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Arcaneresearch.MODID);
    public static final RegistryObject<Item> THAUMONOMICON = ITEMS.register("thaumonomicon",
            ItemThaumonomicon::new);
    public static final RegistryObject<Item> THAUMONOMICON_CHEAT = ITEMS.register("thaumonomicon_cheat",
            () -> new ItemThaumonomicon(true));
    public static final RegistryObject<Item> CRIMSON_RITES = ITEMS.register("crimson_rites",
            ItemCrimsonRites::new);
    public static final RegistryObject<Item> ELDRITCH_OBELISK_PLACER = ITEMS.register("eldritch_obelisk_placer",
            arcaneresearch.common.items.misc.ItemEldritchObeliskPlacer::new);
    public static final RegistryObject<Item> THAUMOMETER = ITEMS.register("thaumometer",
            ItemThaumometer::new);
    public static final RegistryObject<Item> SCRIBING_TOOLS = ITEMS.register("scribing_tools",
            ItemScribingTools::new);
    public static final RegistryObject<Item> PHIAL_EMPTY = ITEMS.register("phial_empty",
            ItemPhial::createEmpty);
    public static final RegistryObject<Item> PHIAL_FILLED = ITEMS.register("phial_filled",
            ItemPhial::createFilled);
    public static final RegistryObject<Item> QUICKSILVER = ITEMS.register("quicksilver",
            ItemMaterial::basic);
    public static final RegistryObject<Item> ALUMENTUM = ITEMS.register("alumentum",
            ItemAlumentum::new);
    public static final RegistryObject<Item> SALIS_MUNDUS = ITEMS.register("salis_mundus",
            ItemMagicDust::new);
    public static final RegistryObject<Item> THAUMIUM_INGOT = ITEMS.register("thaumium_ingot",
            ItemMaterial::basic);
    public static final RegistryObject<Item> THAUMIUM_NUGGET = ITEMS.register("thaumium_nugget",
            ItemMaterial::basic);
    public static final RegistryObject<Item> VOID_METAL_INGOT = ITEMS.register("void_metal_ingot",
            () -> new ItemMaterial(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> VOID_METAL_NUGGET = ITEMS.register("void_metal_nugget",
            () -> new ItemMaterial(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BRASS_INGOT = ITEMS.register("brass_ingot",
            ItemMaterial::basic);
    public static final RegistryObject<Item> BRASS_NUGGET = ITEMS.register("brass_nugget",
            ItemMaterial::basic);
    public static final RegistryObject<Item> COPPER_NUGGET = ITEMS.register("nugget_copper",
            ItemMaterial::basic);
    public static final RegistryObject<Item> TIN_NUGGET = ITEMS.register("nugget_tin",
            ItemMaterial::basic);
    public static final RegistryObject<Item> SILVER_NUGGET = ITEMS.register("nugget_silver",
            ItemMaterial::basic);
    public static final RegistryObject<Item> LEAD_NUGGET = ITEMS.register("nugget_lead",
            ItemMaterial::basic);
    public static final RegistryObject<Item> QUICKSILVER_NUGGET = ITEMS.register("quicksilver_nugget",
            ItemMaterial::basic);
    public static final RegistryObject<Item> QUARTZ_NUGGET = ITEMS.register("quartz_nugget",
            ItemMaterial::basic);
    public static final RegistryObject<Item> RAREEARTH_NUGGET = ITEMS.register("nugget_rareearth",
            ItemMaterial::basic);
    public static final RegistryObject<Item> AMBER = ITEMS.register("amber",
            ItemMaterial::basic);
    public static final RegistryObject<Item> ENCHANTED_FABRIC = ITEMS.register("enchanted_fabric",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> BLANK_SEAL = ITEMS.register("blank_seal",
            ItemMaterial::basic);
    public static final RegistryObject<Item> MIRRORED_GLASS = ITEMS.register("mirrored_glass",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> FILTER = ITEMS.register("filter",
            ItemMaterial::basic);
    public static final RegistryObject<Item> MORPHIC_RESONATOR = ITEMS.register("morphic_resonator",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> VIS_RESONATOR = ITEMS.register("vis_resonator",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> TALLOW = ITEMS.register("tallow",
            ItemTallow::new);
    public static final RegistryObject<Item> JAR_BRACE = ITEMS.register("jar_brace",
            ItemMaterial::basic);
    public static final RegistryObject<Item> MECHANISM_SIMPLE = ITEMS.register("mechanism_simple",
            ItemMaterial::basic);
    public static final RegistryObject<Item> MECHANISM_COMPLEX = ITEMS.register("mechanism_complex",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> PLATE_IRON = ITEMS.register("plate_iron",
            ItemMaterial::basic);
    public static final RegistryObject<Item> PLATE_BRASS = ITEMS.register("plate_brass",
            ItemMaterial::basic);
    public static final RegistryObject<Item> PLATE_THAUMIUM = ITEMS.register("plate_thaumium",
            ItemMaterial::basic);
    public static final RegistryObject<Item> PLATE_VOID = ITEMS.register("plate_void",
            ItemMaterial::rare);
    public static final RegistryObject<Item> CLUSTER_IRON = ITEMS.register("cluster_iron",
            ItemMaterial::basic);
    public static final RegistryObject<Item> CLUSTER_GOLD = ITEMS.register("cluster_gold",
            ItemMaterial::basic);
    public static final RegistryObject<Item> CLUSTER_COPPER = ITEMS.register("cluster_copper",
            ItemMaterial::basic);
    public static final RegistryObject<Item> CLUSTER_TIN = ITEMS.register("cluster_tin",
            ItemMaterial::basic);
    public static final RegistryObject<Item> CLUSTER_SILVER = ITEMS.register("cluster_silver",
            ItemMaterial::basic);
    public static final RegistryObject<Item> CLUSTER_LEAD = ITEMS.register("cluster_lead",
            ItemMaterial::basic);
    public static final RegistryObject<Item> CLUSTER_CINNABAR = ITEMS.register("cluster_cinnabar",
            ItemMaterial::basic);
    public static final RegistryObject<Item> CLUSTER_QUARTZ = ITEMS.register("cluster_quartz",
            ItemMaterial::basic);
    public static final RegistryObject<Item> BRAIN_CLOCKWORK = ITEMS.register("brain_clockwork",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> BRAIN_CURIOUS = ITEMS.register("brain_curious",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> CURIOSITY = ITEMS.register("curiosity",
            ItemMaterial::basic);
    public static final RegistryObject<Item> CURIO_ARCANE = ITEMS.register("curio_arcane",
            () -> new arcaneresearch.common.items.curios.ItemCurio("arcane"));
    public static final RegistryObject<Item> CURIO_PRESERVED = ITEMS.register("curio_preserved",
            () -> new arcaneresearch.common.items.curios.ItemCurio("preserved"));
    public static final RegistryObject<Item> CURIO_ANCIENT = ITEMS.register("curio_ancient",
            () -> new arcaneresearch.common.items.curios.ItemCurio("ancient"));
    public static final RegistryObject<Item> CURIO_ELDRITCH = ITEMS.register("curio_eldritch",
            () -> new arcaneresearch.common.items.curios.ItemCurio("eldritch"));
    public static final RegistryObject<Item> CURIO_KNOWLEDGE = ITEMS.register("curio_knowledge",
            () -> new arcaneresearch.common.items.curios.ItemCurio("knowledge"));
    public static final RegistryObject<Item> CURIO_TWISTED = ITEMS.register("curio_twisted",
            () -> new arcaneresearch.common.items.curios.ItemCurio("twisted"));
    public static final RegistryObject<Item> CURIO_RITES = ITEMS.register("curio_rites",
            () -> new arcaneresearch.common.items.curios.ItemCurio("rites"));
    public static final RegistryObject<Item> PRIMORDIAL_PEARL = ITEMS.register("primordial_pearl",
            ItemPrimordialPearl::new);
    public static final RegistryObject<Item> ELDRITCH_EYE = ITEMS.register("eldritch_eye",
            arcaneresearch.common.items.resources.EldritchEyeItem::new);
    public static final RegistryObject<Item> TAINT_SLIME = ITEMS.register("taint_slime",
            ItemMaterial::basic);
    public static final RegistryObject<Item> TAINT_TENDRIL = ITEMS.register("taint_tendril",
            ItemMaterial::basic);
    public static final RegistryObject<Item> ZOMBIE_BRAIN = ITEMS.register("zombie_brain",
            arcaneresearch.common.items.consumables.ItemZombieBrain::new);
    public static final RegistryObject<Item> VOID_SEED = ITEMS.register("void_seed",
            () -> new ItemMaterial(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> TRIPLE_MEAT_TREAT = ITEMS.register("triple_meat_treat",
            () -> new Item(new Item.Properties()
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(8)
                            .saturationMod(1.0f)
                            .meat()
                            .build())));
    public static final RegistryObject<Item> CHUNKS_BEEF = ITEMS.register("chunks_beef",
            ItemChunksBeef::new);
    public static final RegistryObject<Item> CHUNKS_CHICKEN = ITEMS.register("chunks_chicken",
            ItemChunksChicken::new);
    public static final RegistryObject<Item> CHUNKS_PORK = ITEMS.register("chunks_pork",
            ItemChunksPork::new);
    public static final RegistryObject<Item> CHUNKS_FISH = ITEMS.register("chunks_fish",
            ItemChunksFish::new);
    public static final RegistryObject<Item> CHUNKS_RABBIT = ITEMS.register("chunks_rabbit",
            ItemChunksRabbit::new);
    public static final RegistryObject<Item> CHUNKS_MUTTON = ITEMS.register("chunks_mutton",
            ItemChunksMutton::new);
    public static final RegistryObject<Item> GOGGLES = ITEMS.register("goggles",
            ItemGoggles::new);
    public static final RegistryObject<Item> CLOTH_CHEST = ITEMS.register("cloth_chest",
            ItemRobeArmor::createChest);
    public static final RegistryObject<Item> CLOTH_LEGS = ITEMS.register("cloth_legs",
            ItemRobeArmor::createLegs);
    public static final RegistryObject<Item> CLOTH_BOOTS = ITEMS.register("cloth_boots",
            ItemRobeArmor::createBoots);
    public static final RegistryObject<Item> THAUMIUM_SWORD = ITEMS.register("thaumium_sword",
            ItemThaumiumSword::new);
    public static final RegistryObject<Item> THAUMIUM_PICK = ITEMS.register("thaumium_pick",
            ItemThaumiumPickaxe::new);
    public static final RegistryObject<Item> THAUMIUM_AXE = ITEMS.register("thaumium_axe",
            ItemThaumiumAxe::new);
    public static final RegistryObject<Item> THAUMIUM_SHOVEL = ITEMS.register("thaumium_shovel",
            ItemThaumiumShovel::new);
    public static final RegistryObject<Item> THAUMIUM_HOE = ITEMS.register("thaumium_hoe",
            ItemThaumiumHoe::new);
    public static final RegistryObject<Item> VOID_SWORD = ITEMS.register("void_sword",
            ItemVoidSword::new);
    public static final RegistryObject<Item> VOID_PICK = ITEMS.register("void_pick",
            ItemVoidPickaxe::new);
    public static final RegistryObject<Item> VOID_AXE = ITEMS.register("void_axe",
            ItemVoidAxe::new);
    public static final RegistryObject<Item> VOID_SHOVEL = ITEMS.register("void_shovel",
            ItemVoidShovel::new);
    public static final RegistryObject<Item> VOID_HOE = ITEMS.register("void_hoe",
            ItemVoidHoe::new);
    public static final RegistryObject<Item> THAUMIUM_HELM = ITEMS.register("thaumium_helm",
            ItemThaumiumArmor::createHelmet);
    public static final RegistryObject<Item> THAUMIUM_CHEST = ITEMS.register("thaumium_chest",
            ItemThaumiumArmor::createChestplate);
    public static final RegistryObject<Item> THAUMIUM_LEGS = ITEMS.register("thaumium_legs",
            ItemThaumiumArmor::createLeggings);
    public static final RegistryObject<Item> THAUMIUM_BOOTS = ITEMS.register("thaumium_boots",
            ItemThaumiumArmor::createBoots);
    public static final RegistryObject<Item> VOID_HELM = ITEMS.register("void_helm",
            ItemVoidArmor::createHelmet);
    public static final RegistryObject<Item> VOID_CHEST = ITEMS.register("void_chest",
            ItemVoidArmor::createChestplate);
    public static final RegistryObject<Item> VOID_LEGS = ITEMS.register("void_legs",
            ItemVoidArmor::createLeggings);
    public static final RegistryObject<Item> VOID_BOOTS = ITEMS.register("void_boots",
            ItemVoidArmor::createBoots);
    public static final RegistryObject<Item> CASTER_BASIC = ITEMS.register("caster_basic",
            ItemCaster::createBasic);
    public static final RegistryObject<Item> FOCUS_1 = ITEMS.register("focus_1",
            ItemFocus::createBasic);
    public static final RegistryObject<Item> FOCUS_BLANK = ITEMS.register("focus_blank",
            ItemFocus::createBlank);
    public static final RegistryObject<Item> FOCUS_ADVANCED = ITEMS.register("focus_advanced",
            ItemFocus::createAdvanced);
    public static final RegistryObject<Item> TRAVELLER_BOOTS = ITEMS.register("traveller_boots",
            ItemBootsTraveller::new);
    public static final RegistryObject<Item> FORTRESS_HELM = ITEMS.register("fortress_helm",
            ItemFortressArmor::createHelmet);
    public static final RegistryObject<Item> FORTRESS_CHEST = ITEMS.register("fortress_chest",
            ItemFortressArmor::createChestplate);
    public static final RegistryObject<Item> FORTRESS_LEGS = ITEMS.register("fortress_legs",
            ItemFortressArmor::createLeggings);
    public static final RegistryObject<Item> VOID_ROBE_HELM = ITEMS.register("void_robe_helm",
            ItemVoidRobeArmor::createHelmet);
    public static final RegistryObject<Item> VOID_ROBE_CHEST = ITEMS.register("void_robe_chest",
            ItemVoidRobeArmor::createChestplate);
    public static final RegistryObject<Item> VOID_ROBE_LEGS = ITEMS.register("void_robe_legs",
            ItemVoidRobeArmor::createLeggings);
    public static final RegistryObject<Item> CRIMSON_ROBE_HELM = ITEMS.register("crimson_robe_helm",
            ItemCultistRobeArmor::createHelmet);
    public static final RegistryObject<Item> CRIMSON_ROBE_CHEST = ITEMS.register("crimson_robe_chest",
            ItemCultistRobeArmor::createChestplate);
    public static final RegistryObject<Item> CRIMSON_ROBE_LEGS = ITEMS.register("crimson_robe_legs",
            ItemCultistRobeArmor::createLeggings);
    public static final RegistryObject<Item> CRIMSON_PLATE_HELM = ITEMS.register("crimson_plate_helm",
            ItemCultistPlateArmor::createHelmet);
    public static final RegistryObject<Item> CRIMSON_PLATE_CHEST = ITEMS.register("crimson_plate_chest",
            ItemCultistPlateArmor::createChestplate);
    public static final RegistryObject<Item> CRIMSON_PLATE_LEGS = ITEMS.register("crimson_plate_legs",
            ItemCultistPlateArmor::createLeggings);
    public static final RegistryObject<Item> CRIMSON_BOOTS = ITEMS.register("crimson_boots",
            ItemCultistBoots::new);
    public static final RegistryObject<Item> CRIMSON_PRAETOR_HELM = ITEMS.register("crimson_praetor_helm",
            ItemCultistLeaderArmor::createHelmet);
    public static final RegistryObject<Item> CRIMSON_PRAETOR_CHEST = ITEMS.register("crimson_praetor_chest",
            ItemCultistLeaderArmor::createChestplate);
    public static final RegistryObject<Item> CRIMSON_PRAETOR_LEGS = ITEMS.register("crimson_praetor_legs",
            ItemCultistLeaderArmor::createLeggings);
    public static final RegistryObject<Item> AMULET_VIS_FOUND = ITEMS.register("amulet_vis_found",
            ItemAmuletVis::createFound);
    public static final RegistryObject<Item> AMULET_VIS_CRAFTED = ITEMS.register("amulet_vis_crafted",
            ItemAmuletVis::createCrafted);
    public static final RegistryObject<Item> CLOUD_RING = ITEMS.register("cloud_ring",
            ItemCloudRing::new);
    public static final RegistryObject<Item> CURIOSITY_BAND = ITEMS.register("curiosity_band",
            ItemCuriosityBand::new);
    public static final RegistryObject<Item> CHARM_UNDYING = ITEMS.register("charm_undying",
            ItemCharmUndying::new);
    public static final RegistryObject<Item> VERDANT_CHARM = ITEMS.register("verdant_charm",
            ItemVerdantCharm::createBasic);
    public static final RegistryObject<Item> VERDANT_CHARM_LIFE = ITEMS.register("verdant_charm_life",
            ItemVerdantCharm::createLife);
    public static final RegistryObject<Item> VERDANT_CHARM_SUSTAIN = ITEMS.register("verdant_charm_sustain",
            ItemVerdantCharm::createSustain);
    public static final RegistryObject<Item> VOIDSEER_CHARM = ITEMS.register("voidseer_charm",
            ItemVoidseerCharm::new);
    public static final RegistryObject<Item> BAUBLE_AMULET_MUNDANE = ITEMS.register("bauble_amulet_mundane",
            ItemBauble::amuletMundane);
    public static final RegistryObject<Item> BAUBLE_RING_MUNDANE = ITEMS.register("bauble_ring_mundane",
            ItemBauble::ringMundane);
    public static final RegistryObject<Item> BAUBLE_GIRDLE_MUNDANE = ITEMS.register("bauble_girdle_mundane",
            ItemBauble::girdleMundane);
    public static final RegistryObject<Item> BAUBLE_RING_APPRENTICE = ITEMS.register("bauble_ring_apprentice",
            ItemBauble::ringApprentice);
    public static final RegistryObject<Item> BAUBLE_AMULET_FANCY = ITEMS.register("bauble_amulet_fancy",
            ItemBauble::amuletFancy);
    public static final RegistryObject<Item> BAUBLE_RING_FANCY = ITEMS.register("bauble_ring_fancy",
            ItemBauble::ringFancy);
    public static final RegistryObject<Item> BAUBLE_GIRDLE_FANCY = ITEMS.register("bauble_girdle_fancy",
            ItemBauble::girdleFancy);
    public static final RegistryObject<Item> RUNIC_AMULET = ITEMS.register("runic_amulet",
            arcaneresearch.common.items.baubles.ItemRunicBauble::amulet);
    public static final RegistryObject<Item> RUNIC_AMULET_EMERGENCY = ITEMS.register("runic_amulet_emergency",
            arcaneresearch.common.items.baubles.ItemRunicBauble::amuletEmergency);
    public static final RegistryObject<Item> RUNIC_RING_LESSER = ITEMS.register("runic_ring_lesser",
            arcaneresearch.common.items.baubles.ItemRunicBauble::ringLesser);
    public static final RegistryObject<Item> RUNIC_RING = ITEMS.register("runic_ring",
            arcaneresearch.common.items.baubles.ItemRunicBauble::ring);
    public static final RegistryObject<Item> RUNIC_RING_CHARGED = ITEMS.register("runic_ring_charged",
            arcaneresearch.common.items.baubles.ItemRunicBauble::ringCharged);
    public static final RegistryObject<Item> RUNIC_RING_REGEN = ITEMS.register("runic_ring_regen",
            arcaneresearch.common.items.baubles.ItemRunicBauble::ringRegen);
    public static final RegistryObject<Item> RUNIC_GIRDLE = ITEMS.register("runic_girdle",
            arcaneresearch.common.items.baubles.ItemRunicBauble::girdle);
    public static final RegistryObject<Item> RUNIC_GIRDLE_KINETIC = ITEMS.register("runic_girdle_kinetic",
            arcaneresearch.common.items.baubles.ItemRunicBauble::girdleKinetic);
    public static final RegistryObject<Item> ENCHANTED_PLACEHOLDER = ITEMS.register("enchanted_placeholder",
            arcaneresearch.common.items.curios.ItemEnchantmentPlaceholder::new);
    public static final RegistryObject<Item> ELEMENTAL_PICK = ITEMS.register("elemental_pick",
            ItemElementalPickaxe::new);
    public static final RegistryObject<Item> ELEMENTAL_AXE = ITEMS.register("elemental_axe",
            ItemElementalAxe::new);
    public static final RegistryObject<Item> ELEMENTAL_SWORD = ITEMS.register("elemental_sword",
            ItemElementalSword::new);
    public static final RegistryObject<Item> ELEMENTAL_SHOVEL = ITEMS.register("elemental_shovel",
            ItemElementalShovel::new);
    public static final RegistryObject<Item> PRIMAL_CRUSHER = ITEMS.register("primal_crusher",
            ItemPrimalCrusher::new);
    public static final RegistryObject<Item> CRIMSON_BLADE = ITEMS.register("crimson_blade",
            ItemCrimsonBlade::new);
    public static final RegistryObject<Item> ELEMENTAL_HOE = ITEMS.register("elemental_hoe",
            ItemElementalHoe::new);
    public static final RegistryObject<Item> RESONATOR = ITEMS.register("resonator",
            ItemResonator::new);
    public static final RegistryObject<Item> SANITY_CHECKER = ITEMS.register("sanity_checker",
            ItemSanityChecker::new);
    public static final RegistryObject<Item> HAND_MIRROR = ITEMS.register("hand_mirror",
            ItemHandMirror::new);
    public static final RegistryObject<Item> GRAPPLE_GUN = ITEMS.register("grapple_gun",
            ItemGrappleGun::new);
    public static final RegistryObject<Item> BONE_BOW = ITEMS.register("bone_bow",
            arcaneresearch.common.items.tools.ItemBoneBow::new);
    public static final RegistryObject<Item> PRIMAL_ARROW_AIR = ITEMS.register("primal_arrow_air",
            () -> new arcaneresearch.common.items.tools.ItemPrimalArrow(
                    arcaneresearch.common.items.tools.ItemPrimalArrow.AIR));
    public static final RegistryObject<Item> PRIMAL_ARROW_FIRE = ITEMS.register("primal_arrow_fire",
            () -> new arcaneresearch.common.items.tools.ItemPrimalArrow(
                    arcaneresearch.common.items.tools.ItemPrimalArrow.FIRE));
    public static final RegistryObject<Item> PRIMAL_ARROW_WATER = ITEMS.register("primal_arrow_water",
            () -> new arcaneresearch.common.items.tools.ItemPrimalArrow(
                    arcaneresearch.common.items.tools.ItemPrimalArrow.WATER));
    public static final RegistryObject<Item> PRIMAL_ARROW_EARTH = ITEMS.register("primal_arrow_earth",
            () -> new arcaneresearch.common.items.tools.ItemPrimalArrow(
                    arcaneresearch.common.items.tools.ItemPrimalArrow.EARTH));
    public static final RegistryObject<Item> PRIMAL_ARROW_ORDER = ITEMS.register("primal_arrow_order",
            () -> new arcaneresearch.common.items.tools.ItemPrimalArrow(
                    arcaneresearch.common.items.tools.ItemPrimalArrow.ORDER));
    public static final RegistryObject<Item> PRIMAL_ARROW_ENTROPY = ITEMS.register("primal_arrow_entropy",
            () -> new arcaneresearch.common.items.tools.ItemPrimalArrow(
                    arcaneresearch.common.items.tools.ItemPrimalArrow.ENTROPY));
    public static final RegistryObject<Item> BATH_SALTS = ITEMS.register("bath_salts",
            ItemBathSalts::new);
    public static final RegistryObject<Item> SANITY_SOAP = ITEMS.register("sanity_soap",
            ItemSanitySoap::new);
    public static final RegistryObject<Item> BOTTLE_TAINT = ITEMS.register("bottle_taint",
            ItemBottleTaint::new);
    public static final RegistryObject<Item> CAUSALITY_COLLAPSER = ITEMS.register("causality_collapser",
            ItemCausalityCollapser::new);
    public static final RegistryObject<Item> LABEL_BLANK = ITEMS.register("label_blank",
            ItemLabel::createBlank);
    public static final RegistryObject<Item> LABEL_FILLED = ITEMS.register("label_filled",
            ItemLabel::createFilled);
    public static final RegistryObject<Item> LOOT_BAG_COMMON = ITEMS.register("loot_bag_common",
            ItemLootBag::createCommon);
    public static final RegistryObject<Item> LOOT_BAG_UNCOMMON = ITEMS.register("loot_bag_uncommon",
            ItemLootBag::createUncommon);
    public static final RegistryObject<Item> LOOT_BAG_RARE = ITEMS.register("loot_bag_rare",
            ItemLootBag::createRare);
    public static final RegistryObject<Item> PECH_WAND = ITEMS.register("pech_wand",
            ItemPechWand::new);
    public static final RegistryObject<Item> CELESTIAL_NOTES_SUN = ITEMS.register("celestial_notes_sun",
            ItemCelestialNotes::createSun);
    public static final RegistryObject<Item> CELESTIAL_NOTES_STARS_1 = ITEMS.register("celestial_notes_stars_1",
            ItemCelestialNotes::createStars1);
    public static final RegistryObject<Item> CELESTIAL_NOTES_STARS_2 = ITEMS.register("celestial_notes_stars_2",
            ItemCelestialNotes::createStars2);
    public static final RegistryObject<Item> CELESTIAL_NOTES_STARS_3 = ITEMS.register("celestial_notes_stars_3",
            ItemCelestialNotes::createStars3);
    public static final RegistryObject<Item> CELESTIAL_NOTES_STARS_4 = ITEMS.register("celestial_notes_stars_4",
            ItemCelestialNotes::createStars4);
    public static final RegistryObject<Item> CELESTIAL_NOTES_MOON_1 = ITEMS.register("celestial_notes_moon_1",
            ItemCelestialNotes::createMoon1);
    public static final RegistryObject<Item> CELESTIAL_NOTES_MOON_2 = ITEMS.register("celestial_notes_moon_2",
            ItemCelestialNotes::createMoon2);
    public static final RegistryObject<Item> CELESTIAL_NOTES_MOON_3 = ITEMS.register("celestial_notes_moon_3",
            ItemCelestialNotes::createMoon3);
    public static final RegistryObject<Item> CELESTIAL_NOTES_MOON_4 = ITEMS.register("celestial_notes_moon_4",
            ItemCelestialNotes::createMoon4);
    public static final RegistryObject<Item> CELESTIAL_NOTES_MOON_5 = ITEMS.register("celestial_notes_moon_5",
            ItemCelestialNotes::createMoon5);
    public static final RegistryObject<Item> CELESTIAL_NOTES_MOON_6 = ITEMS.register("celestial_notes_moon_6",
            ItemCelestialNotes::createMoon6);
    public static final RegistryObject<Item> CELESTIAL_NOTES_MOON_7 = ITEMS.register("celestial_notes_moon_7",
            ItemCelestialNotes::createMoon7);
    public static final RegistryObject<Item> CELESTIAL_NOTES_MOON_8 = ITEMS.register("celestial_notes_moon_8",
            ItemCelestialNotes::createMoon8);
    public static final RegistryObject<Item> FOCUS_POUCH = ITEMS.register("focus_pouch",
            ItemFocusPouch::new);
    public static final RegistryObject<Item> GOLEM_PLACER = ITEMS.register("golem_placer",
            ItemGolemPlacer::new);
    public static final RegistryObject<Item> GOLEM_BELL = ITEMS.register("golem_bell",
            ItemGolemBell::new);
    public static final RegistryObject<Item> SEAL_BLANK = ITEMS.register("seal_blank",
            ItemSealPlacer::createBlank);
    public static final RegistryObject<Item> SEAL_PICKUP = ITEMS.register("seal_pickup",
            () -> ItemSealPlacer.create("arcaneresearch:pickup"));
    public static final RegistryObject<Item> SEAL_EMPTY = ITEMS.register("seal_empty",
            () -> ItemSealPlacer.create("arcaneresearch:empty"));
    public static final RegistryObject<Item> SEAL_FILL = ITEMS.register("seal_fill",
            () -> ItemSealPlacer.create("arcaneresearch:fill"));
    public static final RegistryObject<Item> SEAL_GUARD = ITEMS.register("seal_guard",
            () -> ItemSealPlacer.create("arcaneresearch:guard"));
    public static final RegistryObject<Item> SEAL_BUTCHER = ITEMS.register("seal_butcher",
            () -> ItemSealPlacer.create("arcaneresearch:butcher"));
    public static final RegistryObject<Item> SEAL_HARVEST = ITEMS.register("seal_harvest",
            () -> ItemSealPlacer.create("arcaneresearch:harvest"));
    public static final RegistryObject<Item> SEAL_LUMBER = ITEMS.register("seal_lumber",
            () -> ItemSealPlacer.create("arcaneresearch:lumber"));
    public static final RegistryObject<Item> SEAL_BREAKER = ITEMS.register("seal_breaker",
            () -> ItemSealPlacer.create("arcaneresearch:breaker"));
    public static final RegistryObject<Item> SEAL_PROVIDE = ITEMS.register("seal_provide",
            () -> ItemSealPlacer.create("arcaneresearch:provider"));
    public static final RegistryObject<Item> SEAL_STOCK = ITEMS.register("seal_stock",
            () -> ItemSealPlacer.create("arcaneresearch:stock"));
    public static final RegistryObject<Item> SEAL_USE = ITEMS.register("seal_use",
            () -> ItemSealPlacer.create("arcaneresearch:use"));
    public static final RegistryObject<Item> SEAL_BREAKER_ADVANCED = ITEMS.register("seal_breaker_advanced",
            () -> ItemSealPlacer.create("arcaneresearch:breaker_advanced"));
    public static final RegistryObject<Item> SEAL_PICKUP_ADVANCED = ITEMS.register("seal_pickup_advanced",
            () -> ItemSealPlacer.create("arcaneresearch:pickup_advanced"));
    public static final RegistryObject<Item> SEAL_EMPTY_ADVANCED = ITEMS.register("seal_empty_advanced",
            () -> ItemSealPlacer.create("arcaneresearch:empty_advanced"));
    public static final RegistryObject<Item> SEAL_FILL_ADVANCED = ITEMS.register("seal_fill_advanced",
            () -> ItemSealPlacer.create("arcaneresearch:fill_advanced"));
    public static final RegistryObject<Item> SEAL_GUARD_ADVANCED = ITEMS.register("seal_guard_advanced",
            () -> ItemSealPlacer.create("arcaneresearch:guard_advanced"));
    public static final RegistryObject<Item> CRYSTAL_ESSENCE = ITEMS.register("crystal_essence",
            ItemCrystalEssence::new);
    public static final RegistryObject<Item> WISP_ESSENCE = ITEMS.register("wisp_essence",
            arcaneresearch.common.items.resources.ItemWispEssence::new);
    public static final RegistryObject<Item> TURRET_PLACER_BASIC = ITEMS.register("turret_placer_basic",
            ItemTurretPlacer::createBasic);
    public static final RegistryObject<Item> TURRET_PLACER_ADVANCED = ITEMS.register("turret_placer_advanced",
            ItemTurretPlacer::createAdvanced);
    public static final RegistryObject<Item> TURRET_PLACER_BORE = ITEMS.register("turret_placer_bore",
            ItemTurretPlacer::createBore);
    public static final RegistryObject<Item> GRAPPLE_GUN_SPOOL = ITEMS.register("grapple_gun_spool",
            ItemMaterial::basic);
    public static final RegistryObject<Item> GRAPPLE_GUN_TIP = ITEMS.register("grapple_gun_tip",
            ItemMaterial::basic);
    public static final RegistryObject<Item> GOLEM_MODULE_AGGRESSION = ITEMS.register("golem_module_aggression",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> GOLEM_MODULE_VISION = ITEMS.register("golem_module_vision",
            ItemMaterial::uncommon);
    public static final RegistryObject<Item> INHABITED_ZOMBIE_SPAWN_EGG = ITEMS.register("inhabited_zombie_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.INHABITED_ZOMBIE, 0x4E2A6B, 0x7A9E4F,
                    new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_GUARDIAN_SPAWN_EGG = ITEMS.register("eldritch_guardian_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ELDRITCH_GUARDIAN, 0x14141F, 0x7B2FF7,
                    new Item.Properties()));
    public static final RegistryObject<Item> WISP_SPAWN_EGG = ITEMS.register("wisp_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WISP, 0xE8F6FF, 0x66E0FF,
                    new Item.Properties()));
    public static final RegistryObject<Item> FIRE_BAT_SPAWN_EGG = ITEMS.register("fire_bat_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FIRE_BAT, 0x3A1414, 0xFF7B1C,
                    new Item.Properties()));
    public static final RegistryObject<Item> BRAINY_ZOMBIE_SPAWN_EGG = ITEMS.register("brainy_zombie_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BRAINY_ZOMBIE, 0x4E6B3A, 0xD77A9E,
                    new Item.Properties()));
    public static final RegistryObject<Item> MIND_SPIDER_SPAWN_EGG = ITEMS.register("mind_spider_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MIND_SPIDER, 0x2E1A3A, 0xC040C0,
                    new Item.Properties()));
    public static final RegistryObject<Item> THAUMIC_SLIME_SPAWN_EGG = ITEMS.register("thaumic_slime_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THAUMIC_SLIME, 0x5DA23A, 0xB5E61D,
                    new Item.Properties()));
    public static final RegistryObject<Item> GIANT_BRAINY_ZOMBIE_SPAWN_EGG = ITEMS.register("giant_brainy_zombie_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GIANT_BRAINY_ZOMBIE, 0x3A5229, 0xD77A9E,
                    new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_CRAB_SPAWN_EGG = ITEMS.register("eldritch_crab_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ELDRITCH_CRAB, 0x5A2E4E, 0xC0392B,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPELL_BAT_SPAWN_EGG = ITEMS.register("spell_bat_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SPELL_BAT, 0x2B2A5A, 0x4FD1FF,
                    new Item.Properties()));
    public static final RegistryObject<Item> CULTIST_SPAWN_EGG = ITEMS.register("cultist_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CULTIST, 0x8E1F2F, 0xD9B48F,
                    new Item.Properties()));
    public static final RegistryObject<Item> CULTIST_KNIGHT_SPAWN_EGG = ITEMS.register("cultist_knight_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CULTIST_KNIGHT, 0x5E1420, 0x9AA3B2,
                    new Item.Properties()));
    public static final RegistryObject<Item> CULTIST_CLERIC_SPAWN_EGG = ITEMS.register("cultist_cleric_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CULTIST_CLERIC, 0x8E1F2F, 0xE8C547,
                    new Item.Properties()));
    public static final RegistryObject<Item> PECH_SPAWN_EGG = ITEMS.register("pech_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PECH, 0x6B5B3E, 0xA8D05A,
                    new Item.Properties()));
    public static final RegistryObject<Item> TAINT_CRAWLER_SPAWN_EGG = ITEMS.register("taint_crawler_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TAINT_CRAWLER, 0x4A1E5E, 0x1A1A1A,
                    new Item.Properties()));
    public static final RegistryObject<Item> TAINT_SWARM_SPAWN_EGG = ITEMS.register("taint_swarm_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TAINT_SWARM, 0x5E2A7E, 0xFF4FD8,
                    new Item.Properties()));
    public static final RegistryObject<Item> CULTIST_LEADER_SPAWN_EGG = ITEMS.register("cultist_leader_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CULTIST_LEADER, 0x2A0E14, 0xE8C547,
                    new Item.Properties()));
    public static final RegistryObject<Item> TAINTACLE_GIANT_SPAWN_EGG = ITEMS.register("taintacle_giant_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TAINTACLE_GIANT, 0x3A1450, 0x66CC33,
                    new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_GOLEM_SPAWN_EGG = ITEMS.register("eldritch_golem_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ELDRITCH_GOLEM, 0x1F1F2E, 0x7B2FF7,
                    new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_WARDEN_SPAWN_EGG = ITEMS.register("eldritch_warden_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ELDRITCH_WARDEN, 0x0F0F1A, 0x9D4EDD,
                    new Item.Properties()));
    public static final RegistryObject<Item> CREATIVE_FLUX_SPONGE = ITEMS.register("creative_flux_sponge",
            () -> new ItemCreativeFluxSponge(new Item.Properties()));
}
