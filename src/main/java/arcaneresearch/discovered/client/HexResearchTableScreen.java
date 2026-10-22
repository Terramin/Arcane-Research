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
      
    boolean hexRequested;
      
    ItemStack lastSlotCopy = ItemStack.EMPTY;

     



 
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
        
        try {
            PacketHandler.sendToServer(new arcaneresearch.discovered.scan.MsgWalletRequest());
        } catch (Exception ignored) {
        }
    }

    Player player() {
        return minecraft != null ? minecraft.player : null;
    }

      
    net.minecraft.client.gui.Font font() {
        return font;
    }

     
 
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
        HexUtils.Hex hex = new HexUtils.Pixel(mx - (leftPos + HexTableDraw.CX),
                my - (topPos + HexTableDraw.CY)).toHex(HexTableDraw.HEX);
        if (note != null && note.hexes.containsKey(hex.toString())) {
            return hex;
        }
        return null;
    }

    

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

    

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partialTick) {
        
        
        
        super.extractBackground(graphics, mx, my, partialTick);
        draw.drawTableBackground(graphics);
        
        poolCache = poolOrder();
        refreshNote();
        
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

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mx, int my) {
    }
}
