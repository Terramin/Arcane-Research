package arcaneresearch.discovered.table;

import arcaneresearch.helper.IDsHelper;
import arcaneresearch.helper.MenuHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import arcaneresearch.Arcaneresearch;

@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DiscoveredTableSetup {
    public static final String TABLE_ID = "cognition_note_table";
    public static final String SINGLE_TABLE_ID = "discovered_table";
    private static Block block;
    private static Block singleTable;
    private static Item singleTableItem;
    private static BlockEntityType<CognitionNoteTableTile> entityType;
    private static MenuType<CognitionNoteTableMenu> menuType;

    public static Block getBlock() {
        return block;
    }

    public static Block getSingleTable() {
        return singleTable;
    }

    public static Item getSingleTableItem() {
        return singleTableItem;
    }

    public static BlockEntityType<CognitionNoteTableTile> getEntityType() {
        return entityType;
    }

    public static MenuType<CognitionNoteTableMenu> getMenuType() {
        return menuType;
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRegister(RegisterEvent event) {
        ResourceLocation id = IDsHelper.mod(TABLE_ID);
        ResourceLocation singleId = IDsHelper.mod(SINGLE_TABLE_ID);
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS)) {
            event.register(ForgeRegistries.Keys.BLOCKS, id, CognitionNoteTableBlock::new);
            block = ForgeRegistries.BLOCKS.getValue(id);
            event.register(ForgeRegistries.Keys.BLOCKS, singleId, DiscoveredTableBlock::new);
            singleTable = ForgeRegistries.BLOCKS.getValue(singleId);
        } else if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) {
            event.register(ForgeRegistries.Keys.ITEMS, singleId,
                    () -> new BlockItem(singleTable, new Item.Properties()));
            singleTableItem = ForgeRegistries.ITEMS.getValue(singleId);
        } else if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES)) {
            BlockEntityType<CognitionNoteTableTile> beType =
                    BlockEntityType.Builder.<CognitionNoteTableTile>of(CognitionNoteTableTile::new, block).build(null);
            event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, id, () -> beType);
            entityType = (BlockEntityType<CognitionNoteTableTile>) (BlockEntityType<?>)
                    ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(id);
        } else if (event.getRegistryKey().equals(ForgeRegistries.Keys.MENU_TYPES)) {
            MenuType<CognitionNoteTableMenu> menu = MenuHelper.createType(
                    (containerId, inventory, buf) -> new CognitionNoteTableMenu(
                            containerId, inventory, buf.readBlockPos()));
            event.register(ForgeRegistries.Keys.MENU_TYPES, id, () -> menu);
            menuType = (MenuType<CognitionNoteTableMenu>) (MenuType<?>)
                    ForgeRegistries.MENU_TYPES.getValue(id);
        }
    }
}
