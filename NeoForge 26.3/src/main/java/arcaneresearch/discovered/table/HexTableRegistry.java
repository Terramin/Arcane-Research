package arcaneresearch.discovered.table;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.RegisterEvent;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.client.HexNoteTableRenderer;
import arcaneresearch.discovered.client.HexResearchTableScreen;

/**
 * Регистрации hex-стола (стадия 2): пара MAIN/SIDE, тайл, тип меню,
 * сборки через salis mundus. Всё через события, файлы проекта
 * не трогаются. Имена: {@code hex_note_table} (пара, без предмета).
 *
 * NEOFORGE_PORT (как DiscoveredTableSetup): RegisterEvent с MOD-bus,
 * Registries.*, Identifier.fromNamespaceAndPath, Properties с setId,
 * BlockEntityType — конструктором, MenuType — IMenuTypeExtension.create
 * (буфер RegistryFriendlyByteBuf — меню принимает его);
 * MenuScreens.register -&gt; RegisterMenuScreensEvent;
 * ItemBlockRenderTypes.setRenderLayer удалён в 26.x -&gt; render_type
 * в JSON-моделях (hex_note_table.json уже с minecraft:cutout);
 * BER — EntityRenderersEvent.RegisterRenderers (стол 1 + стол 2).
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class HexTableRegistry {

    public static final String NOTE_TABLE_ID = "hex_note_table";

    public static MenuType<HexResearchTableMenu> MENU;
    public static Block NOTE_BLOCK;
    public static BlockEntityType<HexNoteTableTile> TILE;

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRegister(RegisterEvent event) {
        Identifier id = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, NOTE_TABLE_ID);
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            NOTE_BLOCK = new HexNoteTableBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, id)));
            Block noteForReg = NOTE_BLOCK;
            event.register(Registries.BLOCK, id, () -> noteForReg);
        } else if (event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE)) {
            if (NOTE_BLOCK != null) {
                BlockEntityType<HexNoteTableTile> beType =
                        new BlockEntityType<>(HexNoteTableTile::new, NOTE_BLOCK);
                BlockEntityType<HexNoteTableTile> beForReg = beType;
                event.register(Registries.BLOCK_ENTITY_TYPE, id, () -> beForReg);
                TILE = beType;
            }
        } else if (event.getRegistryKey().equals(Registries.MENU)) {
            MenuType<HexResearchTableMenu> menu = IMenuTypeExtension.create(
                    (containerId, inventory, buf) -> new HexResearchTableMenu(
                            containerId, inventory, buf));
            MenuType<HexResearchTableMenu> menuForReg = menu;
            event.register(Registries.MENU, id, () -> menuForReg);
            MENU = menu;
        }
    }

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class ClientReg {
        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            if (MENU != null) {
                event.register(MENU, HexResearchTableScreen::new);
            }
        }

        /** Объёмные перья столов (BER): стол 1 всегда, стол 2 при инструментах. */
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            try {
                event.registerBlockEntityRenderer(
                        arcaneresearch.discovered.table.DiscoveredTableSetup.getEntityType(),
                        arcaneresearch.discovered.client.CognitionNoteTableRenderer::new);
            } catch (Exception ignored) {
            }
            try {
                if (TILE != null) {
                    event.registerBlockEntityRenderer(TILE, HexNoteTableRenderer::new);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
