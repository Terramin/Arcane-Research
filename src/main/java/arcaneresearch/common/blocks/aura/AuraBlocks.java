package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.items.aura.EldritchPortalItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.items.aura.ItemAuraNode;
import arcaneresearch.common.items.aura.ItemJarNode;

public class AuraBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Arcaneresearch.MODID);

    public static final DeferredRegister.Items BLOCK_ITEMS =
            DeferredRegister.createItems(Arcaneresearch.MODID);

    public static final DeferredBlock<Block> AURA_NODE = BLOCKS.registerBlock("aura_node", BlockAuraNode::new);

    public static final DeferredBlock<Block> JAR_NODE = BLOCKS.registerBlock("jar_node", BlockJarNode::new);

    public static final DeferredBlock<Block> STABILIZER = BLOCKS.registerBlock("node_stabilizer",
            props -> new BlockStabilizer(props, false));

    public static final DeferredBlock<Block> STABILIZER_ADVANCED =
            BLOCKS.registerBlock("node_stabilizer_advanced", props -> new BlockStabilizer(props, true));

    public static final DeferredBlock<Block> TRANSDUCER = BLOCKS.registerBlock("node_transducer",
            BlockTransducer::new);

    public static final DeferredBlock<Block> ENERGIZED_NODE = BLOCKS.registerBlock("energized_node",
            BlockEnergizedNode::new);

    public static final DeferredBlock<Block> AURA_TOTEM = BLOCKS.registerBlock("aura_totem",
            arcaneresearch.common.blocks.aura.AuraTotemBlock::new);

    public static final DeferredBlock<Block> OBSIDIAN_TOTEM = BLOCKS.registerBlock("obsidian_totem",
            BlockObsidianTotem::new);

    public static final DeferredBlock<Block> AIRY = BLOCKS.registerBlock("airy",
            BlockAiry::new);

    public static final DeferredBlock<Block> ELDRITCH_ALTAR = BLOCKS.registerBlock("eldritch_altar",
            arcaneresearch.common.blocks.aura.EldritchAltarBlock::new);

    public static final DeferredBlock<Block> NODE_CONVERTER = BLOCKS.registerBlock("node_converter",
            arcaneresearch.common.blocks.aura.NodeConverterBlock::new);

    public static final DeferredBlock<Block> ELDRITCH_PORTAL = BLOCKS.registerBlock("eldritch_portal",
            arcaneresearch.common.blocks.aura.EldritchPortalBlock::new);

    public static final DeferredBlock<Block> ELDRITCH_STAIRS = BLOCKS.registerBlock("eldritch_stairs",
            props -> new arcaneresearch.common.blocks.aura.EldritchStairsBlock(props,
                    () -> arcaneresearch.init.ModBlocks.ELDRITCH_STONE_TILE.get().defaultBlockState()));

    public static final DeferredBlock<Block> ELDRITCH_TRAP = BLOCKS.registerBlock("eldritch_trap",
            arcaneresearch.common.blocks.aura.EldritchTrapBlock::new);

    public static final DeferredBlock<Block> ELDRITCH_CRAB = BLOCKS.registerBlock("eldritch_crab",
            arcaneresearch.common.blocks.aura.EldritchCrabBlock::new);

    public static final DeferredBlock<Block> ELDRITCH_OBELISK = BLOCKS.registerBlock("eldritch_obelisk",
            arcaneresearch.common.blocks.aura.EldritchObeliskBlock::new);

      
    public static final DeferredBlock<Block> ELDRITCH_OBELISK_BODY = BLOCKS.registerBlock("eldritch_obelisk_body",
            arcaneresearch.common.blocks.aura.EldritchObeliskBodyBlock::new);

    public static final DeferredBlock<Block> ELDRITCH_CAP = BLOCKS.registerBlock("eldritch_cap",
            arcaneresearch.common.blocks.aura.EldritchCapBlock::new);

    
    
    public static final DeferredItem<Item> JAR_NODE_ITEM = BLOCK_ITEMS.registerItem("jar_node",
            props -> new ItemJarNode(JAR_NODE.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> AURA_NODE_ITEM = BLOCK_ITEMS.registerItem("aura_node",
            props -> new ItemAuraNode(AURA_NODE.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> STABILIZER_ITEM = BLOCK_ITEMS.registerItem("node_stabilizer",
            props -> new arcaneresearch.common.items.aura.ItemNodeDevice(STABILIZER.get(), props.useBlockDescriptionPrefix(),
                    arcaneresearch.common.items.aura.ItemNodeDevice.Kind.STABILIZER));

    public static final DeferredItem<Item> STABILIZER_ADVANCED_ITEM =
            BLOCK_ITEMS.registerItem("node_stabilizer_advanced",
                    props -> new arcaneresearch.common.items.aura.ItemNodeDevice(STABILIZER_ADVANCED.get(),
                            props.useBlockDescriptionPrefix(),
                            arcaneresearch.common.items.aura.ItemNodeDevice.Kind.ADVANCED));

    public static final DeferredItem<Item> TRANSDUCER_ITEM = BLOCK_ITEMS.registerItem("node_transducer",
            props -> new arcaneresearch.common.items.aura.ItemNodeDevice(TRANSDUCER.get(), props.useBlockDescriptionPrefix(),
                    arcaneresearch.common.items.aura.ItemNodeDevice.Kind.TRANSDUCER));

    public static final DeferredItem<Item> AURA_TOTEM_ITEM = BLOCK_ITEMS.registerItem("aura_totem",
            props -> new BlockItem(AURA_TOTEM.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> OBSIDIAN_TOTEM_ITEM = BLOCK_ITEMS.registerItem("obsidian_totem",
            props -> new BlockItem(OBSIDIAN_TOTEM.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> ELDRITCH_ALTAR_ITEM = BLOCK_ITEMS.registerItem("eldritch_altar",
            props -> new BlockItem(ELDRITCH_ALTAR.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> NODE_CONVERTER_ITEM = BLOCK_ITEMS.registerItem("node_converter",
            props -> new arcaneresearch.common.items.aura.ItemNodeDevice(NODE_CONVERTER.get(), props.useBlockDescriptionPrefix(),
                    arcaneresearch.common.items.aura.ItemNodeDevice.Kind.CONVERTER));

    public static final DeferredItem<Item> ELDRITCH_PORTAL_ITEM = BLOCK_ITEMS.registerItem("eldritch_portal",
            props -> new arcaneresearch.common.items.aura.EldritchPortalItem(ELDRITCH_PORTAL.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> ELDRITCH_STAIRS_ITEM = BLOCK_ITEMS.registerItem("eldritch_stairs",
            props -> new BlockItem(ELDRITCH_STAIRS.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> ELDRITCH_TRAP_ITEM = BLOCK_ITEMS.registerItem("eldritch_trap",
            props -> new BlockItem(ELDRITCH_TRAP.get(), props.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> ELDRITCH_CRAB_ITEM = BLOCK_ITEMS.registerItem("eldritch_crab",
            props -> new BlockItem(ELDRITCH_CRAB.get(), props.useBlockDescriptionPrefix()));

    
    

    public static final DeferredItem<Item> ELDRITCH_CAP_ITEM = BLOCK_ITEMS.registerItem("eldritch_cap",
            props -> new BlockItem(ELDRITCH_CAP.get(), props.useBlockDescriptionPrefix()));

    private AuraBlocks() {
    }
}
