package arcaneresearch.common.config;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IDustTrigger;
import arcaneresearch.api.crafting.Part;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.items.aura.ItemAuraNode;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.lib.crafting.DustTriggerMultiblock;
import arcaneresearch.common.lib.crafting.DustTriggerOre;
import arcaneresearch.common.lib.crafting.DustTriggerSimple;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
public class ConfigMultiblocks {
    public static void init() {
        IDustTrigger.clearTriggers();
        registerSimpleTriggers();
        registerInfernalFurnace();
        registerInfusionAltars();
        registerThaumatorium();
        registerGolemPress();
        registerNodeJar();
        registerEldritchPortal();
        Arcaneresearch.LOGGER.info("Registered {} dust triggers", IDustTrigger.getTriggers().size());
    }
    private static void registerSimpleTriggers() {
        IDustTrigger.registerDustTrigger(new DustTriggerSimple(
                "!gotdream",
                Blocks.BOOKSHELF,
                new ItemStack(ModItems.THAUMONOMICON.get())
        ));
        IDustTrigger.registerDustTrigger(new DustTriggerSimple(
                "FIRSTSTEPS@1",
                Blocks.CRAFTING_TABLE,
                new ItemStack(ModBlocks.ARCANE_WORKBENCH.get())
        ));
        IDustTrigger.registerDustTrigger(new DustTriggerSimple(
                "UNLOCKALCHEMY@1",
                Blocks.CAULDRON,
                new ItemStack(ModBlocks.CRUCIBLE.get())
        ));
        IDustTrigger.registerDustTrigger(new DustTriggerOre(
                "!gotdream",
                TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                        new ResourceLocation("forge", "bookshelves")),
                new ItemStack(ModItems.THAUMONOMICON.get())));
        IDustTrigger.registerDustTrigger(new DustTriggerOre(
                "FIRSTSTEPS@1",
                TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                        new ResourceLocation("forge", "workbench")),
                new ItemStack(ModBlocks.ARCANE_WORKBENCH.get())));
    }
    private static void registerInfernalFurnace() {
        Part NB = new Part(Blocks.NETHER_BRICKS, ModBlocks.PLACEHOLDER_NETHERBRICK.get());
        Part OB = new Part(Blocks.OBSIDIAN, ModBlocks.PLACEHOLDER_OBSIDIAN.get());
        Part IB = new Part(Blocks.IRON_BARS, ItemStack.EMPTY);
        Part LA = new Part(Blocks.LAVA, ModBlocks.INFERNAL_FURNACE.get(), true);
        Part[][][] blueprint = {
                {
                        {NB, OB, NB},
                        {OB, null, OB},
                        {NB, OB, NB}
                },
                {
                        {NB, OB, NB},
                        {OB, LA, OB},
                        {NB, IB, NB}
                },
                {
                        {NB, OB, NB},
                        {OB, OB, OB},
                        {NB, OB, NB}
                }
        };
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFERNALFURNACE", blueprint));
        ArcaneresearchApi.addMultiblockRecipeToCatalog(
                new ResourceLocation(Arcaneresearch.MODID, "infernalfurnace"),
                new ArcaneresearchApi.BluePrint("INFERNALFURNACE", blueprint,
                        new ItemStack(Blocks.NETHER_BRICKS, 12),
                        new ItemStack(Blocks.OBSIDIAN, 12),
                        new ItemStack(Blocks.IRON_BARS),
                        new ItemStack(Items.LAVA_BUCKET)
                )
        );
    }
    private static void registerInfusionAltars() {
        Part IM = new Part(ModBlocks.INFUSION_MATRIX.get(), null);
        Part SNT = new Part(ModBlocks.ARCANE_STONE.get(), ItemStack.EMPTY);
        net.minecraft.world.level.block.Block arcanePillar = ModBlocks.ARCANE_PILLAR.get();
        Part SNB1 = new Part(ModBlocks.ARCANE_STONE.get(), arcanePillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.EAST));
        Part SNB2 = new Part(ModBlocks.ARCANE_STONE.get(), arcanePillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.NORTH));
        Part SNB3 = new Part(ModBlocks.ARCANE_STONE.get(), arcanePillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.SOUTH));
        Part SNB4 = new Part(ModBlocks.ARCANE_STONE.get(), arcanePillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.WEST));
        Part PN = new Part(ModBlocks.PEDESTAL_ARCANE.get(), null);
        Part[][][] normalBlueprint = {
                {
                        {null, null, null},
                        {null, IM, null},
                        {null, null, null}
                },
                {
                        {SNT, null, SNT},
                        {null, null, null},
                        {SNT, null, SNT}
                },
                {
                        {SNB1, null, SNB3},
                        {null, PN, null},
                        {SNB2, null, SNB4}
                }
        };
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFUSION", normalBlueprint));
        ArcaneresearchApi.addMultiblockRecipeToCatalog(
                new ResourceLocation(Arcaneresearch.MODID, "infusionaltar"),
                new ArcaneresearchApi.BluePrint("INFUSION", normalBlueprint,
                        new ItemStack(ModBlocks.ARCANE_STONE.get(), 8),
                        new ItemStack(ModBlocks.PEDESTAL_ARCANE.get()),
                        new ItemStack(ModBlocks.INFUSION_MATRIX.get())
                )
        );
        Part SAT = new Part(ModBlocks.ANCIENT_STONE.get(), ItemStack.EMPTY);
        net.minecraft.world.level.block.Block ancientPillar = ModBlocks.ANCIENT_PILLAR.get();
        Part SAB1 = new Part(ModBlocks.ANCIENT_STONE.get(), ancientPillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.EAST));
        Part SAB2 = new Part(ModBlocks.ANCIENT_STONE.get(), ancientPillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.NORTH));
        Part SAB3 = new Part(ModBlocks.ANCIENT_STONE.get(), ancientPillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.SOUTH));
        Part SAB4 = new Part(ModBlocks.ANCIENT_STONE.get(), ancientPillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.WEST));
        Part PA = new Part(ModBlocks.PEDESTAL_ANCIENT.get(), null);
        Part[][][] ancientBlueprint = {
                {{null, null, null}, {null, IM, null}, {null, null, null}},
                {{SAT, null, SAT}, {null, null, null}, {SAT, null, SAT}},
                {{SAB1, null, SAB3}, {null, PA, null}, {SAB2, null, SAB4}}
        };
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFUSIONANCIENT", ancientBlueprint));
        ArcaneresearchApi.addMultiblockRecipeToCatalog(
                new ResourceLocation(Arcaneresearch.MODID, "infusionaltarancient"),
                new ArcaneresearchApi.BluePrint("INFUSIONANCIENT", ancientBlueprint,
                        new ItemStack(ModBlocks.ANCIENT_STONE.get(), 8),
                        new ItemStack(ModBlocks.PEDESTAL_ANCIENT.get()),
                        new ItemStack(ModBlocks.INFUSION_MATRIX.get())
                )
        );
        Part SET = new Part(ModBlocks.ELDRITCH_STONE_TILE.get(), ItemStack.EMPTY);
        net.minecraft.world.level.block.Block eldritchPillar = ModBlocks.ELDRITCH_PILLAR.get();
        Part SEB1 = new Part(ModBlocks.ELDRITCH_STONE_TILE.get(), eldritchPillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.EAST));
        Part SEB2 = new Part(ModBlocks.ELDRITCH_STONE_TILE.get(), eldritchPillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.NORTH));
        Part SEB3 = new Part(ModBlocks.ELDRITCH_STONE_TILE.get(), eldritchPillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.SOUTH));
        Part SEB4 = new Part(ModBlocks.ELDRITCH_STONE_TILE.get(), eldritchPillar.defaultBlockState()
                .setValue(arcaneresearch.common.blocks.basic.BlockPillarTC.FACING, Direction.WEST));
        Part PE = new Part(ModBlocks.PEDESTAL_ELDRITCH.get(), null);
        Part[][][] eldritchBlueprint = {
                {{null, null, null}, {null, IM, null}, {null, null, null}},
                {{SET, null, SET}, {null, null, null}, {SET, null, SET}},
                {{SEB1, null, SEB3}, {null, PE, null}, {SEB2, null, SEB4}}
        };
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("INFUSIONELDRITCH", eldritchBlueprint));
        ArcaneresearchApi.addMultiblockRecipeToCatalog(
                new ResourceLocation(Arcaneresearch.MODID, "infusionaltareldritch"),
                new ArcaneresearchApi.BluePrint("INFUSIONELDRITCH", eldritchBlueprint,
                        new ItemStack(ModBlocks.ELDRITCH_STONE_TILE.get(), 8),
                        new ItemStack(ModBlocks.PEDESTAL_ELDRITCH.get()),
                        new ItemStack(ModBlocks.INFUSION_MATRIX.get())
                )
        );
    }
    private static void registerThaumatorium() {
        Part TH1 = new Part(ModBlocks.ALCHEMICAL_BRASS_BLOCK.get(), ModBlocks.THAUMATORIUM_TOP.get()).setApplyPlayerFacing(true);
        Part TH2 = new Part(ModBlocks.ALCHEMICAL_BRASS_BLOCK.get(), ModBlocks.THAUMATORIUM.get()).setApplyPlayerFacing(true);
        Part TH3 = new Part(ModBlocks.CRUCIBLE.get(), null);
        Part[][][] blueprint = {
                {{TH1}},
                {{TH2}},
                {{TH3}}
        };
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("THAUMATORIUM", blueprint));
        ArcaneresearchApi.addMultiblockRecipeToCatalog(
                new ResourceLocation(Arcaneresearch.MODID, "thaumatorium"),
                new ArcaneresearchApi.BluePrint("THAUMATORIUM", blueprint,
                        new ItemStack(ModBlocks.ALCHEMICAL_BRASS_BLOCK.get(), 2),
                        new ItemStack(ModBlocks.CRUCIBLE.get())
                )
        );
    }
    private static void registerNodeJar() {
        Part SLAB = new Part(Blocks.OAK_SLAB, ItemStack.EMPTY);
        Part GLASS = new Part(Blocks.GLASS, ItemStack.EMPTY);
        Part NODE = new Part(
                AuraBlocks.AURA_NODE.get(), ItemStack.EMPTY).setNodeType(NodeType.NORMAL);
        Part[][][] blueprint = {
                {{SLAB, SLAB, SLAB}, {SLAB, SLAB, SLAB}, {SLAB, SLAB, SLAB}},
                {{GLASS, GLASS, GLASS}, {GLASS, GLASS, GLASS}, {GLASS, GLASS, GLASS}},
                {{GLASS, GLASS, GLASS}, {GLASS, NODE, GLASS}, {GLASS, GLASS, GLASS}},
                {{GLASS, GLASS, GLASS}, {GLASS, GLASS, GLASS}, {GLASS, GLASS, GLASS}}
        };
        ArcaneresearchApi.addMultiblockRecipeToCatalog(
                new ResourceLocation(Arcaneresearch.MODID, "nodejar"),
                new ArcaneresearchApi.BluePrint("LEGACYJARNODE",
                        new ItemStack(
                                AuraBlocks.JAR_NODE.get()),
                        blueprint,
                        new ItemStack(Blocks.GLASS, 26),
                        new ItemStack(Blocks.OAK_SLAB, 9),
                        ItemAuraNode.createStack(NodeType.NORMAL, null, primalDisplayAspects())
                )
        );
    }
    private static void registerEldritchPortal() {
        Part TILE = new Part(ModBlocks.ELDRITCH_STONE_TILE.get(), ItemStack.EMPTY);
        Part ALTAR = new Part(
                AuraBlocks.ELDRITCH_ALTAR.get(), ItemStack.EMPTY);
        Part NODE = new Part(
                AuraBlocks.AURA_NODE.get(), ItemStack.EMPTY).setNodeType(NodeType.DARK);
        Part[][][] blueprint = {
                {
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, NODE, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null}
                },
                {
                        {null, null, null, null, null},
                        {null, null, null, null, null},
                        {null, null, ALTAR, null, null},
                        {null, null, null, null, null},
                        {null, null, null, null, null}
                },
                {
                        {TILE, TILE, TILE, TILE, TILE},
                        {TILE, TILE, TILE, TILE, TILE},
                        {TILE, TILE, TILE, TILE, TILE},
                        {TILE, TILE, TILE, TILE, TILE},
                        {TILE, TILE, TILE, TILE, TILE}
                }
        };
        ArcaneresearchApi.addMultiblockRecipeToCatalog(
                new ResourceLocation(Arcaneresearch.MODID, "eldritchportal"),
                new ArcaneresearchApi.BluePrint("LEGACYELDRITCHALTAR",
                        new ItemStack(
                                AuraBlocks.ELDRITCH_PORTAL.get()),
                        blueprint,
                        new ItemStack(ModBlocks.ELDRITCH_STONE_TILE.get(), 25),
                        new ItemStack(
                                AuraBlocks.ELDRITCH_ALTAR.get()),
                        new ItemStack(ModItems.ELDRITCH_EYE.get(), 4),
                        ItemAuraNode.createStack(NodeType.DARK, null, primalDisplayAspects())
                )
        );
    }
    private static AspectList primalDisplayAspects() {
        AspectList aspects = new AspectList();
        try {
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (aspect != null) {
                    aspects.add(aspect, 25);
                }
            }
        } catch (Exception ignored) {
        }
        return aspects;
    }
    private static void registerGolemPress() {
        Part GP1 = new Part(Blocks.IRON_BARS, ModBlocks.PLACEHOLDER_BARS.get());
        Part GP2 = new Part(Blocks.CAULDRON, ModBlocks.PLACEHOLDER_CAULDRON.get());
        Part GP3 = new Part(Blocks.PISTON.defaultBlockState().setValue(DirectionalBlock.FACING, Direction.UP),
                ModBlocks.GOLEM_BUILDER.get());
        Part GP4 = new Part(Blocks.ANVIL, ModBlocks.PLACEHOLDER_ANVIL.get());
        Part GP5 = new Part(ModBlocks.TABLE_STONE.get(), ModBlocks.PLACEHOLDER_TABLE.get());
        Part[][][] blueprint = {
                {
                        {null, null},
                        {GP1, null}
                },
                {
                        {GP2, GP4},
                        {GP3, GP5}
                }
        };
        IDustTrigger.registerDustTrigger(new DustTriggerMultiblock("MINDCLOCKWORK", blueprint));
        ArcaneresearchApi.addMultiblockRecipeToCatalog(
                new ResourceLocation(Arcaneresearch.MODID, "golempress"),
                new ArcaneresearchApi.BluePrint("MINDCLOCKWORK",
                        new ItemStack(ModBlocks.GOLEM_BUILDER.get()),
                        blueprint,
                        new ItemStack(Blocks.IRON_BARS),
                        new ItemStack(Items.CAULDRON),
                        new ItemStack(Blocks.PISTON),
                        new ItemStack(Blocks.ANVIL),
                        new ItemStack(ModBlocks.TABLE_STONE.get())
                )
        );
    }
}
