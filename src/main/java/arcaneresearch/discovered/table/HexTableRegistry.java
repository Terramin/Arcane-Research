package arcaneresearch.discovered.table;
import arcaneresearch.helper.MenuHelper;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import arcaneresearch.Arcaneresearch;
public class HexTableRegistry {
    public static MenuType<HexResearchTableMenu> MENU;
    public static Block NOTE_BLOCK;
    public static BlockEntityType<HexNoteTableTile> TILE;
    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Reg {
        @SubscribeEvent
        public static void onRegister(RegisterEvent event) {
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.MENU_TYPES)) {
                MENU = MenuHelper.createType(HexResearchTableMenu::new);
                final MenuType<HexResearchTableMenu> type = MENU;
                event.register(ForgeRegistries.Keys.MENU_TYPES,
                        helper -> helper.register("hex_note_table", type));
            }
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS)) {
                NOTE_BLOCK = new HexNoteTableBlock();
                final Block note = NOTE_BLOCK;
                event.register(ForgeRegistries.Keys.BLOCKS,
                        helper -> helper.register("hex_note_table", note));
            }
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES)) {
                if (NOTE_BLOCK != null) {
                    final Block note = NOTE_BLOCK;
                    TILE = BlockEntityType.Builder.of(HexNoteTableTile::new, note).build(null);
                    final BlockEntityType<HexNoteTableTile> tile = TILE;
                    event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES,
                            helper -> helper.register("hex_note_table", tile));
                }
            }
        }
    }
    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientReg {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                if (MENU != null) {
                    net.minecraft.client.gui.screens.MenuScreens.register(MENU, arcaneresearch.discovered.client.HexResearchTableScreen::new);
                }
                if (NOTE_BLOCK != null) {
                    net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(NOTE_BLOCK, net.minecraft.client.renderer.RenderType.cutout());
                }
            });
        }
        @SubscribeEvent
        public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            try {
                event.registerBlockEntityRenderer(
                        arcaneresearch.discovered.table.DiscoveredTableSetup.getEntityType(),
                        arcaneresearch.discovered.client.CognitionNoteTableRenderer::new);
            } catch (Exception ignored) {
            }
            try {
                if (TILE != null) {
                    event.registerBlockEntityRenderer(TILE, arcaneresearch.discovered.client.HexNoteTableRenderer::new);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
