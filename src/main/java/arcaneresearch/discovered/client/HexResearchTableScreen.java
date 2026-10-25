package arcaneresearch.discovered.client;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.discovered.table.HexResearchGame;
import arcaneresearch.discovered.table.HexResearchNoteData;
import arcaneresearch.discovered.table.HexResearchTableMenu;
import arcaneresearch.discovered.table.HexTableNetwork;
import arcaneresearch.discovered.table.HexUtils;
import arcaneresearch.discovered.table.MsgEnsureHex;
@OnlyIn(Dist.CLIENT)
public class HexResearchTableScreen extends AbstractContainerScreen<HexResearchTableMenu> {
    private static final int GUI_W = 255;
    private static final int GUI_H = 255;
    final HexResearchTableMenu menu;
    HexResearchNoteData note;
    int left;
    int top;
    int page;
    Aspect select1;
    Aspect select2;
    long lastCombineClick;
    boolean dragging;
    Aspect dragged;
    double dragX;
    double dragY;
    boolean hexRequested;
    ItemStack lastSlotCopy = ItemStack.EMPTY;
    private List<Aspect> poolCache;
    private final HexTableDraw draw;
    private final HexTableInput input;
    public HexResearchTableScreen(HexResearchTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.menu = menu;
        this.imageWidth = GUI_W;
        this.imageHeight = GUI_H;
        this.draw = new HexTableDraw(this);
        this.input = new HexTableInput(this);
        refreshNote();
    }
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
        left = (width - GUI_W) / 2;
        top = (height - GUI_H) / 2;
        try {
            arcaneresearch.discovered.scan.ScanNetwork.CHANNEL
                    .sendToServer(new arcaneresearch.discovered.scan.MsgWalletRequest());
        } catch (Exception ignored) {
        }
    }
    Player player() {
        return minecraft != null ? minecraft.player : null;
    }
    net.minecraft.client.gui.Font font() {
        return font;
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
    private List<Aspect> poolOrder() {
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
    int shownAmount(Aspect aspect) {
        return arcaneresearch.discovered.scan.ScanWallet.viewAmount(aspect);
    }
    int shownBonus(Aspect aspect) {
        return arcaneresearch.discovered.scan.ScanWallet.viewBonus(aspect);
    }
    boolean hasTools() {
        try {
            ItemStack tools = menu.getSlot(0).getItem();
            return HexResearchTableMenu.isInkStack(tools)
                    && tools.getDamageValue() < tools.getMaxDamage();
        } catch (Exception e) {
            return false;
        }
    }
    public void applySync(CompoundTag tag) {
        if (tag == null) {
            return;
        }
        if (note != null) {
            note.readFromNBT(tag);
        } else {
            HexResearchNoteData created = new HexResearchNoteData();
            created.readFromNBT(tag);
            if (created.key != null && !created.key.isEmpty()) {
                note = created;
            }
        }
        hexRequested = false;
    }
    private void requestHexField() {
        if (hexRequested) {
            return;
        }
        try {
            ItemStack stack = menu.getSlot(1).getItem();
            if (HexResearchTableMenu.isNoteStack(stack)
                    && HexResearchNoteData.getData(stack) == null) {
                HexTableNetwork.CHANNEL.sendToServer(new MsgEnsureHex());
                hexRequested = true;
            }
        } catch (Exception ignored) {
        }
    }
    void sfx(SoundEvent event, float volume) {
        sfx(event, volume, 1.0F);
    }
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
    HexUtils.Hex sheetHex(double mx, double my) {
        HexUtils.Hex hex = new HexUtils.Pixel(mx - (left + HexTableDraw.CX),
                my - (top + HexTableDraw.CY)).toHex(HexTableDraw.HEX);
        if (note != null && note.hexes.containsKey(hex.toString())) {
            return hex;
        }
        return null;
    }
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        return input.mouseClicked(mx, my, button) || super.mouseClicked(mx, my, button);
    }
    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        return input.mouseDragged(mx, my, button, dx, dy) || super.mouseDragged(mx, my, button, dx, dy);
    }
    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        return input.mouseReleased(mx, my, button) || super.mouseReleased(mx, my, button);
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mx, int my) {
    }
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float partialTick) {
        renderBackground(graphics);
        draw.drawTableBackground(graphics);
        poolCache = poolOrder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        draw.renderMenuSlots(graphics, mx, my);
        refreshNote();
        try {
            ItemStack slotNow = menu.getSlot(1).getItem();
            if (!ItemStack.matches(slotNow, lastSlotCopy)) {
                lastSlotCopy = slotNow.copy();
                hexRequested = false;
            }
        } catch (Exception ignored) {
        }
        draw.renderCarried(graphics, mx, my);
        if (note == null) {
            requestHexField();
        } else {
            hexRequested = false;
        }
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
        if (note != null) {
            draw.drawSheet(graphics, mx, my);
        }
        draw.renderDragOrb(graphics);
        draw.drawHover(graphics, mx, my);
        draw.drawInkWarning(graphics);
        poolCache = null;
    }
}
