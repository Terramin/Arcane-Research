package arcaneresearch.client.lib.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import arcaneresearch.Arcaneresearch;

import java.util.HashMap;
import java.util.Map;

// NEOFORGE_PORT: 1:1 logic from Forge client/lib/events/RunicShieldHud.java.
// REQUIRED changes: RenderGuiOverlayEvent.Post (Forge overlay event, gone) ->
// GuiLayer registered above PLAYER_HEALTH via RegisterGuiLayersEvent (same
// draw-after-hearts ordering); raw RenderSystem blend state gone -> pipeline
// blits only (GUI_TEXTURED; gold pulse via the tinted-blit color arg, same as
// HudHandler); GuiGraphics -> GuiGraphicsExtractor (same blit/pose calls);
// Identifier(MODID, path) -> Identifier.fromNamespaceAndPath; survival check
// via vanilla Gui#shouldDrawSurvivalElements (same method ForgeGui overrode).
/**
 * Сердца рунного щита: одна пипка за единицу заряда, ровно в ячейках
 * сердец ваниллы (x = sw/2-91 + col*8, y = (sh-39) - row*rowHeight —
 * формулы из декомпила ForgeGui.renderHealth). Ряды и шаг сжатия
 * повторяют ваниллу (max HP атрибутов + абсорб); оверлей кладётся только
 * на существующие ячейки сердец, остаток заряда — рядами строго над
 * верхней строкой сердец. Спрайты — лист TC4 (подложка + руна cell*16,96
 * в половинном масштабе, золото с пульсацией).
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class RunicShieldHud {

    private static final Identifier SHEET = Identifier.fromNamespaceAndPath("arcaneresearch",
            "textures/misc/particles_1710.png");

    private static final Map<Integer, Integer> charge = new HashMap<>();
    private static final Map<Integer, Integer> total = new HashMap<>();

    public static void applySync(int playerId, int current, int max) {
        if (max <= 0) {
            charge.remove(playerId);
            total.remove(playerId);
            return;
        }
        charge.put(playerId, Math.min(max, Math.max(0, current)));
        total.put(playerId, max);
    }

    public static final GuiLayer RUNIC_SHIELD = (graphics, deltaTracker) -> {
        renderOverlay(graphics);
    };

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "runic_shield"), RUNIC_SHIELD);
    }

    /**
     * Рисуем сразу после ванильных сердец: слой идёт над PLAYER_HEALTH, плюс
     * то же условие видимости сердец ({@code shouldDrawSurvivalElements})
     * вместо проверки режима игры.
     */
    private static void renderOverlay(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        // Факт наличия сердец, а не режим игры: живы и ванилла рисует HP.
        // NEOFORGE_PORT: Gui#shouldDrawSurvivalElements удалён в 26.x (Gui/Hud реворк);
        // паритет ForgeGui.shouldDrawSurvivalElements = !hideGui && gameMode.canHurtPlayer().
        // hideGui покрыт слоем (PLAYER_HEALTH не рисуется при hideGui), здесь — canHurtPlayer.
        boolean survival = false;
        try {
            survival = mc.gameMode != null && mc.gameMode.canHurtPlayer();
        } catch (Exception ignored) {
        }
        if (player == null || !player.isAlive() || mc.gui.hud.isHidden() || !survival) {
            return;
        }
        Integer id = player.getId();
        int max = total.getOrDefault(id, 0);
        int current = charge.getOrDefault(id, 0);
        if (max <= 0 || current <= 0) {
            return;
        }
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        // Сетка сердец ваниллы из декомпила ForgeGui.renderHealth:
        // x = sw/2-91 + col*8, y = (sh-leftHeight) - row*rowHeight;
        // rows = ceil((max(attrHP, HP) + абсорб) / 2 / 10), rowHeight = max(10-(rows-2),3).
        // leftHeight на момент сердец = 39 (здоровье рисуется первым).
        // Пипка кладётся ровно в ячейку сердца — отрисовка НА сердцах, как в TC4.
        // Дословно ванилла: rows = ceil((max(HP_атрибут, HP) + ceil(абсорб)) / 2 / 10).
        // (Ванилла берёт ещё и мигающий lastHealth при уроне — секундный кейс, пропуск.)
        // Оверлей — только на существующие ячейки сердец (max HP + абсорб):
        // верхняя строка ваниллы бывает неполной, и пипки в её пустых колонках
        // висели бы в пустоте. Остаток заряда идёт новыми рядами строго над
        // верхней строкой сердец, а не в пустые колонки.
        float base = Math.max(player.getMaxHealth(), player.getHealth())
                + (float) Math.ceil(player.getAbsorptionAmount());
        int heartRows = Math.max(1, (int) Math.ceil(base / 2.0F / 10.0F));
        int rowHeight = Math.max(10 - (heartRows - 2), 3);
        int heartCells = Math.max(0, (int) Math.ceil(base / 2.0F));
        int overlaid = Math.min(current, heartCells);
        int extraRowStart = heartRows * 10;
        int baseX = sw / 2 - 91;
        int baseY = sh - 39;

        // Глубина как у ванильных сердец: тест включён (движок использует
        // LEQUAL — перекрытие в тех же ячейках работает), поверх левых GUI.
        for (int a = 0; a < current; a++) {
            int cellIndex = a < overlaid ? a : extraRowStart + (a - overlaid);
            int col = cellIndex % 10;
            int row = cellIndex / 10;
            int px = baseX + col * 8;
            int py = baseY - row * rowHeight;
            int cell = a % 10;
            // Белая подложка 9x9, как в TC4.
            graphics.blit(RenderPipelines.GUI_TEXTURED, SHEET, px, py, 160, 16, 9, 9, 256, 256);
            // Руна 16x16 в масштабе 0.5 (экранные 8x8 на той же клетке).
            // Позиция уже в translate — рисуем в (0,0), иначе двойной сдвиг.
            graphics.pose().pushMatrix();
            graphics.pose().translate(px, py);
            graphics.pose().scale(0.5F, 0.5F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, SHEET, 0, 0, cell * 16, 96, 16, 16,
                    16, 16, 256, 256,
                    ARGB.colorFromFloat(Mth.sin(player.tickCount / 4.0F + cell) * 0.4F + 0.6F,
                            1.0F, 0.75F, 0.24F));
            graphics.pose().popMatrix();
        }
    }
}
