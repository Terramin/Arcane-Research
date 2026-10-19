package arcaneresearch.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
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
import arcaneresearch.common.items.resources.EldritchEyeItem;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.common.items.resources.ItemMagicDust;
import arcaneresearch.common.items.resources.ItemMaterial;
import arcaneresearch.common.items.resources.ItemTallow;
import arcaneresearch.common.items.tools.*;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Arcaneresearch.MODID);

// NEOFORGE_PORT: entries ported 1:1 from Forge 1.20.1 ModItems.java (same order/names/suppliers).
// Transport: DeferredRegister.create(ForgeRegistries.ITEMS) -> DeferredRegister.createItems(MODID);
// RegistryObject<Item> -> DeferredItem<Item> (same .get()).
// Spawn eggs: ForgeSpawnEggItem(type, baseColor, spotColor, props) removed -> vanilla SpawnEggItem
// with new Item.Properties().spawnEgg(type-holder); per-egg base/spot colors have no 26.x equivalent
// (vanilla eggs are single-sprite now) and were dropped.
public static final DeferredItem<Item> THAUMONOMICON = ITEMS.registerItem("thaumonomicon",
            props -> new ItemThaumonomicon(props));

    public static final DeferredItem<Item> THAUMONOMICON_CHEAT = ITEMS.registerItem("thaumonomicon_cheat",
            props -> new ItemThaumonomicon(props, true));

    public static final DeferredItem<Item> CRIMSON_RITES = ITEMS.registerItem("crimson_rites",
            ItemCrimsonRites::new);

    /** TC4 itemEldritchObject meta 4: свой предмет обелиска (не блок). */
    public static final DeferredItem<Item> ELDRITCH_OBELISK_PLACER = ITEMS.registerItem("eldritch_obelisk_placer",
            arcaneresearch.common.items.misc.ItemEldritchObeliskPlacer::new);

    public static final DeferredItem<Item> THAUMOMETER = ITEMS.registerItem("thaumometer",
            ItemThaumometer::new);

    public static final DeferredItem<Item> SCRIBING_TOOLS = ITEMS.registerItem("scribing_tools",
            ItemScribingTools::new);

    public static final DeferredItem<Item> PHIAL_EMPTY = ITEMS.registerItem("phial_empty",
            ItemPhial::createEmpty);

    public static final DeferredItem<Item> PHIAL_FILLED = ITEMS.registerItem("phial_filled",
            ItemPhial::createFilled);

    public static final DeferredItem<Item> QUICKSILVER = ITEMS.registerItem("quicksilver",
            ItemMaterial::basic);

    public static final DeferredItem<Item> ALUMENTUM = ITEMS.registerItem("alumentum",
            ItemAlumentum::new);

    public static final DeferredItem<Item> SALIS_MUNDUS = ITEMS.registerItem("salis_mundus",
            ItemMagicDust::new);

    public static final DeferredItem<Item> THAUMIUM_INGOT = ITEMS.registerItem("thaumium_ingot",
            ItemMaterial::basic);

    public static final DeferredItem<Item> THAUMIUM_NUGGET = ITEMS.registerItem("thaumium_nugget",
            ItemMaterial::basic);

    public static final DeferredItem<Item> VOID_METAL_INGOT = ITEMS.registerItem("void_metal_ingot",
            props -> new ItemMaterial(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> VOID_METAL_NUGGET = ITEMS.registerItem("void_metal_nugget",
            props -> new ItemMaterial(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> BRASS_INGOT = ITEMS.registerItem("brass_ingot",
            ItemMaterial::basic);

    public static final DeferredItem<Item> BRASS_NUGGET = ITEMS.registerItem("brass_nugget",
            ItemMaterial::basic);

    public static final DeferredItem<Item> COPPER_NUGGET = ITEMS.registerItem("nugget_copper",
            ItemMaterial::basic);

    public static final DeferredItem<Item> TIN_NUGGET = ITEMS.registerItem("nugget_tin",
            ItemMaterial::basic);

    public static final DeferredItem<Item> SILVER_NUGGET = ITEMS.registerItem("nugget_silver",
            ItemMaterial::basic);

    public static final DeferredItem<Item> LEAD_NUGGET = ITEMS.registerItem("nugget_lead",
            ItemMaterial::basic);

    public static final DeferredItem<Item> QUICKSILVER_NUGGET = ITEMS.registerItem("quicksilver_nugget",
            ItemMaterial::basic);

    public static final DeferredItem<Item> QUARTZ_NUGGET = ITEMS.registerItem("quartz_nugget",
            ItemMaterial::basic);

    public static final DeferredItem<Item> RAREEARTH_NUGGET = ITEMS.registerItem("nugget_rareearth",
            ItemMaterial::basic);

    public static final DeferredItem<Item> AMBER = ITEMS.registerItem("amber",
            ItemMaterial::basic);

    public static final DeferredItem<Item> ENCHANTED_FABRIC = ITEMS.registerItem("enchanted_fabric",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> BLANK_SEAL = ITEMS.registerItem("blank_seal",
            ItemMaterial::basic);

    public static final DeferredItem<Item> MIRRORED_GLASS = ITEMS.registerItem("mirrored_glass",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> FILTER = ITEMS.registerItem("filter",
            ItemMaterial::basic);

    public static final DeferredItem<Item> MORPHIC_RESONATOR = ITEMS.registerItem("morphic_resonator",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> VIS_RESONATOR = ITEMS.registerItem("vis_resonator",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> TALLOW = ITEMS.registerItem("tallow",
            ItemTallow::new);

    public static final DeferredItem<Item> JAR_BRACE = ITEMS.registerItem("jar_brace",
            ItemMaterial::basic);

    public static final DeferredItem<Item> MECHANISM_SIMPLE = ITEMS.registerItem("mechanism_simple",
            ItemMaterial::basic);

    public static final DeferredItem<Item> MECHANISM_COMPLEX = ITEMS.registerItem("mechanism_complex",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> PLATE_IRON = ITEMS.registerItem("plate_iron",
            ItemMaterial::basic);

    public static final DeferredItem<Item> PLATE_BRASS = ITEMS.registerItem("plate_brass",
            ItemMaterial::basic);

    public static final DeferredItem<Item> PLATE_THAUMIUM = ITEMS.registerItem("plate_thaumium",
            ItemMaterial::basic);

    public static final DeferredItem<Item> PLATE_VOID = ITEMS.registerItem("plate_void",
            ItemMaterial::rare);

    public static final DeferredItem<Item> CLUSTER_IRON = ITEMS.registerItem("cluster_iron",
            ItemMaterial::basic);

    public static final DeferredItem<Item> CLUSTER_GOLD = ITEMS.registerItem("cluster_gold",
            ItemMaterial::basic);

    public static final DeferredItem<Item> CLUSTER_COPPER = ITEMS.registerItem("cluster_copper",
            ItemMaterial::basic);

    public static final DeferredItem<Item> CLUSTER_TIN = ITEMS.registerItem("cluster_tin",
            ItemMaterial::basic);

    public static final DeferredItem<Item> CLUSTER_SILVER = ITEMS.registerItem("cluster_silver",
            ItemMaterial::basic);

    public static final DeferredItem<Item> CLUSTER_LEAD = ITEMS.registerItem("cluster_lead",
            ItemMaterial::basic);

    public static final DeferredItem<Item> CLUSTER_CINNABAR = ITEMS.registerItem("cluster_cinnabar",
            ItemMaterial::basic);

    public static final DeferredItem<Item> CLUSTER_QUARTZ = ITEMS.registerItem("cluster_quartz",
            ItemMaterial::basic);

    public static final DeferredItem<Item> BRAIN_CLOCKWORK = ITEMS.registerItem("brain_clockwork",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> BRAIN_CURIOUS = ITEMS.registerItem("brain_curious",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> CURIOSITY = ITEMS.registerItem("curiosity",
            ItemMaterial::basic);

    public static final DeferredItem<Item> CURIO_ARCANE = ITEMS.registerItem("curio_arcane",
            props -> new arcaneresearch.common.items.curios.ItemCurio(props, "arcane"));
    public static final DeferredItem<Item> CURIO_PRESERVED = ITEMS.registerItem("curio_preserved",
            props -> new arcaneresearch.common.items.curios.ItemCurio(props, "preserved"));
    public static final DeferredItem<Item> CURIO_ANCIENT = ITEMS.registerItem("curio_ancient",
            props -> new arcaneresearch.common.items.curios.ItemCurio(props, "ancient"));
    public static final DeferredItem<Item> CURIO_ELDRITCH = ITEMS.registerItem("curio_eldritch",
            props -> new arcaneresearch.common.items.curios.ItemCurio(props, "eldritch"));
    public static final DeferredItem<Item> CURIO_KNOWLEDGE = ITEMS.registerItem("curio_knowledge",
            props -> new arcaneresearch.common.items.curios.ItemCurio(props, "knowledge"));
    public static final DeferredItem<Item> CURIO_TWISTED = ITEMS.registerItem("curio_twisted",
            props -> new arcaneresearch.common.items.curios.ItemCurio(props, "twisted"));
    public static final DeferredItem<Item> CURIO_RITES = ITEMS.registerItem("curio_rites",
            props -> new arcaneresearch.common.items.curios.ItemCurio(props, "rites"));

    public static final DeferredItem<Item> PRIMORDIAL_PEARL = ITEMS.registerItem("primordial_pearl",
            ItemPrimordialPearl::new);

    public static final DeferredItem<Item> ELDRITCH_EYE = ITEMS.registerItem("eldritch_eye",
            arcaneresearch.common.items.resources.EldritchEyeItem::new);

    public static final DeferredItem<Item> TAINT_SLIME = ITEMS.registerItem("taint_slime",
            ItemMaterial::basic);

    public static final DeferredItem<Item> TAINT_TENDRIL = ITEMS.registerItem("taint_tendril",
            ItemMaterial::basic);

    public static final DeferredItem<Item> ZOMBIE_BRAIN = ITEMS.registerItem("zombie_brain",
            arcaneresearch.common.items.consumables.ItemZombieBrain::new);

    public static final DeferredItem<Item> VOID_SEED = ITEMS.registerItem("void_seed",
            props -> new ItemMaterial(props.rarity(Rarity.RARE)));

    public static final DeferredItem<Item> TRIPLE_MEAT_TREAT = ITEMS.registerItem("triple_meat_treat",
            arcaneresearch.common.items.consumables.ItemTripleMeatTreat::new);

    public static final DeferredItem<Item> CHUNKS_BEEF = ITEMS.registerItem("chunks_beef",
            ItemChunksBeef::new);

    public static final DeferredItem<Item> CHUNKS_CHICKEN = ITEMS.registerItem("chunks_chicken",
            ItemChunksChicken::new);

    public static final DeferredItem<Item> CHUNKS_PORK = ITEMS.registerItem("chunks_pork",
            ItemChunksPork::new);

    public static final DeferredItem<Item> CHUNKS_FISH = ITEMS.registerItem("chunks_fish",
            ItemChunksFish::new);

    public static final DeferredItem<Item> CHUNKS_RABBIT = ITEMS.registerItem("chunks_rabbit",
            ItemChunksRabbit::new);

    public static final DeferredItem<Item> CHUNKS_MUTTON = ITEMS.registerItem("chunks_mutton",
            ItemChunksMutton::new);

    public static final DeferredItem<Item> GOGGLES = ITEMS.registerItem("goggles",
            ItemGoggles::new);

    public static final DeferredItem<Item> CLOTH_CHEST = ITEMS.registerItem("cloth_chest",
            ItemRobeArmor::createChest);

    public static final DeferredItem<Item> CLOTH_LEGS = ITEMS.registerItem("cloth_legs",
            ItemRobeArmor::createLegs);

    public static final DeferredItem<Item> CLOTH_BOOTS = ITEMS.registerItem("cloth_boots",
            ItemRobeArmor::createBoots);

    public static final DeferredItem<Item> THAUMIUM_SWORD = ITEMS.registerItem("thaumium_sword",
            ItemThaumiumSword::new);

    public static final DeferredItem<Item> THAUMIUM_PICK = ITEMS.registerItem("thaumium_pick",
            ItemThaumiumPickaxe::new);

    public static final DeferredItem<Item> THAUMIUM_AXE = ITEMS.registerItem("thaumium_axe",
            ItemThaumiumAxe::new);

    public static final DeferredItem<Item> THAUMIUM_SHOVEL = ITEMS.registerItem("thaumium_shovel",
            ItemThaumiumShovel::new);

    public static final DeferredItem<Item> THAUMIUM_HOE = ITEMS.registerItem("thaumium_hoe",
            ItemThaumiumHoe::new);

    public static final DeferredItem<Item> VOID_SWORD = ITEMS.registerItem("void_sword",
            ItemVoidSword::new);

    public static final DeferredItem<Item> VOID_PICK = ITEMS.registerItem("void_pick",
            ItemVoidPickaxe::new);

    public static final DeferredItem<Item> VOID_AXE = ITEMS.registerItem("void_axe",
            ItemVoidAxe::new);

    public static final DeferredItem<Item> VOID_SHOVEL = ITEMS.registerItem("void_shovel",
            ItemVoidShovel::new);

    public static final DeferredItem<Item> VOID_HOE = ITEMS.registerItem("void_hoe",
            ItemVoidHoe::new);

    public static final DeferredItem<Item> THAUMIUM_HELM = ITEMS.registerItem("thaumium_helm",
            ItemThaumiumArmor::createHelmet);

    public static final DeferredItem<Item> THAUMIUM_CHEST = ITEMS.registerItem("thaumium_chest",
            ItemThaumiumArmor::createChestplate);

    public static final DeferredItem<Item> THAUMIUM_LEGS = ITEMS.registerItem("thaumium_legs",
            ItemThaumiumArmor::createLeggings);

    public static final DeferredItem<Item> THAUMIUM_BOOTS = ITEMS.registerItem("thaumium_boots",
            ItemThaumiumArmor::createBoots);

    public static final DeferredItem<Item> VOID_HELM = ITEMS.registerItem("void_helm",
            ItemVoidArmor::createHelmet);

    public static final DeferredItem<Item> VOID_CHEST = ITEMS.registerItem("void_chest",
            ItemVoidArmor::createChestplate);

    public static final DeferredItem<Item> VOID_LEGS = ITEMS.registerItem("void_legs",
            ItemVoidArmor::createLeggings);

    public static final DeferredItem<Item> VOID_BOOTS = ITEMS.registerItem("void_boots",
            ItemVoidArmor::createBoots);

    public static final DeferredItem<Item> CASTER_BASIC = ITEMS.registerItem("caster_basic",
            ItemCaster::createBasic);

    public static final DeferredItem<Item> FOCUS_1 = ITEMS.registerItem("focus_1",
            ItemFocus::createBasic);

    public static final DeferredItem<Item> FOCUS_BLANK = ITEMS.registerItem("focus_blank",
            ItemFocus::createBlank);

    public static final DeferredItem<Item> FOCUS_ADVANCED = ITEMS.registerItem("focus_advanced",
            ItemFocus::createAdvanced);

    public static final DeferredItem<Item> TRAVELLER_BOOTS = ITEMS.registerItem("traveller_boots",
            ItemBootsTraveller::new);

    public static final DeferredItem<Item> FORTRESS_HELM = ITEMS.registerItem("fortress_helm",
            ItemFortressArmor::createHelmet);

    public static final DeferredItem<Item> FORTRESS_CHEST = ITEMS.registerItem("fortress_chest",
            ItemFortressArmor::createChestplate);

    public static final DeferredItem<Item> FORTRESS_LEGS = ITEMS.registerItem("fortress_legs",
            ItemFortressArmor::createLeggings);

    public static final DeferredItem<Item> VOID_ROBE_HELM = ITEMS.registerItem("void_robe_helm",
            ItemVoidRobeArmor::createHelmet);

    public static final DeferredItem<Item> VOID_ROBE_CHEST = ITEMS.registerItem("void_robe_chest",
            ItemVoidRobeArmor::createChestplate);

    public static final DeferredItem<Item> VOID_ROBE_LEGS = ITEMS.registerItem("void_robe_legs",
            ItemVoidRobeArmor::createLeggings);

    public static final DeferredItem<Item> CRIMSON_ROBE_HELM = ITEMS.registerItem("crimson_robe_helm",
            ItemCultistRobeArmor::createHelmet);

    public static final DeferredItem<Item> CRIMSON_ROBE_CHEST = ITEMS.registerItem("crimson_robe_chest",
            ItemCultistRobeArmor::createChestplate);

    public static final DeferredItem<Item> CRIMSON_ROBE_LEGS = ITEMS.registerItem("crimson_robe_legs",
            ItemCultistRobeArmor::createLeggings);

    public static final DeferredItem<Item> CRIMSON_PLATE_HELM = ITEMS.registerItem("crimson_plate_helm",
            ItemCultistPlateArmor::createHelmet);

    public static final DeferredItem<Item> CRIMSON_PLATE_CHEST = ITEMS.registerItem("crimson_plate_chest",
            ItemCultistPlateArmor::createChestplate);

    public static final DeferredItem<Item> CRIMSON_PLATE_LEGS = ITEMS.registerItem("crimson_plate_legs",
            ItemCultistPlateArmor::createLeggings);

    public static final DeferredItem<Item> CRIMSON_BOOTS = ITEMS.registerItem("crimson_boots",
            ItemCultistBoots::new);

    public static final DeferredItem<Item> CRIMSON_PRAETOR_HELM = ITEMS.registerItem("crimson_praetor_helm",
            ItemCultistLeaderArmor::createHelmet);

    public static final DeferredItem<Item> CRIMSON_PRAETOR_CHEST = ITEMS.registerItem("crimson_praetor_chest",
            ItemCultistLeaderArmor::createChestplate);

    public static final DeferredItem<Item> CRIMSON_PRAETOR_LEGS = ITEMS.registerItem("crimson_praetor_legs",
            ItemCultistLeaderArmor::createLeggings);

    public static final DeferredItem<Item> AMULET_VIS_FOUND = ITEMS.registerItem("amulet_vis_found",
            ItemAmuletVis::createFound);

    public static final DeferredItem<Item> AMULET_VIS_CRAFTED = ITEMS.registerItem("amulet_vis_crafted",
            ItemAmuletVis::createCrafted);

    public static final DeferredItem<Item> CLOUD_RING = ITEMS.registerItem("cloud_ring",
            ItemCloudRing::new);

    public static final DeferredItem<Item> CURIOSITY_BAND = ITEMS.registerItem("curiosity_band",
            ItemCuriosityBand::new);

    public static final DeferredItem<Item> CHARM_UNDYING = ITEMS.registerItem("charm_undying",
            ItemCharmUndying::new);

    public static final DeferredItem<Item> VERDANT_CHARM = ITEMS.registerItem("verdant_charm",
            ItemVerdantCharm::createBasic);

    public static final DeferredItem<Item> VERDANT_CHARM_LIFE = ITEMS.registerItem("verdant_charm_life",
            ItemVerdantCharm::createLife);

    public static final DeferredItem<Item> VERDANT_CHARM_SUSTAIN = ITEMS.registerItem("verdant_charm_sustain",
            ItemVerdantCharm::createSustain);

    public static final DeferredItem<Item> VOIDSEER_CHARM = ITEMS.registerItem("voidseer_charm",
            ItemVoidseerCharm::new);

    public static final DeferredItem<Item> BAUBLE_AMULET_MUNDANE = ITEMS.registerItem("bauble_amulet_mundane",
            ItemBauble::amuletMundane);

    public static final DeferredItem<Item> BAUBLE_RING_MUNDANE = ITEMS.registerItem("bauble_ring_mundane",
            ItemBauble::ringMundane);

    public static final DeferredItem<Item> BAUBLE_GIRDLE_MUNDANE = ITEMS.registerItem("bauble_girdle_mundane",
            ItemBauble::girdleMundane);

    public static final DeferredItem<Item> BAUBLE_RING_APPRENTICE = ITEMS.registerItem("bauble_ring_apprentice",
            ItemBauble::ringApprentice);

    public static final DeferredItem<Item> BAUBLE_AMULET_FANCY = ITEMS.registerItem("bauble_amulet_fancy",
            ItemBauble::amuletFancy);

    public static final DeferredItem<Item> BAUBLE_RING_FANCY = ITEMS.registerItem("bauble_ring_fancy",
            ItemBauble::ringFancy);

    public static final DeferredItem<Item> BAUBLE_GIRDLE_FANCY = ITEMS.registerItem("bauble_girdle_fancy",
            ItemBauble::girdleFancy);

    // NEOFORGE_PORT: порт Forge ModItems.RUNIC_* (TC4 parity, factories уже берут props).
    public static final DeferredItem<Item> RUNIC_AMULET = ITEMS.registerItem("runic_amulet",
            arcaneresearch.common.items.baubles.ItemRunicBauble::amulet);

    public static final DeferredItem<Item> RUNIC_AMULET_EMERGENCY = ITEMS.registerItem("runic_amulet_emergency",
            arcaneresearch.common.items.baubles.ItemRunicBauble::amuletEmergency);

    public static final DeferredItem<Item> RUNIC_RING_LESSER = ITEMS.registerItem("runic_ring_lesser",
            arcaneresearch.common.items.baubles.ItemRunicBauble::ringLesser);

    public static final DeferredItem<Item> RUNIC_RING = ITEMS.registerItem("runic_ring",
            arcaneresearch.common.items.baubles.ItemRunicBauble::ring);

    public static final DeferredItem<Item> RUNIC_RING_CHARGED = ITEMS.registerItem("runic_ring_charged",
            arcaneresearch.common.items.baubles.ItemRunicBauble::ringCharged);

    public static final DeferredItem<Item> RUNIC_RING_REGEN = ITEMS.registerItem("runic_ring_regen",
            arcaneresearch.common.items.baubles.ItemRunicBauble::ringRegen);

    public static final DeferredItem<Item> RUNIC_GIRDLE = ITEMS.registerItem("runic_girdle",
            arcaneresearch.common.items.baubles.ItemRunicBauble::girdle);

    public static final DeferredItem<Item> RUNIC_GIRDLE_KINETIC = ITEMS.registerItem("runic_girdle_kinetic",
            arcaneresearch.common.items.baubles.ItemRunicBauble::girdleKinetic);

    public static final DeferredItem<Item> ENCHANTED_PLACEHOLDER = ITEMS.registerItem("enchanted_placeholder",
            arcaneresearch.common.items.curios.ItemEnchantmentPlaceholder::new);

    public static final DeferredItem<Item> ELEMENTAL_PICK = ITEMS.registerItem("elemental_pick",
            ItemElementalPickaxe::new);

    public static final DeferredItem<Item> ELEMENTAL_AXE = ITEMS.registerItem("elemental_axe",
            ItemElementalAxe::new);

    public static final DeferredItem<Item> ELEMENTAL_SWORD = ITEMS.registerItem("elemental_sword",
            ItemElementalSword::new);

    public static final DeferredItem<Item> ELEMENTAL_SHOVEL = ITEMS.registerItem("elemental_shovel",
            ItemElementalShovel::new);

    public static final DeferredItem<Item> PRIMAL_CRUSHER = ITEMS.registerItem("primal_crusher",
            ItemPrimalCrusher::new);

    public static final DeferredItem<Item> CRIMSON_BLADE = ITEMS.registerItem("crimson_blade",
            ItemCrimsonBlade::new);

    public static final DeferredItem<Item> ELEMENTAL_HOE = ITEMS.registerItem("elemental_hoe",
            ItemElementalHoe::new);

    public static final DeferredItem<Item> RESONATOR = ITEMS.registerItem("resonator",
            ItemResonator::new);

    public static final DeferredItem<Item> SANITY_CHECKER = ITEMS.registerItem("sanity_checker",
            ItemSanityChecker::new);

    public static final DeferredItem<Item> HAND_MIRROR = ITEMS.registerItem("hand_mirror",
            ItemHandMirror::new);

    public static final DeferredItem<Item> GRAPPLE_GUN = ITEMS.registerItem("grapple_gun",
            ItemGrappleGun::new);

    // NEOFORGE_PORT: порт Forge ModItems.BONE_BOW + 6 PRIMAL_ARROW_* (TC4 parity).
    public static final DeferredItem<Item> BONE_BOW = ITEMS.registerItem("bone_bow",
            arcaneresearch.common.items.tools.ItemBoneBow::new);

    public static final DeferredItem<Item> PRIMAL_ARROW_AIR = ITEMS.registerItem("primal_arrow_air",
            props -> new arcaneresearch.common.items.tools.ItemPrimalArrow(props,
                    arcaneresearch.common.items.tools.ItemPrimalArrow.AIR));

    public static final DeferredItem<Item> PRIMAL_ARROW_FIRE = ITEMS.registerItem("primal_arrow_fire",
            props -> new arcaneresearch.common.items.tools.ItemPrimalArrow(props,
                    arcaneresearch.common.items.tools.ItemPrimalArrow.FIRE));

    public static final DeferredItem<Item> PRIMAL_ARROW_WATER = ITEMS.registerItem("primal_arrow_water",
            props -> new arcaneresearch.common.items.tools.ItemPrimalArrow(props,
                    arcaneresearch.common.items.tools.ItemPrimalArrow.WATER));

    public static final DeferredItem<Item> PRIMAL_ARROW_EARTH = ITEMS.registerItem("primal_arrow_earth",
            props -> new arcaneresearch.common.items.tools.ItemPrimalArrow(props,
                    arcaneresearch.common.items.tools.ItemPrimalArrow.EARTH));

    public static final DeferredItem<Item> PRIMAL_ARROW_ORDER = ITEMS.registerItem("primal_arrow_order",
            props -> new arcaneresearch.common.items.tools.ItemPrimalArrow(props,
                    arcaneresearch.common.items.tools.ItemPrimalArrow.ORDER));

    public static final DeferredItem<Item> PRIMAL_ARROW_ENTROPY = ITEMS.registerItem("primal_arrow_entropy",
            props -> new arcaneresearch.common.items.tools.ItemPrimalArrow(props,
                    arcaneresearch.common.items.tools.ItemPrimalArrow.ENTROPY));

    public static final DeferredItem<Item> BATH_SALTS = ITEMS.registerItem("bath_salts",
            ItemBathSalts::new);

    public static final DeferredItem<Item> SANITY_SOAP = ITEMS.registerItem("sanity_soap",
            ItemSanitySoap::new);

    public static final DeferredItem<Item> BOTTLE_TAINT = ITEMS.registerItem("bottle_taint",
            ItemBottleTaint::new);

    public static final DeferredItem<Item> CAUSALITY_COLLAPSER = ITEMS.registerItem("causality_collapser",
            ItemCausalityCollapser::new);

    public static final DeferredItem<Item> LABEL_BLANK = ITEMS.registerItem("label_blank",
            ItemLabel::createBlank);

    public static final DeferredItem<Item> LABEL_FILLED = ITEMS.registerItem("label_filled",
            ItemLabel::createFilled);

    public static final DeferredItem<Item> LOOT_BAG_COMMON = ITEMS.registerItem("loot_bag_common",
            ItemLootBag::createCommon);

    public static final DeferredItem<Item> LOOT_BAG_UNCOMMON = ITEMS.registerItem("loot_bag_uncommon",
            ItemLootBag::createUncommon);

    public static final DeferredItem<Item> LOOT_BAG_RARE = ITEMS.registerItem("loot_bag_rare",
            ItemLootBag::createRare);

    public static final DeferredItem<Item> PECH_WAND = ITEMS.registerItem("pech_wand",
            ItemPechWand::new);

    public static final DeferredItem<Item> CELESTIAL_NOTES_SUN = ITEMS.registerItem("celestial_notes_sun",
            ItemCelestialNotes::createSun);

    public static final DeferredItem<Item> CELESTIAL_NOTES_STARS_1 = ITEMS.registerItem("celestial_notes_stars_1",
            ItemCelestialNotes::createStars1);

    public static final DeferredItem<Item> CELESTIAL_NOTES_STARS_2 = ITEMS.registerItem("celestial_notes_stars_2",
            ItemCelestialNotes::createStars2);

    public static final DeferredItem<Item> CELESTIAL_NOTES_STARS_3 = ITEMS.registerItem("celestial_notes_stars_3",
            ItemCelestialNotes::createStars3);

    public static final DeferredItem<Item> CELESTIAL_NOTES_STARS_4 = ITEMS.registerItem("celestial_notes_stars_4",
            ItemCelestialNotes::createStars4);

    public static final DeferredItem<Item> CELESTIAL_NOTES_MOON_1 = ITEMS.registerItem("celestial_notes_moon_1",
            ItemCelestialNotes::createMoon1);

    public static final DeferredItem<Item> CELESTIAL_NOTES_MOON_2 = ITEMS.registerItem("celestial_notes_moon_2",
            ItemCelestialNotes::createMoon2);

    public static final DeferredItem<Item> CELESTIAL_NOTES_MOON_3 = ITEMS.registerItem("celestial_notes_moon_3",
            ItemCelestialNotes::createMoon3);

    public static final DeferredItem<Item> CELESTIAL_NOTES_MOON_4 = ITEMS.registerItem("celestial_notes_moon_4",
            ItemCelestialNotes::createMoon4);

    public static final DeferredItem<Item> CELESTIAL_NOTES_MOON_5 = ITEMS.registerItem("celestial_notes_moon_5",
            ItemCelestialNotes::createMoon5);

    public static final DeferredItem<Item> CELESTIAL_NOTES_MOON_6 = ITEMS.registerItem("celestial_notes_moon_6",
            ItemCelestialNotes::createMoon6);

    public static final DeferredItem<Item> CELESTIAL_NOTES_MOON_7 = ITEMS.registerItem("celestial_notes_moon_7",
            ItemCelestialNotes::createMoon7);

    public static final DeferredItem<Item> CELESTIAL_NOTES_MOON_8 = ITEMS.registerItem("celestial_notes_moon_8",
            ItemCelestialNotes::createMoon8);

    public static final DeferredItem<Item> FOCUS_POUCH = ITEMS.registerItem("focus_pouch",
            ItemFocusPouch::new);

    public static final DeferredItem<Item> GOLEM_PLACER = ITEMS.registerItem("golem_placer",
            ItemGolemPlacer::new);

    public static final DeferredItem<Item> GOLEM_BELL = ITEMS.registerItem("golem_bell",
            ItemGolemBell::new);

    public static final DeferredItem<Item> SEAL_BLANK = ITEMS.registerItem("seal_blank",
            ItemSealPlacer::createBlank);

    public static final DeferredItem<Item> SEAL_PICKUP = ITEMS.registerItem("seal_pickup",
            props -> ItemSealPlacer.create(props, "arcaneresearch:pickup"));

    public static final DeferredItem<Item> SEAL_EMPTY = ITEMS.registerItem("seal_empty",
            props -> ItemSealPlacer.create(props, "arcaneresearch:empty"));

    public static final DeferredItem<Item> SEAL_FILL = ITEMS.registerItem("seal_fill",
            props -> ItemSealPlacer.create(props, "arcaneresearch:fill"));

    public static final DeferredItem<Item> SEAL_GUARD = ITEMS.registerItem("seal_guard",
            props -> ItemSealPlacer.create(props, "arcaneresearch:guard"));

    public static final DeferredItem<Item> SEAL_BUTCHER = ITEMS.registerItem("seal_butcher",
            props -> ItemSealPlacer.create(props, "arcaneresearch:butcher"));

    public static final DeferredItem<Item> SEAL_HARVEST = ITEMS.registerItem("seal_harvest",
            props -> ItemSealPlacer.create(props, "arcaneresearch:harvest"));

    public static final DeferredItem<Item> SEAL_LUMBER = ITEMS.registerItem("seal_lumber",
            props -> ItemSealPlacer.create(props, "arcaneresearch:lumber"));

    public static final DeferredItem<Item> SEAL_BREAKER = ITEMS.registerItem("seal_breaker",
            props -> ItemSealPlacer.create(props, "arcaneresearch:breaker"));

    public static final DeferredItem<Item> SEAL_PROVIDE = ITEMS.registerItem("seal_provide",
            props -> ItemSealPlacer.create(props, "arcaneresearch:provider"));

    public static final DeferredItem<Item> SEAL_STOCK = ITEMS.registerItem("seal_stock",
            props -> ItemSealPlacer.create(props, "arcaneresearch:stock"));

    public static final DeferredItem<Item> SEAL_USE = ITEMS.registerItem("seal_use",
            props -> ItemSealPlacer.create(props, "arcaneresearch:use"));

    public static final DeferredItem<Item> SEAL_BREAKER_ADVANCED = ITEMS.registerItem("seal_breaker_advanced",
            props -> ItemSealPlacer.create(props, "arcaneresearch:breaker_advanced"));

    public static final DeferredItem<Item> SEAL_PICKUP_ADVANCED = ITEMS.registerItem("seal_pickup_advanced",
            props -> ItemSealPlacer.create(props, "arcaneresearch:pickup_advanced"));

    public static final DeferredItem<Item> SEAL_EMPTY_ADVANCED = ITEMS.registerItem("seal_empty_advanced",
            props -> ItemSealPlacer.create(props, "arcaneresearch:empty_advanced"));

    public static final DeferredItem<Item> SEAL_FILL_ADVANCED = ITEMS.registerItem("seal_fill_advanced",
            props -> ItemSealPlacer.create(props, "arcaneresearch:fill_advanced"));

    public static final DeferredItem<Item> SEAL_GUARD_ADVANCED = ITEMS.registerItem("seal_guard_advanced",
            props -> ItemSealPlacer.create(props, "arcaneresearch:guard_advanced"));

    public static final DeferredItem<Item> CRYSTAL_ESSENCE = ITEMS.registerItem("crystal_essence",
            props -> new ItemCrystalEssence(props));

    public static final DeferredItem<Item> WISP_ESSENCE = ITEMS.registerItem("wisp_essence",
            arcaneresearch.common.items.resources.ItemWispEssence::new);

    public static final DeferredItem<Item> TURRET_PLACER_BASIC = ITEMS.registerItem("turret_placer_basic",
            ItemTurretPlacer::createBasic);

    public static final DeferredItem<Item> TURRET_PLACER_ADVANCED = ITEMS.registerItem("turret_placer_advanced",
            ItemTurretPlacer::createAdvanced);

    public static final DeferredItem<Item> TURRET_PLACER_BORE = ITEMS.registerItem("turret_placer_bore",
            ItemTurretPlacer::createBore);

    public static final DeferredItem<Item> GRAPPLE_GUN_SPOOL = ITEMS.registerItem("grapple_gun_spool",
            ItemMaterial::basic);

    public static final DeferredItem<Item> GRAPPLE_GUN_TIP = ITEMS.registerItem("grapple_gun_tip",
            ItemMaterial::basic);

    public static final DeferredItem<Item> GOLEM_MODULE_AGGRESSION = ITEMS.registerItem("golem_module_aggression",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> GOLEM_MODULE_VISION = ITEMS.registerItem("golem_module_vision",
            ItemMaterial::uncommon);

    public static final DeferredItem<Item> INHABITED_ZOMBIE_SPAWN_EGG = ITEMS.registerItem("inhabited_zombie_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.INHABITED_ZOMBIE.get())));

    public static final DeferredItem<Item> ELDRITCH_GUARDIAN_SPAWN_EGG = ITEMS.registerItem("eldritch_guardian_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.ELDRITCH_GUARDIAN.get())));

    public static final DeferredItem<Item> WISP_SPAWN_EGG = ITEMS.registerItem("wisp_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.WISP.get())));

    public static final DeferredItem<Item> FIRE_BAT_SPAWN_EGG = ITEMS.registerItem("fire_bat_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.FIRE_BAT.get())));

    public static final DeferredItem<Item> BRAINY_ZOMBIE_SPAWN_EGG = ITEMS.registerItem("brainy_zombie_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.BRAINY_ZOMBIE.get())));

    public static final DeferredItem<Item> MIND_SPIDER_SPAWN_EGG = ITEMS.registerItem("mind_spider_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.MIND_SPIDER.get())));

    public static final DeferredItem<Item> THAUMIC_SLIME_SPAWN_EGG = ITEMS.registerItem("thaumic_slime_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.THAUMIC_SLIME.get())));

    public static final DeferredItem<Item> GIANT_BRAINY_ZOMBIE_SPAWN_EGG = ITEMS.registerItem("giant_brainy_zombie_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.GIANT_BRAINY_ZOMBIE.get())));

    public static final DeferredItem<Item> ELDRITCH_CRAB_SPAWN_EGG = ITEMS.registerItem("eldritch_crab_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.ELDRITCH_CRAB.get())));

    public static final DeferredItem<Item> SPELL_BAT_SPAWN_EGG = ITEMS.registerItem("spell_bat_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.SPELL_BAT.get())));

    public static final DeferredItem<Item> CULTIST_SPAWN_EGG = ITEMS.registerItem("cultist_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.CULTIST.get())));

    public static final DeferredItem<Item> CULTIST_KNIGHT_SPAWN_EGG = ITEMS.registerItem("cultist_knight_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.CULTIST_KNIGHT.get())));

    public static final DeferredItem<Item> CULTIST_CLERIC_SPAWN_EGG = ITEMS.registerItem("cultist_cleric_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.CULTIST_CLERIC.get())));

    public static final DeferredItem<Item> PECH_SPAWN_EGG = ITEMS.registerItem("pech_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.PECH.get())));

    public static final DeferredItem<Item> TAINT_CRAWLER_SPAWN_EGG = ITEMS.registerItem("taint_crawler_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.TAINT_CRAWLER.get())));

    public static final DeferredItem<Item> TAINT_SWARM_SPAWN_EGG = ITEMS.registerItem("taint_swarm_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.TAINT_SWARM.get())));

    public static final DeferredItem<Item> CULTIST_LEADER_SPAWN_EGG = ITEMS.registerItem("cultist_leader_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.CULTIST_LEADER.get())));

    public static final DeferredItem<Item> TAINTACLE_GIANT_SPAWN_EGG = ITEMS.registerItem("taintacle_giant_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.TAINTACLE_GIANT.get())));

    public static final DeferredItem<Item> ELDRITCH_GOLEM_SPAWN_EGG = ITEMS.registerItem("eldritch_golem_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.ELDRITCH_GOLEM.get())));

    public static final DeferredItem<Item> ELDRITCH_WARDEN_SPAWN_EGG = ITEMS.registerItem("eldritch_warden_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(ModEntities.ELDRITCH_WARDEN.get())));

    public static final DeferredItem<Item> CREATIVE_FLUX_SPONGE = ITEMS.registerItem("creative_flux_sponge",
            props -> new ItemCreativeFluxSponge(props));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
