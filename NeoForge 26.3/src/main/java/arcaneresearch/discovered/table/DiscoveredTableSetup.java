package arcaneresearch.discovered.table;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.RegisterEvent;

import arcaneresearch.Arcaneresearch;

/**
 * Регистрация стола записок без правок вне temp/:
 * блок + BlockItem + BlockEntityType + MenuType (RegisterEvent, MOD-bus).
 *
 * NEOFORGE_PORT: net.minecraftforge.* -> net.neoforged.* (EventBusSubscriber
 * без bus = MOD-bus, RegisterEvent с тем же register(ResourceKey, Identifier,
 * Supplier)); ForgeRegistries.Keys.* -> Registries.*; значения через
 * BuiltInRegistries; ResourceLocation -> Identifier;
 * IForgeMenuType.create -> IMenuTypeExtension.create (та же форма фабрики;
 * буфер теперь RegistryFriendlyByteBuf — меню принимает его, см. Menu).
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID)
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
        Identifier id = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, TABLE_ID);
        Identifier singleId = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, SINGLE_TABLE_ID);
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            // NEOFORGE_PORT (26.3): direct RegisterEvent does not inject the
            // id; set it explicitly (same requirement as DeferredRegister).
            block = new CognitionNoteTableBlock(
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                            .setId(net.minecraft.resources.ResourceKey.create(Registries.BLOCK, id)));
            Block blockForReg = block;
            event.register(Registries.BLOCK, id, () -> blockForReg);
            singleTable = new DiscoveredTableBlock(
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                            .setId(net.minecraft.resources.ResourceKey.create(Registries.BLOCK, singleId)));
            Block singleForReg = singleTable;
            event.register(Registries.BLOCK, singleId, () -> singleForReg);
        } else if (event.getRegistryKey().equals(Registries.ITEM)) {
            // У note_table предмета нет: только двухблочная структура
            // (ставится кликом чернилами по table).
            if (singleTable != null) {
                Block tableForItem = singleTable;
                singleTableItem = new BlockItem(tableForItem, new Item.Properties()
                        .setId(net.minecraft.resources.ResourceKey.create(Registries.ITEM, singleId)));
                Item itemForReg = singleTableItem;
                event.register(Registries.ITEM, singleId, () -> itemForReg);
            }
        } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            BlockEntityType<CognitionNoteTableTile> beType =
                    new BlockEntityType<>(CognitionNoteTableTile::new, block);
            BlockEntityType<CognitionNoteTableTile> beForReg = beType;
            event.register(Registries.BLOCK_ENTITY_TYPE, id, () -> beForReg);
            entityType = beType;
        } else if (event.getRegistryKey().equals(Registries.MENU)) {
            MenuType<CognitionNoteTableMenu> menu = IMenuTypeExtension.create(
                    (containerId, inventory, buf) -> new CognitionNoteTableMenu(
                            containerId, inventory, buf.readBlockPos()));
            MenuType<CognitionNoteTableMenu> menuForReg = menu;
            event.register(Registries.MENU, id, () -> menuForReg);
            menuType = menu;
        }
    }
}
