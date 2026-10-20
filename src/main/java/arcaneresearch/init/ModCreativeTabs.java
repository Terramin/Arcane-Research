package arcaneresearch.init;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.parts.*;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.blocks.essentia.BlockJarItem;import arcaneresearch.common.golems.GolemProperties;
import arcaneresearch.common.golems.ItemGolemPlacer;
import arcaneresearch.common.items.consumables.ItemLabel;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Arcaneresearch.MODID);
    public static final RegistryObject<CreativeModeTab> ARCANERESEARCH_TAB = CREATIVE_MODE_TABS.register("arcaneresearch_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.arcaneresearch"))
                    .icon(() -> new ItemStack(ModItems.THAUMONOMICON.get()))
                    .displayItems((parameters, output) -> {
                        acceptBlock(output, ModBlocks.AMBER_ORE.get());
                        acceptBlock(output, ModBlocks.CINNABAR_ORE.get());
                        acceptBlock(output, ModBlocks.QUARTZ_ORE.get());
                        acceptBlock(output, ModBlocks.DEEPSLATE_AMBER_ORE.get());
                        acceptBlock(output, ModBlocks.DEEPSLATE_CINNABAR_ORE.get());
                        acceptBlock(output, ModBlocks.DEEPSLATE_QUARTZ_ORE.get());
                        acceptBlock(output, ModBlocks.CRYSTAL_AIR.get());
                        acceptBlock(output, ModBlocks.CRYSTAL_FIRE.get());
                        acceptBlock(output, ModBlocks.CRYSTAL_WATER.get());
                        acceptBlock(output, ModBlocks.CRYSTAL_EARTH.get());
                        acceptBlock(output, ModBlocks.CRYSTAL_ORDER.get());
                        acceptBlock(output, ModBlocks.CRYSTAL_ENTROPY.get());
                        acceptBlock(output, ModBlocks.CRYSTAL_FLUX.get());
                        acceptBlock(output, ModBlocks.ARCANE_STONE.get());
                        acceptBlock(output, ModBlocks.ARCANE_STONE_BRICK.get());
                        acceptBlock(output, ModBlocks.ANCIENT_STONE.get());
                        acceptBlock(output, ModBlocks.ANCIENT_STONE_TILE.get());
                        acceptBlock(output, ModBlocks.ANCIENT_STONE_ROCK.get());
                        acceptBlock(output, ModBlocks.ANCIENT_STONE_GLYPHED.get());
                        acceptBlock(output, ModBlocks.ANCIENT_STONE_DOORWAY.get());
                        acceptBlock(output, ModBlocks.ELDRITCH_STONE_TILE.get());
                        acceptBlock(output, ModBlocks.ELDRITCH_STONE.get());
                        acceptBlock(output, ModBlocks.POROUS_STONE.get());
                        acceptBlock(output, ModBlocks.ARCANE_STONE_STAIRS.get());
                        acceptBlock(output, ModBlocks.ARCANE_STONE_BRICK_STAIRS.get());
                        acceptBlock(output, ModBlocks.ANCIENT_STONE_STAIRS.get());
                        acceptBlock(output, ModBlocks.ARCANE_STONE_SLAB.get());
                        acceptBlock(output, ModBlocks.ARCANE_STONE_BRICK_SLAB.get());
                        acceptBlock(output, ModBlocks.ANCIENT_STONE_SLAB.get());
                        acceptBlock(output, ModBlocks.ELDRITCH_STONE_SLAB.get());
                        acceptBlock(output, AuraBlocks.ELDRITCH_ALTAR.get());
                        acceptBlock(output, AuraBlocks.ELDRITCH_CAP.get());
                        acceptBlock(output, AuraBlocks.ELDRITCH_CRAB.get());
                        output.accept(ModItems.ELDRITCH_OBELISK_PLACER.get());
                        acceptBlock(output, AuraBlocks.ELDRITCH_PORTAL.get());
                        acceptBlock(output, AuraBlocks.ELDRITCH_STAIRS.get());
                        acceptBlock(output, AuraBlocks.ELDRITCH_TRAP.get());
                        acceptBlock(output, ModBlocks.GREATWOOD_SAPLING.get());
                        acceptBlock(output, ModBlocks.SILVERWOOD_SAPLING.get());
                        acceptBlock(output, ModBlocks.GREATWOOD_LOG.get());
                        acceptBlock(output, ModBlocks.SILVERWOOD_LOG.get());
                        acceptBlock(output, ModBlocks.GREATWOOD_LEAVES.get());
                        acceptBlock(output, ModBlocks.SILVERWOOD_LEAVES.get());
                        acceptBlock(output, ModBlocks.SHIMMERLEAF.get());
                        acceptBlock(output, ModBlocks.CINDERPEARL.get());
                        acceptBlock(output, ModBlocks.VISHROOM.get());
                        acceptBlock(output, ModBlocks.GREATWOOD_PLANKS.get());
                        acceptBlock(output, ModBlocks.SILVERWOOD_PLANKS.get());
                        acceptBlock(output, ModBlocks.GREATWOOD_STAIRS.get());
                        acceptBlock(output, ModBlocks.SILVERWOOD_STAIRS.get());
                        acceptBlock(output, ModBlocks.GREATWOOD_SLAB.get());
                        acceptBlock(output, ModBlocks.SILVERWOOD_SLAB.get());
                        acceptBlock(output, ModBlocks.AMBER_BLOCK.get());
                        acceptBlock(output, ModBlocks.AMBER_BRICK.get());
                        acceptBlock(output, ModBlocks.FLESH_BLOCK.get());
                        acceptBlock(output, ModBlocks.LOOT_CRATE_COMMON.get());
                        acceptBlock(output, ModBlocks.LOOT_CRATE_UNCOMMON.get());
                        acceptBlock(output, ModBlocks.LOOT_CRATE_RARE.get());
                        acceptBlock(output, ModBlocks.LOOT_URN_COMMON.get());
                        acceptBlock(output, ModBlocks.LOOT_URN_UNCOMMON.get());
                        acceptBlock(output, ModBlocks.LOOT_URN_RARE.get());
                        acceptBlock(output, ModBlocks.TAINT_FIBRE.get());
                        acceptBlock(output, ModBlocks.TAINT_CRUST.get());
                        acceptBlock(output, ModBlocks.TAINT_SOIL.get());
                        acceptBlock(output, ModBlocks.TAINT_ROCK.get());
                        acceptBlock(output, ModBlocks.TAINT_GEYSER.get());
                        acceptBlock(output, ModBlocks.TAINT_FEATURE.get());
                        acceptBlock(output, ModBlocks.TAINT_LOG.get());
                        acceptBlock(output, ModBlocks.GRASS_AMBIENT.get());
                        acceptBlock(output, ModBlocks.TABLE_WOOD.get());
                        acceptBlock(output, ModBlocks.TABLE_STONE.get());
                        acceptBlock(output, ModBlocks.PEDESTAL_ARCANE.get());
                        acceptBlock(output, ModBlocks.PEDESTAL_ANCIENT.get());
                        acceptBlock(output, ModBlocks.PEDESTAL_ELDRITCH.get());
                        acceptBlock(output, ModBlocks.BRASS_BLOCK.get());
                        acceptBlock(output, ModBlocks.THAUMIUM_BLOCK.get());
                        acceptBlock(output, ModBlocks.VOID_METAL_BLOCK.get());
                        acceptBlock(output, ModBlocks.ALCHEMICAL_BRASS_BLOCK.get());
                        acceptBlock(output, ModBlocks.ALCHEMICAL_BRASS_ADVANCED_BLOCK.get());
                        acceptBlock(output, ModBlocks.PAVING_STONE_TRAVEL.get());
                        acceptBlock(output, ModBlocks.PAVING_STONE_BARRIER.get());
                        acceptBlock(output, ModBlocks.PAVING_STONE.get());
                        acceptBlock(output, ModBlocks.ARCANE_PILLAR.get());
                        acceptBlock(output, ModBlocks.ANCIENT_PILLAR.get());
                        acceptBlock(output, ModBlocks.ELDRITCH_PILLAR.get());
                        acceptBlock(output, ModBlocks.MATRIX_SPEED.get());
                        acceptBlock(output, ModBlocks.MATRIX_COST.get());
                        acceptBlock(output, ModBlocks.CANDLE_WHITE.get());
                        acceptBlock(output, ModBlocks.CANDLE_ORANGE.get());
                        acceptBlock(output, ModBlocks.CANDLE_MAGENTA.get());
                        acceptBlock(output, ModBlocks.CANDLE_LIGHT_BLUE.get());
                        acceptBlock(output, ModBlocks.CANDLE_YELLOW.get());
                        acceptBlock(output, ModBlocks.CANDLE_LIME.get());
                        acceptBlock(output, ModBlocks.CANDLE_PINK.get());
                        acceptBlock(output, ModBlocks.CANDLE_GRAY.get());
                        acceptBlock(output, ModBlocks.CANDLE_LIGHT_GRAY.get());
                        acceptBlock(output, ModBlocks.CANDLE_CYAN.get());
                        acceptBlock(output, ModBlocks.CANDLE_PURPLE.get());
                        acceptBlock(output, ModBlocks.CANDLE_BLUE.get());
                        acceptBlock(output, ModBlocks.CANDLE_BROWN.get());
                        acceptBlock(output, ModBlocks.CANDLE_GREEN.get());
                        acceptBlock(output, ModBlocks.CANDLE_RED.get());
                        acceptBlock(output, ModBlocks.CANDLE_BLACK.get());
                        acceptBlock(output, ModBlocks.BANNER_WHITE.get());
                        acceptBlock(output, ModBlocks.BANNER_ORANGE.get());
                        acceptBlock(output, ModBlocks.BANNER_MAGENTA.get());
                        acceptBlock(output, ModBlocks.BANNER_LIGHT_BLUE.get());
                        acceptBlock(output, ModBlocks.BANNER_YELLOW.get());
                        acceptBlock(output, ModBlocks.BANNER_LIME.get());
                        acceptBlock(output, ModBlocks.BANNER_PINK.get());
                        acceptBlock(output, ModBlocks.BANNER_GRAY.get());
                        acceptBlock(output, ModBlocks.BANNER_LIGHT_GRAY.get());
                        acceptBlock(output, ModBlocks.BANNER_CYAN.get());
                        acceptBlock(output, ModBlocks.BANNER_PURPLE.get());
                        acceptBlock(output, ModBlocks.BANNER_BLUE.get());
                        acceptBlock(output, ModBlocks.BANNER_BROWN.get());
                        acceptBlock(output, ModBlocks.BANNER_GREEN.get());
                        acceptBlock(output, ModBlocks.BANNER_RED.get());
                        acceptBlock(output, ModBlocks.BANNER_BLACK.get());
                        acceptBlock(output, ModBlocks.BANNER_CRIMSON_CULT.get());
                        acceptBlock(output, ModBlocks.NITOR_WHITE.get());
                        acceptBlock(output, ModBlocks.NITOR_ORANGE.get());
                        acceptBlock(output, ModBlocks.NITOR_MAGENTA.get());
                        acceptBlock(output, ModBlocks.NITOR_LIGHT_BLUE.get());
                        acceptBlock(output, ModBlocks.NITOR_YELLOW.get());
                        acceptBlock(output, ModBlocks.NITOR_LIME.get());
                        acceptBlock(output, ModBlocks.NITOR_PINK.get());
                        acceptBlock(output, ModBlocks.NITOR_GRAY.get());
                        acceptBlock(output, ModBlocks.NITOR_LIGHT_GRAY.get());
                        acceptBlock(output, ModBlocks.NITOR_CYAN.get());
                        acceptBlock(output, ModBlocks.NITOR_PURPLE.get());
                        acceptBlock(output, ModBlocks.NITOR_BLUE.get());
                        acceptBlock(output, ModBlocks.NITOR_BROWN.get());
                        acceptBlock(output, ModBlocks.NITOR_GREEN.get());
                        acceptBlock(output, ModBlocks.NITOR_RED.get());
                        acceptBlock(output, ModBlocks.NITOR_BLACK.get());
                        acceptBlock(output, ModBlocks.VIS_BATTERY.get());
                        acceptBlock(output, ModBlocks.INLAY.get());
                        acceptBlock(output, ModBlocks.ARCANE_WORKBENCH.get());
                        acceptBlock(output, ModBlocks.ARCANE_WORKBENCH_CHARGER.get());
                        acceptBlock(output, ModBlocks.DIOPTRA.get());
                        acceptBlock(output, ModBlocks.RESEARCH_TABLE.get());
                        acceptBlock(output, ModBlocks.DECONSTRUCTION_TABLE.get());
                        acceptBlock(output, ModBlocks.CRUCIBLE.get());
                        acceptBlock(output, ModBlocks.ARCANE_EAR.get());
                        acceptBlock(output, ModBlocks.ARCANE_EAR_TOGGLE.get());
                        acceptBlock(output, ModBlocks.LAMP_ARCANE.get());
                        acceptBlock(output, ModBlocks.LAMP_FERTILITY.get());
                        acceptBlock(output, ModBlocks.LAMP_GROWTH.get());
                        acceptBlock(output, ModBlocks.LEVITATOR.get());
                        acceptBlock(output, ModBlocks.CENTRIFUGE.get());
                        acceptBlock(output, ModBlocks.BELLOWS.get());
                        acceptBlock(output, ModBlocks.SMELTER.get());
                        acceptBlock(output, ModBlocks.SMELTER_THAUMIUM.get());
                        acceptBlock(output, ModBlocks.SMELTER_VOID.get());
                        acceptBlock(output, ModBlocks.SMELTER_AUX.get());
                        acceptBlock(output, ModBlocks.SMELTER_VENT.get());
                        acceptBlock(output, ModBlocks.ALEMBIC.get());
                        acceptBlock(output, ModBlocks.RECHARGE_PEDESTAL.get());
                        acceptBlock(output, ModBlocks.FOCAL_MANIPULATOR.get());
                        acceptBlock(output, ModBlocks.HUNGRY_CHEST.get());
                        acceptBlock(output, ModBlocks.TUBE_NORMAL.get());
                        acceptBlock(output, ModBlocks.TUBE_VALVE.get());
                        acceptBlock(output, ModBlocks.TUBE_RESTRICTED.get());
                        acceptBlock(output, ModBlocks.TUBE_ONEWAY.get());
                        acceptBlock(output, ModBlocks.TUBE_FILTER.get());
                        acceptBlock(output, ModBlocks.TUBE_BUFFER.get());
                        acceptBlock(output, ModBlocks.JAR_NORMAL.get());
                        acceptBlock(output, ModBlocks.JAR_VOID.get());
                        acceptBlock(output, ModBlocks.JAR_BRAIN.get());
                        for (Aspect aspect : Aspect.aspects.values()) {
                            ItemStack jar = new ItemStack(ModBlocks.JAR_NORMAL.get());
                            BlockJarItem.setAspectsStatic(jar, new AspectList().add(aspect, 250));
                            output.accept(jar);
                        }
                        java.util.List<ItemStack> auraJars = new java.util.ArrayList<>();
                        java.util.List<ItemStack> auraNodes = new java.util.ArrayList<>();
                        Aspect[] auraPrimals = {Aspect.AIR, Aspect.EARTH, Aspect.FIRE,
                                Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY};
                        for (Aspect primal : auraPrimals) {
                            AspectList single = new AspectList().add(primal, 30);
                            auraJars.add(arcaneresearch.common.items.aura.ItemJarNode.createStack(
                                    arcaneresearch.common.lib.aura.NodeType.NORMAL, null, single));
                            auraNodes.add(arcaneresearch.common.items.aura.ItemAuraNode.createStack(
                                    arcaneresearch.common.lib.aura.NodeType.NORMAL, null, single));
                        }
                        AspectList auraNodeFull = new AspectList()
                                .add(Aspect.AIR, 30).add(Aspect.EARTH, 30).add(Aspect.FIRE, 30)
                                .add(Aspect.WATER, 30).add(Aspect.ORDER, 30).add(Aspect.ENTROPY, 30);
                        for (arcaneresearch.common.lib.aura.NodeType nodeType
                                : arcaneresearch.common.lib.aura.NodeType.values()) {
                            auraJars.add(arcaneresearch.common.items.aura.ItemJarNode.createStack(
                                    nodeType, null, auraNodeFull));
                            auraNodes.add(arcaneresearch.common.items.aura.ItemAuraNode.createStack(
                                    nodeType, null, auraNodeFull));
                        }
                        for (ItemStack auraJar : auraJars) {
                            output.accept(auraJar);
                        }
                        for (ItemStack auraNode : auraNodes) {
                            output.accept(auraNode);
                        }
                        output.accept(arcaneresearch.common.blocks.aura.AuraBlocks.STABILIZER_ITEM.get());
                        output.accept(arcaneresearch.common.blocks.aura.AuraBlocks.STABILIZER_ADVANCED_ITEM.get());
                        output.accept(arcaneresearch.common.blocks.aura.AuraBlocks.TRANSDUCER_ITEM.get());
                        acceptBlock(output, AuraBlocks.NODE_CONVERTER.get());
                        acceptBlock(output, AuraBlocks.AURA_TOTEM.get());
                        acceptBlock(output, AuraBlocks.OBSIDIAN_TOTEM.get());
                        acceptBlock(output, ModBlocks.INFUSION_MATRIX.get());
                        acceptBlock(output, ModBlocks.EVERFULL_URN.get());
                        acceptBlock(output, ModBlocks.THAUMATORIUM.get());
                        acceptBlock(output, ModBlocks.BRAIN_BOX.get());
                        acceptBlock(output, ModBlocks.SPA.get());
                        acceptBlock(output, ModBlocks.GOLEM_BUILDER.get());
                        acceptBlock(output, ModBlocks.MIRROR_ITEM.get());
                        acceptBlock(output, ModBlocks.MIRROR_ESSENTIA.get());
                        acceptBlock(output, ModBlocks.ESSENTIA_INPUT.get());
                        acceptBlock(output, ModBlocks.ESSENTIA_OUTPUT.get());
                        acceptBlock(output, ModBlocks.REDSTONE_RELAY.get());
                        acceptBlock(output, ModBlocks.PATTERN_CRAFTER.get());
                        acceptBlock(output, ModBlocks.POTION_SPRAYER.get());
                        acceptBlock(output, ModBlocks.ACTIVATOR_RAIL.get());
                        acceptBlock(output, ModBlocks.STABILIZER.get());
                        acceptBlock(output, ModBlocks.VIS_GENERATOR.get());
                        acceptBlock(output, ModBlocks.CONDENSER.get());
                        acceptBlock(output, ModBlocks.CONDENSER_LATTICE.get());
                        acceptBlock(output, ModBlocks.CONDENSER_LATTICE_DIRTY.get());
                        acceptBlock(output, ModBlocks.VOID_SIPHON.get());
                        acceptBlock(output, ModBlocks.ESSENTIA_RESERVOIR.get());
                        acceptBlock(output, ModBlocks.FLUX_SCRUBBER.get());
                        output.accept(ModItems.THAUMONOMICON.get());
                        if (arcaneresearch.common.config.ModConfig.allowCheatSheet) {
                            output.accept(ModItems.THAUMONOMICON_CHEAT.get());
                        }
                        output.accept(ModItems.CURIOSITY.get());
                        output.accept(ModItems.CURIO_ARCANE.get());
                        output.accept(ModItems.CURIO_PRESERVED.get());
                        output.accept(ModItems.CURIO_ANCIENT.get());
                        output.accept(ModItems.CURIO_ELDRITCH.get());
                        output.accept(ModItems.CURIO_KNOWLEDGE.get());
                        output.accept(ModItems.CURIO_TWISTED.get());
                        output.accept(ModItems.CURIO_RITES.get());
                        output.accept(ModItems.LOOT_BAG_COMMON.get());
                        output.accept(ModItems.LOOT_BAG_UNCOMMON.get());
                        output.accept(ModItems.LOOT_BAG_RARE.get());
                        output.accept(ModItems.PRIMORDIAL_PEARL.get());
                        output.accept(ModItems.ELDRITCH_EYE.get());
                        output.accept(ModItems.PECH_WAND.get());
                        output.accept(ModItems.CELESTIAL_NOTES_SUN.get());
                        output.accept(ModItems.CELESTIAL_NOTES_STARS_1.get());
                        output.accept(ModItems.CELESTIAL_NOTES_STARS_2.get());
                        output.accept(ModItems.CELESTIAL_NOTES_STARS_3.get());
                        output.accept(ModItems.CELESTIAL_NOTES_STARS_4.get());
                        output.accept(ModItems.CELESTIAL_NOTES_MOON_1.get());
                        output.accept(ModItems.CELESTIAL_NOTES_MOON_2.get());
                        output.accept(ModItems.CELESTIAL_NOTES_MOON_3.get());
                        output.accept(ModItems.CELESTIAL_NOTES_MOON_4.get());
                        output.accept(ModItems.CELESTIAL_NOTES_MOON_5.get());
                        output.accept(ModItems.CELESTIAL_NOTES_MOON_6.get());
                        output.accept(ModItems.CELESTIAL_NOTES_MOON_7.get());
                        output.accept(ModItems.CELESTIAL_NOTES_MOON_8.get());
                        output.accept(ModItems.AMBER.get());
                        output.accept(ModItems.QUICKSILVER.get());
                        output.accept(ModItems.THAUMIUM_INGOT.get());
                        output.accept(ModItems.VOID_METAL_INGOT.get());
                        output.accept(ModItems.BRASS_INGOT.get());
                        output.accept(Items.IRON_NUGGET);
                        output.accept(ModItems.COPPER_NUGGET.get());
                        output.accept(ModItems.TIN_NUGGET.get());
                        output.accept(ModItems.SILVER_NUGGET.get());
                        output.accept(ModItems.LEAD_NUGGET.get());
                        output.accept(ModItems.QUICKSILVER_NUGGET.get());
                        output.accept(ModItems.THAUMIUM_NUGGET.get());
                        output.accept(ModItems.VOID_METAL_NUGGET.get());
                        output.accept(ModItems.BRASS_NUGGET.get());
                        output.accept(ModItems.QUARTZ_NUGGET.get());
                        output.accept(ModItems.RAREEARTH_NUGGET.get());
                        output.accept(ModItems.CLUSTER_IRON.get());
                        output.accept(ModItems.CLUSTER_GOLD.get());
                        output.accept(ModItems.CLUSTER_COPPER.get());
                        output.accept(ModItems.CLUSTER_TIN.get());
                        output.accept(ModItems.CLUSTER_SILVER.get());
                        output.accept(ModItems.CLUSTER_LEAD.get());
                        output.accept(ModItems.CLUSTER_CINNABAR.get());
                        output.accept(ModItems.CLUSTER_QUARTZ.get());
                        output.accept(ModItems.ENCHANTED_FABRIC.get());
                        output.accept(ModItems.VIS_RESONATOR.get());
                        output.accept(ModItems.TALLOW.get());
                        output.accept(ModItems.MECHANISM_SIMPLE.get());
                        output.accept(ModItems.MECHANISM_COMPLEX.get());
                        output.accept(ModItems.PLATE_BRASS.get());
                        output.accept(ModItems.PLATE_IRON.get());
                        output.accept(ModItems.PLATE_THAUMIUM.get());
                        output.accept(ModItems.PLATE_VOID.get());
                        output.accept(ModItems.FILTER.get());
                        output.accept(ModItems.MORPHIC_RESONATOR.get());
                        output.accept(ModItems.SALIS_MUNDUS.get());
                        output.accept(ModItems.MIRRORED_GLASS.get());
                        output.accept(ModItems.VOID_SEED.get());
                        output.accept(ModItems.BRAIN_CLOCKWORK.get());
                        output.accept(ModItems.BRAIN_CURIOUS.get());
                        output.accept(ModItems.GOLEM_MODULE_AGGRESSION.get());
                        output.accept(ModItems.GOLEM_MODULE_VISION.get());
                        for (Aspect aspect : Aspect.aspects.values()) {
                            output.accept(arcaneresearch.common.items.resources.ItemCrystalEssence.createStack(
                                    ModItems.CRYSTAL_ESSENCE.get(), aspect, 1));
                        }
                        for (Aspect aspect : Aspect.aspects.values()) {
                            output.accept(arcaneresearch.common.items.resources.ItemWispEssence.createStack(
                                    ModItems.WISP_ESSENCE.get(), aspect, 1));
                        }
                        output.accept(ModItems.CHUNKS_BEEF.get());
                        output.accept(ModItems.CHUNKS_CHICKEN.get());
                        output.accept(ModItems.CHUNKS_PORK.get());
                        output.accept(ModItems.CHUNKS_FISH.get());
                        output.accept(ModItems.CHUNKS_RABBIT.get());
                        output.accept(ModItems.CHUNKS_MUTTON.get());
                        output.accept(ModItems.TRIPLE_MEAT_TREAT.get());
                        output.accept(ModItems.ZOMBIE_BRAIN.get());
                        output.accept(ModItems.LABEL_BLANK.get());
                        for (Aspect aspect : Aspect.aspects.values()) {
                            ItemStack label = new ItemStack(ModItems.LABEL_FILLED.get());
                            ((ItemLabel) label.getItem()).setAspect(label, aspect);
                            output.accept(label);
                        }
                        output.accept(ModItems.PHIAL_EMPTY.get());
                        for (Aspect aspect : Aspect.aspects.values()) {
                            output.accept(arcaneresearch.common.items.consumables.ItemPhial.makeFilledPhial(aspect));
                        }
                        output.accept(ModItems.ALUMENTUM.get());
                        output.accept(ModItems.JAR_BRACE.get());
                        output.accept(ModItems.BOTTLE_TAINT.get());
                        output.accept(ModFluids.LIQUID_DEATH_BUCKET.get());
                        output.accept(ModFluids.PURIFYING_FLUID_BUCKET.get());
                        output.accept(ModItems.SANITY_SOAP.get());
                        output.accept(ModItems.BATH_SALTS.get());
                        output.accept(ModItems.TURRET_PLACER_BASIC.get());
                        output.accept(ModItems.TURRET_PLACER_ADVANCED.get());
                        output.accept(ModItems.TURRET_PLACER_BORE.get());
                        output.accept(ModItems.CAUSALITY_COLLAPSER.get());
                        output.accept(ModItems.SCRIBING_TOOLS.get());
                        output.accept(ModItems.THAUMOMETER.get());
                        output.accept(ModItems.RESONATOR.get());
                        output.accept(ModItems.SANITY_CHECKER.get());
                        output.accept(ModItems.HAND_MIRROR.get());
                        output.accept(ModItems.INHABITED_ZOMBIE_SPAWN_EGG.get());
                        output.accept(ModItems.ELDRITCH_GUARDIAN_SPAWN_EGG.get());
                        output.accept(ModItems.WISP_SPAWN_EGG.get());
                        output.accept(ModItems.FIRE_BAT_SPAWN_EGG.get());
                        output.accept(ModItems.BRAINY_ZOMBIE_SPAWN_EGG.get());
                        output.accept(ModItems.MIND_SPIDER_SPAWN_EGG.get());
                        output.accept(ModItems.THAUMIC_SLIME_SPAWN_EGG.get());
                        output.accept(ModItems.GIANT_BRAINY_ZOMBIE_SPAWN_EGG.get());
                        output.accept(ModItems.ELDRITCH_CRAB_SPAWN_EGG.get());
                        output.accept(ModItems.SPELL_BAT_SPAWN_EGG.get());
                        output.accept(ModItems.CULTIST_SPAWN_EGG.get());
                        output.accept(ModItems.CULTIST_KNIGHT_SPAWN_EGG.get());
                        output.accept(ModItems.CULTIST_CLERIC_SPAWN_EGG.get());
                        output.accept(ModItems.PECH_SPAWN_EGG.get());
                        output.accept(ModItems.TAINT_CRAWLER_SPAWN_EGG.get());
                        output.accept(ModItems.TAINT_SWARM_SPAWN_EGG.get());
                        output.accept(ModItems.CULTIST_LEADER_SPAWN_EGG.get());
                        output.accept(ModItems.TAINTACLE_GIANT_SPAWN_EGG.get());
                        output.accept(ModItems.ELDRITCH_GOLEM_SPAWN_EGG.get());
                        output.accept(ModItems.ELDRITCH_WARDEN_SPAWN_EGG.get());
                        output.accept(ModItems.THAUMIUM_AXE.get());
                        output.accept(ModItems.THAUMIUM_SWORD.get());
                        output.accept(ModItems.THAUMIUM_SHOVEL.get());
                        output.accept(ModItems.THAUMIUM_PICK.get());
                        output.accept(ModItems.THAUMIUM_HOE.get());
                        output.accept(ModItems.VOID_AXE.get());
                        output.accept(ModItems.VOID_SWORD.get());
                        output.accept(ModItems.VOID_SHOVEL.get());
                        output.accept(ModItems.VOID_PICK.get());
                        output.accept(ModItems.VOID_HOE.get());
                        output.accept(withInfusion(ModItems.ELEMENTAL_AXE.get(),
                                EnumInfusionEnchantment.BURROWING, 1,
                                EnumInfusionEnchantment.COLLECTOR, 1));
                        output.accept(withInfusion(ModItems.ELEMENTAL_SWORD.get(),
                                EnumInfusionEnchantment.ARCING, 2, null, 0));
                        output.accept(withInfusion(ModItems.ELEMENTAL_SHOVEL.get(),
                                EnumInfusionEnchantment.DESTRUCTIVE, 1, null, 0));
                        output.accept(withInfusion(ModItems.ELEMENTAL_PICK.get(),
                                EnumInfusionEnchantment.REFINING, 1,
                                EnumInfusionEnchantment.SOUNDING, 2));
                        output.accept(ModItems.ELEMENTAL_HOE.get());
                        output.accept(withInfusion(ModItems.PRIMAL_CRUSHER.get(),
                                EnumInfusionEnchantment.DESTRUCTIVE, 1,
                                EnumInfusionEnchantment.REFINING, 1));
                        output.accept(ModItems.CRIMSON_BLADE.get());
                        output.accept(ModItems.GRAPPLE_GUN.get());
                        output.accept(ModItems.GRAPPLE_GUN_TIP.get());
                        output.accept(ModItems.GRAPPLE_GUN_SPOOL.get());
                        output.accept(ModItems.BONE_BOW.get());
                        output.accept(ModItems.PRIMAL_ARROW_AIR.get());
                        output.accept(ModItems.PRIMAL_ARROW_FIRE.get());
                        output.accept(ModItems.PRIMAL_ARROW_WATER.get());
                        output.accept(ModItems.PRIMAL_ARROW_EARTH.get());
                        output.accept(ModItems.PRIMAL_ARROW_ORDER.get());
                        output.accept(ModItems.PRIMAL_ARROW_ENTROPY.get());
                        output.accept(ModItems.GOGGLES.get());
                        output.accept(ModItems.CLOTH_CHEST.get());
                        output.accept(ModItems.CLOTH_LEGS.get());
                        output.accept(ModItems.CLOTH_BOOTS.get());
                        output.accept(ModItems.THAUMIUM_HELM.get());
                        output.accept(ModItems.THAUMIUM_CHEST.get());
                        output.accept(ModItems.THAUMIUM_LEGS.get());
                        output.accept(ModItems.THAUMIUM_BOOTS.get());
                        output.accept(ModItems.TRAVELLER_BOOTS.get());
                        output.accept(ModItems.FORTRESS_HELM.get());
                        output.accept(ModItems.FORTRESS_CHEST.get());
                        output.accept(ModItems.FORTRESS_LEGS.get());
                        output.accept(ModItems.VOID_HELM.get());
                        output.accept(ModItems.VOID_CHEST.get());
                        output.accept(ModItems.VOID_LEGS.get());
                        output.accept(ModItems.VOID_BOOTS.get());
                        output.accept(ModItems.VOID_ROBE_HELM.get());
                        output.accept(ModItems.VOID_ROBE_CHEST.get());
                        output.accept(ModItems.VOID_ROBE_LEGS.get());
                        output.accept(ModItems.CRIMSON_PLATE_HELM.get());
                        output.accept(ModItems.CRIMSON_PLATE_CHEST.get());
                        output.accept(ModItems.CRIMSON_PLATE_LEGS.get());
                        output.accept(ModItems.CRIMSON_BOOTS.get());
                        output.accept(ModItems.CRIMSON_ROBE_HELM.get());
                        output.accept(ModItems.CRIMSON_ROBE_CHEST.get());
                        output.accept(ModItems.CRIMSON_ROBE_LEGS.get());
                        output.accept(ModItems.CRIMSON_PRAETOR_HELM.get());
                        output.accept(ModItems.CRIMSON_PRAETOR_CHEST.get());
                        output.accept(ModItems.CRIMSON_PRAETOR_LEGS.get());
                        output.accept(runicMaxed(ModItems.GOGGLES.get()));
                        output.accept(runicMaxed(ModItems.CLOTH_CHEST.get()));
                        output.accept(runicMaxed(ModItems.CLOTH_LEGS.get()));
                        output.accept(runicMaxed(ModItems.CLOTH_BOOTS.get()));
                        output.accept(runicMaxed(ModItems.THAUMIUM_HELM.get()));
                        output.accept(runicMaxed(ModItems.THAUMIUM_CHEST.get()));
                        output.accept(runicMaxed(ModItems.THAUMIUM_LEGS.get()));
                        output.accept(runicMaxed(ModItems.THAUMIUM_BOOTS.get()));
                        output.accept(runicMaxed(ModItems.TRAVELLER_BOOTS.get()));
                        output.accept(runicMaxed(ModItems.FORTRESS_HELM.get()));
                        output.accept(runicMaxed(ModItems.FORTRESS_CHEST.get()));
                        output.accept(runicMaxed(ModItems.FORTRESS_LEGS.get()));
                        output.accept(runicMaxed(ModItems.VOID_HELM.get()));
                        output.accept(runicMaxed(ModItems.VOID_CHEST.get()));
                        output.accept(runicMaxed(ModItems.VOID_LEGS.get()));
                        output.accept(runicMaxed(ModItems.VOID_BOOTS.get()));
                        output.accept(runicMaxed(ModItems.VOID_ROBE_HELM.get()));
                        output.accept(runicMaxed(ModItems.VOID_ROBE_CHEST.get()));
                        output.accept(runicMaxed(ModItems.VOID_ROBE_LEGS.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_PLATE_HELM.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_PLATE_CHEST.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_PLATE_LEGS.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_BOOTS.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_ROBE_HELM.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_ROBE_CHEST.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_ROBE_LEGS.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_PRAETOR_HELM.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_PRAETOR_CHEST.get()));
                        output.accept(runicMaxed(ModItems.CRIMSON_PRAETOR_LEGS.get()));
                        output.accept(ModItems.BAUBLE_AMULET_MUNDANE.get());
                        output.accept(ModItems.BAUBLE_RING_MUNDANE.get());
                        output.accept(ModItems.BAUBLE_GIRDLE_MUNDANE.get());
                        output.accept(ModItems.BAUBLE_RING_APPRENTICE.get());
                        output.accept(ModItems.BAUBLE_AMULET_FANCY.get());
                        output.accept(ModItems.BAUBLE_RING_FANCY.get());
                        output.accept(ModItems.BAUBLE_GIRDLE_FANCY.get());
                        output.accept(ModItems.RUNIC_AMULET.get());
                        output.accept(ModItems.RUNIC_AMULET_EMERGENCY.get());
                        output.accept(ModItems.RUNIC_RING_LESSER.get());
                        output.accept(ModItems.RUNIC_RING.get());
                        output.accept(ModItems.RUNIC_RING_CHARGED.get());
                        output.accept(ModItems.RUNIC_RING_REGEN.get());
                        output.accept(ModItems.RUNIC_GIRDLE.get());
                        output.accept(ModItems.RUNIC_GIRDLE_KINETIC.get());
                        output.accept(ModItems.AMULET_VIS_FOUND.get());
                        output.accept(ModItems.AMULET_VIS_CRAFTED.get());
                        output.accept(ModItems.VERDANT_CHARM.get());
                        output.accept(ModItems.VERDANT_CHARM_LIFE.get());
                        output.accept(ModItems.VERDANT_CHARM_SUSTAIN.get());
                        output.accept(ModItems.CURIOSITY_BAND.get());
                        output.accept(ModItems.VOIDSEER_CHARM.get());
                        output.accept(ModItems.CLOUD_RING.get());
                        output.accept(ModItems.CHARM_UNDYING.get());
                        output.accept(ModItems.CREATIVE_FLUX_SPONGE.get());
                        output.accept(ModItems.ENCHANTED_PLACEHOLDER.get());
                        output.accept(ModItems.CASTER_BASIC.get());
                        output.accept(ModItems.FOCUS_1.get());
                        output.accept(ModItems.FOCUS_BLANK.get());
                        output.accept(ModItems.FOCUS_ADVANCED.get());
                        output.accept(ModItems.FOCUS_POUCH.get());
                        arcaneresearch.common.items.wands.WandItems.initWands();
                        for (arcaneresearch.api.wands.WandCap cap : arcaneresearch.api.wands.WandCap.caps.values()) {
                            output.accept(arcaneresearch.common.items.wands.ItemWandCap.createStack(cap));
                        }
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND.get());
                        for (arcaneresearch.api.wands.WandRod rod : arcaneresearch.api.wands.WandRod.rods.values()) {
                            for (arcaneresearch.api.wands.WandCap cap : arcaneresearch.api.wands.WandCap.caps.values()) {
                                output.accept(createWandStack(rod, cap));
                            }
                        }
                        for (arcaneresearch.api.wands.WandRod rod : arcaneresearch.api.wands.WandRod.rods.values()) {
                            for (arcaneresearch.api.wands.WandCap cap : arcaneresearch.api.wands.WandCap.caps.values()) {
                                output.accept(createEnchantedWandStack(rod, cap));
                            }
                        }
                        output.accept(createSceptreStack("wood", "iron"));
                        output.accept(createSceptreStack("greatwood", "gold"));
                        output.accept(createSceptreStack("silverwood", "thaumium"));
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_POUCH.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_FIRE.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_FROST.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_SHOCK.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_EXCAVATION.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_BUILDER.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_TRADE.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_HOLE.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_GRAPPLE.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_SHARD.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_PRIMAL.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_HELLBAT.get());
                        output.accept(arcaneresearch.common.items.wands.WandItems.WAND_FOCUS_PECH.get());
                        output.accept(ModItems.GOLEM_BELL.get());
                        output.accept(ModItems.GOLEM_PLACER.get());
                        output.accept(createGolemStackById(0, 0, 0, 0, 0));
                        output.accept(createGolemStackById(0, 1, 1, 0, 0));
                        output.accept(createGolemStackById(1, 1, 2, 0, 0));
                        output.accept(createGolemStackById(4, 1, 3, 0, 0));
                        acceptBlock(output, ModBlocks.GOLEM_BUILDER.get());
                        output.accept(ModItems.SEAL_BLANK.get());
                        output.accept(ModItems.SEAL_PICKUP.get());
                        output.accept(ModItems.SEAL_PICKUP_ADVANCED.get());
                        output.accept(ModItems.SEAL_FILL.get());
                        output.accept(ModItems.SEAL_EMPTY.get());
                        output.accept(ModItems.SEAL_HARVEST.get());
                        output.accept(ModItems.SEAL_BUTCHER.get());
                        output.accept(ModItems.SEAL_GUARD.get());
                        output.accept(ModItems.SEAL_LUMBER.get());
                        output.accept(ModItems.SEAL_BREAKER.get());
                        output.accept(ModItems.SEAL_USE.get());
                        output.accept(ModItems.SEAL_PROVIDE.get());
                        output.accept(ModItems.SEAL_STOCK.get());
                        output.accept(ModItems.SEAL_BREAKER_ADVANCED.get());
                        output.accept(ModItems.SEAL_EMPTY_ADVANCED.get());
                        output.accept(ModItems.SEAL_FILL_ADVANCED.get());
                        output.accept(ModItems.SEAL_GUARD_ADVANCED.get());
                    })
                    .build());
    private static void acceptBlock(CreativeModeTab.Output output, Block block) {
        if (block.asItem() != Items.AIR) {
            output.accept(block);
        }
    }
    private static ItemStack withInfusion(Item item, EnumInfusionEnchantment first, int firstLevel,
                                          EnumInfusionEnchantment second, int secondLevel) {
        ItemStack stack = new ItemStack(item);
        EnumInfusionEnchantment.addInfusionEnchantment(stack, first, firstLevel);
        if (second != null) {
            EnumInfusionEnchantment.addInfusionEnchantment(stack, second, secondLevel);
        }
        return stack;
    }
    private static ItemStack createWandStack(arcaneresearch.api.wands.WandRod rod,
                                             arcaneresearch.api.wands.WandCap cap) {
        ItemStack stack = new ItemStack(arcaneresearch.common.items.wands.WandItems.WAND.get());
        if (stack.getItem() instanceof arcaneresearch.api.wands.IWand wand) {
            wand.setRod(stack, rod);
            wand.setCap(stack, cap);
        }
        return stack;
    }
    private static ItemStack runicMaxed(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putByte("TC.RUNIC", (byte) 5);
        return stack;
    }
    private static ItemStack createEnchantedWandStack(arcaneresearch.api.wands.WandRod rod,
                                                      arcaneresearch.api.wands.WandCap cap) {
        ItemStack stack = createWandStack(rod, cap);
        arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment.addInfusionEnchantment(stack,
                arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment.MAGIC_EFFICIENCY, 3);
        return stack;
    }
    private static ItemStack createSceptreStack(String rodTag, String capTag) {
        ItemStack stack = new ItemStack(arcaneresearch.common.items.wands.WandItems.WAND.get());
        if (stack.getItem() instanceof arcaneresearch.api.wands.IWand wand) {
            arcaneresearch.api.wands.WandRod rod =
                    arcaneresearch.api.wands.WandRod.rods.get(rodTag);
            arcaneresearch.api.wands.WandCap cap =
                    arcaneresearch.api.wands.WandCap.caps.get(capTag);
            if (rod != null && cap != null) {
                wand.setRod(stack, rod);
                wand.setCap(stack, cap);
                arcaneresearch.common.items.wands.ItemWand.setSceptre(stack);
            }
        }
        return stack;
    }
    private static ItemStack createGolemStackById(int materialId, int headId, int armsId, int legsId, int addonId) {
        IGolemProperties props = new GolemProperties();
        props.setMaterial(GolemMaterial.getById(materialId));
        props.setHead(GolemHead.getById(headId));
        props.setArms(GolemArm.getById(armsId));
        props.setLegs(GolemLeg.getById(legsId));
        props.setAddon(GolemAddon.getById(addonId));
        return ItemGolemPlacer.createGolemStack(ModItems.GOLEM_PLACER.get(), props, 0);
    }
}
