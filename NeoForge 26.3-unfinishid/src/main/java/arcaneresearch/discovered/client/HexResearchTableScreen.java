package arcaneresearch.discovered.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.table.HexResearchGame;
import arcaneresearch.discovered.table.HexResearchNoteData;
import arcaneresearch.discovered.table.HexResearchTableMenu;
import arcaneresearch.discovered.table.HexUtils;

/**
 * Порт {@code thaumcraft.client.gui.GuiResearchTable} (TC 4.2.3.5) на
 * ванильный экран.
 *
 * Хаб: состояние + меню + синк. Раскладка 1-в-1 (логические 255x255):
 * пул аспектов слева, пергамент 150x150, слоты комбинирования + кнопка,
 * пагинация. Знаки координат из декомпила местами ненадёжны, поэтому
 * хит-зоны и отрисовка заданы из одних констант — консистентность
 * гарантирована. Ввод — {@link HexTableInput}, отрисовка — {@link HexTableDraw}.
 *
 * Отличия от оригинала (серверная привязка к реальному тайлу —
 * вопрос отдельно):
 * <ul>
 *   <li>Контейнер — собственный ({@link HexResearchTableMenu} на
 *   {@code SimpleContainer}), дупликация и сдача записки отсутствуют.</li>
 *   <li>Пул — из записки ({@link HexResearchGame}); стирание всегда
 *   возвращает аспект; чернила тратятся при наличии, иначе ход свободен.</li>
 *   <li>Линии связей — повёрнутыми заливками (без GL-линий); орбы —
 *   заливкой (нет ParticleEngine).</li>
 * </ul>
 *
 * NEOFORGE_PORT (26.3 render-state): GuiGraphics -&gt; GuiGraphicsExtractor;
 * render()/renderBg -&gt; extractBackground (слоты/переносимый предмет рисует
 * фреймворк — ручной renderMenuSlots/renderCarried удалён, иначе двойной
 * рендер); тултипы — deferred setComponentTooltipForNextFrame;
 * mouseClicked(MouseButtonEvent, boolean) / mouseDragged(MouseButtonEvent,
 * double, double) / mouseReleased(MouseButtonEvent); left/top -&gt;
 * leftPos/topPos; размеры в конструктор; RenderSystem-бленд удалён.
 */
@OnlyIn(Dist.CLIENT)
public class HexResearchTableScreen extends AbstractContainerScreen<HexResearchTableMenu> {

    private static final int GUI_W = 255;
    private static final int GUI_H = 255;

    final HexResearchTableMenu menu;
    HexResearchNoteData note;

    int page;
    Aspect select1;
    Aspect select2;
    long lastCombineClick;
    boolean dragging;
    Aspect dragged;
    double dragX;
    double dragY;
    /** Запрос генерации hex-поля уже отправлен (ждём MsgSyncNote). */
    boolean hexRequested;
    /** Копия слота записки для сброса флага при смене предмета. */
    ItemStack lastSlotCopy = ItemStack.EMPTY;

    /**
     * Кэш порядка пула на кадр: построение (обход + сортировка всех аспектов)
     * дорого для 4-6 вызовов за кадр — считается один раз в extractBackground.
     * Вне кадра (клики) — свежий расчёт.
     */
    private List<Aspect> poolCache;

    private final HexTableDraw draw;
    private final HexTableInput input;

