package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.items.aura.EldritchPortalItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.items.aura.ItemAuraNode;
import arcaneresearch.common.items.aura.ItemJarNode;
public class AuraBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Arcaneresearch.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Arcaneresearch.MODID);
    public static final RegistryObject<Block> AURA_NODE = BLOCKS.register("aura_node", BlockAuraNode::new);
    public static final RegistryObject<Block> JAR_NODE = BLOCKS.register("jar_node", BlockJarNode::new);
    public static final RegistryObject<Block> STABILIZER = BLOCKS.register("node_stabilizer",
            () -> new BlockStabilizer(false));
    public static final RegistryObject<Block> STABILIZER_ADVANCED =
            BLOCKS.register("node_stabilizer_advanced", () -> new BlockStabilizer(true));
    public static final RegistryObject<Block> TRANSDUCER = BLOCKS.register("node_transducer",
            BlockTransducer::new);
    public static final RegistryObject<Block> ENERGIZED_NODE = BLOCKS.register("energized_node",
            BlockEnergizedNode::new);
    public static final RegistryObject<Block> AURA_TOTEM = BLOCKS.register("aura_totem",
            arcaneresearch.common.blocks.aura.AuraTotemBlock::new);
    public static final RegistryObject<Block> OBSIDIAN_TOTEM = BLOCKS.register("obsidian_totem",
            BlockObsidianTotem::new);
    public static final RegistryObject<Block> AIRY = BLOCKS.register("airy",
            BlockAiry::new);
    public static final RegistryObject<Block> ELDRITCH_ALTAR = BLOCKS.register("eldritch_altar",
            arcaneresearch.common.blocks.aura.EldritchAltarBlock::new);
    public static final RegistryObject<Block> NODE_CONVERTER = BLOCKS.register("node_converter",
            arcaneresearch.common.blocks.aura.NodeConverterBlock::new);
    public static final RegistryObject<Block> ELDRITCH_PORTAL = BLOCKS.register("eldritch_portal",
            arcaneresearch.common.blocks.aura.EldritchPortalBlock::new);
    public static final RegistryObject<Block> ELDRITCH_STAIRS = BLOCKS.register("eldritch_stairs",
            () -> new arcaneresearch.common.blocks.aura.EldritchStairsBlock(
                    () -> arcaneresearch.init.ModBlocks.ELDRITCH_STONE_TILE.get().defaultBlockState()));
    public static final RegistryObject<Block> ELDRITCH_TRAP = BLOCKS.register("eldritch_trap",
            arcaneresearch.common.blocks.aura.EldritchTrapBlock::new);
    public static final RegistryObject<Block> ELDRITCH_CRAB = BLOCKS.register("eldritch_crab",
            arcaneresearch.common.blocks.aura.EldritchCrabBlock::new);
    public static final RegistryObject<Block> ELDRITCH_OBELISK = BLOCKS.register("eldritch_obelisk",
            arcaneresearch.common.blocks.aura.EldritchObeliskBlock::new);
    public static final RegistryObject<Block> ELDRITCH_OBELISK_BODY = BLOCKS.register("eldritch_obelisk_body",
            arcaneresearch.common.blocks.aura.EldritchObeliskBodyBlock::new);
    public static final RegistryObject<Block> ELDRITCH_CAP = BLOCKS.register("eldritch_cap",
            arcaneresearch.common.blocks.aura.EldritchCapBlock::new);
    public static final RegistryObject<Item> JAR_NODE_ITEM = BLOCK_ITEMS.register("jar_node",
            () -> new ItemJarNode(JAR_NODE.get(), new Item.Properties()));
    public static final RegistryObject<Item> AURA_NODE_ITEM = BLOCK_ITEMS.register("aura_node",
            () -> new ItemAuraNode(AURA_NODE.get(), new Item.Properties()));
    public static final RegistryObject<Item> STABILIZER_ITEM = BLOCK_ITEMS.register("node_stabilizer",
            () -> new arcaneresearch.common.items.aura.ItemNodeDevice(STABILIZER.get(), new Item.Properties(),
                    arcaneresearch.common.items.aura.ItemNodeDevice.Kind.STABILIZER));
    public static final RegistryObject<Item> STABILIZER_ADVANCED_ITEM =
            BLOCK_ITEMS.register("node_stabilizer_advanced",
                    () -> new arcaneresearch.common.items.aura.ItemNodeDevice(STABILIZER_ADVANCED.get(),
                            new Item.Properties(),
                            arcaneresearch.common.items.aura.ItemNodeDevice.Kind.ADVANCED));
    public static final RegistryObject<Item> TRANSDUCER_ITEM = BLOCK_ITEMS.register("node_transducer",
            () -> new arcaneresearch.common.items.aura.ItemNodeDevice(TRANSDUCER.get(), new Item.Properties(),
                    arcaneresearch.common.items.aura.ItemNodeDevice.Kind.TRANSDUCER));
    public static final RegistryObject<Item> AURA_TOTEM_ITEM = BLOCK_ITEMS.register("aura_totem",
            () -> new BlockItem(AURA_TOTEM.get(), new Item.Properties()));
    public static final RegistryObject<Item> OBSIDIAN_TOTEM_ITEM = BLOCK_ITEMS.register("obsidian_totem",
            () -> new BlockItem(OBSIDIAN_TOTEM.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_ALTAR_ITEM = BLOCK_ITEMS.register("eldritch_altar",
            () -> new BlockItem(ELDRITCH_ALTAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> NODE_CONVERTER_ITEM = BLOCK_ITEMS.register("node_converter",
            () -> new arcaneresearch.common.items.aura.ItemNodeDevice(NODE_CONVERTER.get(), new Item.Properties(),
                    arcaneresearch.common.items.aura.ItemNodeDevice.Kind.CONVERTER));
    public static final RegistryObject<Item> ELDRITCH_PORTAL_ITEM = BLOCK_ITEMS.register("eldritch_portal",
            () -> new arcaneresearch.common.items.aura.EldritchPortalItem(ELDRITCH_PORTAL.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_STAIRS_ITEM = BLOCK_ITEMS.register("eldritch_stairs",
            () -> new BlockItem(ELDRITCH_STAIRS.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_TRAP_ITEM = BLOCK_ITEMS.register("eldritch_trap",
            () -> new BlockItem(ELDRITCH_TRAP.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_CRAB_ITEM = BLOCK_ITEMS.register("eldritch_crab",
            () -> new BlockItem(ELDRITCH_CRAB.get(), new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_CAP_ITEM = BLOCK_ITEMS.register("eldritch_cap",
            () -> new BlockItem(ELDRITCH_CAP.get(), new Item.Properties()));
    private AuraBlocks() {
    }
}
