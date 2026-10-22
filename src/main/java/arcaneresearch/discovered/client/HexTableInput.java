package arcaneresearch.discovered.client;

import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;
import arcaneresearch.discovered.table.HexUtils;
import arcaneresearch.discovered.table.MsgCombine;
import arcaneresearch.discovered.table.MsgDupe;
import arcaneresearch.discovered.table.MsgPlaceAspect;
import arcaneresearch.init.ModSounds;

 







 
@OnlyIn(Dist.CLIENT)
public class HexTableInput {

    private final HexResearchTableScreen s;

    public HexTableInput(HexResearchTableScreen screen) {
        this.s = screen;
    }

      
    Aspect poolHit(double mx, double my) {
        
        if (!s.hasTools()) {
            return null;
        }
        List<Aspect> order = s.poolView();
        int drawn = 0;
        int shown = 0;
        for (Aspect aspect : order) {
            if (drawn < s.page * 5) {
                drawn++;
                continue;
            }
            if (shown >= 25) {
                break;
            }
            int xx = shown / 5 * 16;
            int yy = shown % 5 * 16;
            if (s.inZone(mx, my, s.guiLeft() + HexTableDraw.POOL_X + xx,
                    s.guiTop() + HexTableDraw.POOL_Y + yy, 16, 16)) {
                if (s.shownAmount(aspect) + s.shownBonus(aspect) > 0) {
                    return aspect;
                }
                return null;
            }
            drawn++;
            shown++;
        }
        return null;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        s.refreshNote();
        
        if (button != com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        
        if (s.inZone(mx, my, s.guiLeft() + 35, s.guiTop() + 139, 32, 16)) {
            if (s.select1 != null && s.select2 != null
                    && System.nanoTime() - s.lastCombineClick > 200000000L) {
                s.lastCombineClick = System.nanoTime();
                s.sfx(ModSounds.CLACK.get(), 0.4F);
                s.sfx(ModSounds.HH_ON.get(), 0.3F);
                doCombine();
            }
            return true;
        }
        
        if (s.note != null && s.note.complete
                && s.inZone(mx, my, s.guiLeft() + 37, s.guiTop() + 5, 24, 24)) {
            s.sfx(ModSounds.CLACK.get(), 0.4F);
            PacketHandler.sendToServer(new MsgDupe());
            return true;
        }
        
        if (s.inZone(mx, my, s.guiLeft() + 27, s.guiTop() + 121, 24, 8)) {
            if (s.page > 0) {
                s.page--;
                s.sfx(ModSounds.KEY.get(), 0.3F);
            }
            return true;
        }
        if (s.inZone(mx, my, s.guiLeft() + 51, s.guiTop() + 121, 24, 8)) {
            if (s.page < s.lastPage(s.poolView().size())) {
                s.page++;
                s.sfx(ModSounds.KEY.get(), 0.3F);
            }
            return true;
        }
        
        if (s.select1 != null && s.inZone(mx, my, s.guiLeft() + 11, s.guiTop() + 137, 16, 16)) {
            s.select1 = null;
            s.sfx(ModSounds.HH_OFF.get(), 0.2F, s.jitterPitch());
            return true;
        }
        if (s.select2 != null && s.inZone(mx, my, s.guiLeft() + 71, s.guiTop() + 137, 16, 16)) {
            s.select2 = null;
            s.sfx(ModSounds.HH_OFF.get(), 0.2F, s.jitterPitch());
            return true;
        }
        
        Aspect poolAspect = poolHit(mx, my);
        if (poolAspect != null) {
            s.dragging = true;
            s.dragged = poolAspect;
            s.dragX = mx;
            s.dragY = my;
            s.sfx(ModSounds.HH_OFF.get(), 0.2F, s.jitterPitch());
            return true;
        }
        
        HexUtils.Hex hex = s.sheetHex(mx, my);
        if (hex != null) {
            HexEntry entry = s.note.hexEntries.get(hex.toString());
            if (entry != null && entry.type == 2) {
                s.sfx(ModSounds.HH_ON.get(), 0.3F);
                s.sfx(ModSounds.ERASE.get(), 0.2F, s.jitterPitch());
                PacketHandler.sendToServer(new MsgPlaceAspect(hex.q, hex.r, ""));
                return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (s.dragging) {
            s.dragX = mx;
            s.dragY = my;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mx, double my, int button) {
        if (!s.dragging || button != com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }
        s.dragging = false;
        Aspect drop = s.dragged;
        s.dragged = null;
        if (drop == null) {
            return true;
        }
        
        HexUtils.Hex hex = s.sheetHex(mx, my);
        if (hex != null) {
            s.sfx(ModSounds.HH_ON.get(), 0.3F);
            s.sfx(ModSounds.WRITE.get(), 0.2F);
            PacketHandler.sendToServer(new MsgPlaceAspect(hex.q, hex.r, drop.getTag()));
            return true;
        }
        
        if (s.inZone(mx, my, s.guiLeft() + 4, s.guiTop() + 130, 32, 32)) {
            s.select1 = drop;
            return true;
        }
        if (s.inZone(mx, my, s.guiLeft() + 63, s.guiTop() + 130, 32, 32)) {
            s.select2 = drop;
            return true;
        }
        
        Aspect same = poolHit(mx, my);
        if (same != null && same == drop) {
            if (s.select1 == null) {
                s.select1 = drop;
            } else if (s.select2 == null) {
                s.select2 = drop;
            }
        }
        return true;
    }

    private void doCombine() {
        if (s.select1 != null && s.select2 != null) {
            PacketHandler.sendToServer(
                    new MsgCombine(s.select1.getTag(), s.select2.getTag()));
        }
        s.select1 = null;
        s.select2 = null;
    }
}