    public HexResearchTableScreen(HexResearchTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GUI_W, GUI_H);
        this.menu = menu;
        this.draw = new HexTableDraw(this);
        this.input = new HexTableInput(this);
        refreshNote();
    }

    /** Записка живёт в слоте 1 меню (синхронизируется ваниллой). */
    void refreshNote() {
        try {
            this.note = HexResearchNoteData.getData(menu.getSlot(1).getItem());
        } catch (Exception ignored) {
            this.note = null;
        }
    }

    @Override
    protected void init() {
        super.init();
        // Снимок хранилища для отображения пула (кошелёк + бонус тайла).
        try {
            PacketHandler.sendToServer(new arcaneresearch.discovered.scan.MsgWalletRequest());
        } catch (Exception ignored) {
        }
    }

    Player player() {
        return minecraft != null ? minecraft.player : null;
    }

    /** Доступ к protected-полям Screen из хелперов того же пакета. */
    net.minecraft.client.gui.Font font() {
        return font;
    }

    /** NEOFORGE_PORT: leftPos/topPos protected в AbstractContainerScreen и недоступны
     * из хелперов (не наследники); геттеры для HexTableDraw/HexTableInput (те же значения). */
    public int guiLeft() {
        return leftPos;
    }

    public int guiTop() {
        return topPos;
    }

    net.minecraft.client.Minecraft minecraft() {
        return minecraft;
    }

    java.util.List<net.minecraft.world.inventory.Slot> slots() {
        return menu.slots;
    }

    boolean known(Aspect aspect) {
        return HexResearchGame.isKnown(player(), aspect);
    }

    /** Известные игроку аспекты (источник пула — кошелёк/бонус). */
    private List<Aspect> poolOrder() {
        // Сортировка — проектным AspectList.getAspectsSortedByName
        // (тот же пузырёк по getTag, что getAspectsSorted в TC 4.2.3.5).
        arcaneresearch.api.aspects.AspectList known = new arcaneresearch.api.aspects.AspectList();
        for (Aspect aspect : Aspect.aspects.values()) {
            if (aspect != null && known(aspect)) {
                known.add(aspect, 1);
            }
        }
        return new ArrayList<>(java.util.Arrays.asList(known.getAspectsSortedByName()));
    }

    List<Aspect> poolView() {
        if (poolCache != null) {
            return poolCache;
        }
        return poolOrder();
    }

    int lastPage(int count) {
        return Math.max(0, (count - 20) / 5);
    }

    /** Количество аспекта для отображения: зеркало хранилища (кошелёк). */
    int shownAmount(Aspect aspect) {
        return arcaneresearch.discovered.scan.ScanWallet.viewAmount(aspect);
    }

    /** Бонус аспекта с тайла открытого стола. */
    int shownBonus(Aspect aspect) {
        return arcaneresearch.discovered.scan.ScanWallet.viewBonus(aspect);
    }

    /** Годных инструментов в слоте (иначе пул скрыт, ходы запрещены). */
    boolean hasTools() {
        try {
            ItemStack tools = menu.getSlot(0).getItem();
            return HexResearchTableMenu.isInkStack(tools)
                    && tools.getDamageValue() < tools.getMaxDamage();
        } catch (Exception e) {
            return false;
        }
    }

    /** Серверное состояние ({@code MsgSyncNote}): применяется поверх локального. */
    public void applySync(CompoundTag tag) {
        if (tag == null) {
            return;
        }
        if (note != null) {
            note.readFromNBT(tag);
        } else {
            // Записки ещё не было (генерация с нуля): собираем из пакета,
            // иначе состояние теряется и создание выглядит сломанным.
            HexResearchNoteData created = new HexResearchNoteData();
            created.readFromNBT(tag);
            if (created.key != null && !created.key.isEmpty()) {
                note = created;
            }
        }
        hexRequested = false;
    }

    /**
     * Просьба серверу сгенерировать hex-поле для проектной записки в слоте
     * (ключ — из её DiscoveredNoteData). Один запрос до ответа.
     */
    private void requestHexField() {
        if (hexRequested) {
            return;
        }
        try {
            ItemStack stack = menu.getSlot(1).getItem();
            if (HexResearchTableMenu.isNoteStack(stack)
                    && HexResearchNoteData.getData(stack) == null) {
                PacketHandler.sendToServer(
                        new arcaneresearch.discovered.table.MsgEnsureHex());
                hexRequested = true;
            }
        } catch (Exception ignored) {
        }
    }

    void sfx(SoundEvent event, float volume) {
        sfx(event, volume, 1.0F);
    }

    /** Паритет pitch-джиттера оригинала (hhoff/erase: 1.0 + rand * 0.1). */
    void sfx(SoundEvent event, float volume, float pitch) {
        Player player = player();
        if (player != null && player.level() != null && event != null) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                    event, SoundSource.PLAYERS, volume, pitch, false);
        }
    }

    float jitterPitch() {
        try {
            return 1.0F + player().getRandom().nextFloat() * 0.1F;
        } catch (Exception e) {
            return 1.0F;
        }
    }

    boolean inZone(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Пиксель -&gt; гекс поля (координаты относительно центра листа). */
    HexUtils.Hex sheetHex(double mx, double my) {
        HexUtils.Hex hex = new HexUtils.Pixel(mx - (leftPos + HexTableDraw.CX),
                my - (topPos + HexTableDraw.CY)).toHex(HexTableDraw.HEX);
        if (note != null && note.hexes.containsKey(hex.toString())) {
            return hex;
        }
        return null;
    }

    // ---- input: делегаты в HexTableInput (super — для ванильных слотов) ----

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return input.mouseClicked(event.x(), event.y(), event.button())
                || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return input.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)
                || super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return input.mouseReleased(event.x(), event.y(), event.button())
                || super.mouseReleased(event);
    }

    // ---- render ----

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partialTick) {
        super.extractBackground(graphics, mx, my, partialTick);
        // 1.20.1 Screen.renderBackground dim (fillGradient)
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        draw.drawTableBackground(graphics);
        // Кэш порядка пула на кадр (см. poolView).
        poolCache = poolOrder();
        refreshNote();
        // Слот сменился — разрешаем новый запрос генерации.
        try {
            ItemStack slotNow = menu.getSlot(1).getItem();
            if (!ItemStack.matches(slotNow, lastSlotCopy)) {
                lastSlotCopy = slotNow.copy();
                hexRequested = false;
            }
        } catch (Exception ignored) {
        }
        if (note == null) {
            requestHexField();
        } else {
            hexRequested = false;
        }
        // Авто-сброс селектов с пустым хранилищем (паритет drawAspects),
        // а также при убранных инструментах.
        if (!hasTools()) {
            select1 = null;
            select2 = null;
        }
        if (select1 != null && shownAmount(select1) + shownBonus(select1) <= 0) {
            select1 = null;
        }
        if (select2 != null && shownAmount(select2) + shownBonus(select2) <= 0) {
            select2 = null;
        }

        draw.drawDupe(graphics);
        draw.drawPool(graphics, mx, my);
        draw.drawCombineUi(graphics);
        // Без записки — только пул и соединение, лист скрыт.
        if (note != null) {
            draw.drawSheet(graphics, mx, my);
        }
        draw.renderDragOrb(graphics);

        draw.drawHover(graphics, mx, my);
        draw.drawInkWarning(graphics);
        // Кэш живёт только кадр (иначе клики увидят протухший порядок).
        poolCache = null;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mx, int my) {
    }
}
