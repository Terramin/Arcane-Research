package arcaneresearch.client.lib.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; // NEOFORGE_PORT: was Mod
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.items.casters.ItemFocusPouch;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketFocusChangeToServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class FocusRadialHandler {

    private static final Identifier R1 =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/radial.png");
    private static final Identifier R2 =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/radial2.png");
    private static final TreeMap<String, Integer> foci = new TreeMap<>();
    private static final HashMap<String, ItemStack> fociItem = new HashMap<>();
    private static final HashMap<String, Boolean> fociHover = new HashMap<>();
    private static final HashMap<String, Float> fociScale = new HashMap<>();
    private static float radialHudScale = 0f;
    private static long lastTime = 0L;
    private static boolean lastState = false;
    // РЎРЅСЏС‚РёРµ С„РѕРєСѓСЃР° РЅР°РІРµРґРµРЅРёРµРј РЅР° С†РµРЅС‚СЂ (Р±РµР· РєР»РёРєР°, РєР°Рє РЅР°РґРµРІР°РЅРёРµ):
    // РєСѓСЂСЃРѕСЂ РґРѕР»Р¶РµРЅ С…РѕС‚СЊ СЂР°Р· РїРѕРєРёРЅСѓС‚СЊ С†РµРЅС‚СЂ РїРѕСЃР»Рµ РѕС‚РєСЂС‹С‚РёСЏ РјРµРЅСЋ,
    // РёРЅР°С‡Рµ С„РѕРєСѓСЃ СЃР»РµС‚Р°Р» Р±С‹ РїСЂРё РєР°Р¶РґРѕРј РѕС‚РєСЂС‹С‚РёРё (РєСѓСЂСЃРѕСЂ СѓР¶Рµ РІ С†РµРЅС‚СЂРµ).
    private static boolean centerArmed = false;
    private static boolean centerRemoveHover = false;

    private static boolean mouseReleased = false;

    // NEOFORGE_PORT: RenderGuiOverlayEvent removed in 26.x -> GuiLayer.
    public static final GuiLayer RADIAL_LAYER = (graphics, deltaTracker) -> {
        onRenderRadial(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    };

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus_radial"), RADIAL_LAYER);
    }

    private static void onRenderRadial(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        if (!KeyHandler.radialActive && radialHudScale <= 0f) {

            if (mouseReleased) {
                mc.mouseHandler.grabMouse();
                mouseReleased = false;
            }
            return;
        }
        if (mc.gui.screen() != null) {
            KeyHandler.radialActive = false;
            KeyHandler.radialLock = true;
            if (mouseReleased) {
                mc.mouseHandler.grabMouse();
                mouseReleased = false;
            }
            return;
        }
        long time = System.currentTimeMillis();
        if (KeyHandler.radialActive && radialHudScale == 0f) {
            getFociInfo(player);

            mc.mouseHandler.releaseMouse();
            mouseReleased = true;
            centerArmed = false;
        }
        renderFocusRadialHUD(graphics, time, partialTick);
        updateState(time);
        lastTime = time;
    }

    private static float getRadialChange(long time, long lasttime, long total) {
        return (time - lasttime) / (float) total;
    }

    private static void updateState(long time) {
        if (time <= lastTime) {
            return;
        }
        for (String key : new ArrayList<>(fociHover.keySet())) {
            if (Boolean.TRUE.equals(fociHover.get(key))) {
                if (!KeyHandler.radialActive && !KeyHandler.radialLock) {
                    PacketHandler.sendToServer(new PacketFocusChangeToServer(key));
                    KeyHandler.radialLock = true;
                }
                if (fociScale.getOrDefault(key, 1f) < 1.3f) {
                    fociScale.put(key, Math.min(1.3f,
                            fociScale.getOrDefault(key, 1f) + getRadialChange(time, lastTime, 150L)));
                }
            } else if (fociScale.getOrDefault(key, 1f) > 1.0f) {
                fociScale.put(key, Math.max(1.0f,
                        fociScale.getOrDefault(key, 1f) - getRadialChange(time, lastTime, 250L)));
            }
        }
        // РћС‚РїСѓСЃРєР°РЅРёРµ СЃ РєСѓСЂСЃРѕСЂРѕРј РЅР° РѕРґРµС‚РѕРј С„РѕРєСѓСЃРµ вЂ” СЃРЅСЏС‚СЊ РµРіРѕ (Р±РµР· РєР»РёРєР°).
        if (centerRemoveHover && !KeyHandler.radialActive && !KeyHandler.radialLock) {
            centerRemoveHover = false;
            centerArmed = false;
            KeyHandler.radialLock = true;
            PacketHandler.sendToServer(new PacketFocusChangeToServer("REMOVE"));
        }
        if (!KeyHandler.radialActive) {
            radialHudScale -= getRadialChange(time, lastTime, 150L);
        } else if (radialHudScale < 1.0f) {
            radialHudScale += getRadialChange(time, lastTime, 150L);
        }
        radialHudScale = Math.max(0f, Math.min(1f, radialHudScale));
        if (radialHudScale <= 0f) {
            KeyHandler.radialLock = false;
        }
        lastState = KeyHandler.radialActive;
    }

    private static void getFociInfo(Player player) {
        foci.clear();
        fociItem.clear();
        fociHover.clear();
        fociScale.clear();
        final int[] pouchCount = {0};

        CuriosCompat.forEachCuriosItem(player, stack -> {
            if (stack.getItem() instanceof ItemFocusPouch pouch) {
                pouchCount[0]++;
                int q = 0;
                for (ItemStack item : pouch.getInventory(stack)) {
                    addFocus(item, pouchCount[0] * 1000 + q);
                    q++;
                }
            }
        });

        // NEOFORGE_PORT (26.3): Forge scanned inventory.items only (no armor/offhand slots).
        List<ItemStack> inv = player.getInventory().getNonEquipmentItems();
        for (int a = 0; a < inv.size(); ++a) {
            ItemStack item = inv.get(a);
            if (item.getItem() instanceof ItemFocus) {
                addFocus(item, a);
            } else if (item.getItem() instanceof ItemFocusPouch pouch) {
                pouchCount[0]++;
                int q = 0;
                for (ItemStack inPouch : pouch.getInventory(item)) {
                    addFocus(inPouch, pouchCount[0] * 1000 + q);
                    q++;
                }
            }
        }
    }

    private static void addFocus(ItemStack item, int slot) {
        if (item == null || item.isEmpty() || !(item.getItem() instanceof ItemFocus focus)) {
            return;
        }
        String sh = focus.getSortingHelper(item);
        if (sh == null) {
            return;
        }
        foci.put(sh, slot);
        fociItem.put(sh, item.copy());
        fociScale.put(sh, 1.0f);
        fociHover.put(sh, false);
    }

    private static void renderFocusRadialHUD(GuiGraphicsExtractor graphics, long time, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof ICaster)) {
            held = player.getOffhandItem();
        }
        if (!(held.getItem() instanceof ICaster caster)) {
            return;
        }
        // Р¦РµРЅС‚СЂ СЃ РѕРґРµС‚С‹Рј С„РѕРєСѓСЃРѕРј РїРѕРєР°Р·С‹РІР°РµРј, РґР°Р¶Рµ РµСЃР»Рё Р·Р°РїР°СЃРЅС‹С… С„РѕРєСѓСЃРѕРІ РЅРµС‚:
        // РёРЅР°С‡Рµ РµРґРёРЅСЃС‚РІРµРЅРЅС‹Р№ С„РѕРєСѓСЃ РЅРµР»СЊР·СЏ СЃРЅСЏС‚СЊ РєР»РёРєРѕРј.
        ItemStack equipped = caster.getFocusStack(held);
        if (fociItem.isEmpty() && (equipped == null || equipped.isEmpty())) {
            return;
        }

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        double mx = mc.mouseHandler.xpos() * sw / (double) mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * sh / (double) mc.getWindow().getScreenHeight();
        // NEOFORGE_PORT: LWJGL not on the mod compile classpath in 26.3;
        // MouseHandler tracks the same button state.
        boolean mouseDown = mc.mouseHandler.isLeftPressed();

        int cx = sw / 2;
        int cy = sh / 2;
        float width = 16f + fociItem.size() * 2.5f;

        float angle = (player.tickCount + partialTick) * 0.5f;
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx,  cy);
        graphics.pose().rotate((float) Math.toRadians(angle));
        graphics.pose().scale(radialHudScale,  radialHudScale);
        int ring = (int) (width * 2.75f);
        // NEOFORGE_PORT (26.3): Forge setShaderColor(0.5, 0.5, 0.5, 0.5) covered both rings -> ARGB.
        graphics.blit(RenderPipelines.GUI_TEXTURED, R1, -ring / 2, -ring / 2, 0, 0, ring, ring, ring, ring,
                net.minecraft.util.ARGB.colorFromFloat(0.5f, 0.5f, 0.5f, 0.5f));
        graphics.pose().popMatrix();
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx,  cy);
        graphics.pose().rotate((float) Math.toRadians(-angle));
        graphics.pose().scale(radialHudScale,  radialHudScale);
        int ring2 = (int) (width * 2.55f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, R2, -ring2 / 2, -ring2 / 2, 0, 0, ring2, ring2, ring2, ring2,
                net.minecraft.util.ARGB.colorFromFloat(0.5f, 0.5f, 0.5f, 0.5f));
        graphics.pose().popMatrix();
        ItemStack currentFocus = caster.getFocusStack(held);
        ItemStack tooltipStack = null;
        if (currentFocus != null && !currentFocus.isEmpty()) {
            arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, currentFocus, cx - 8, cy - 8);
            boolean centerHover = Math.abs(mx - cx) <= 10 && Math.abs(my - cy) <= 10;
            if (centerHover) {
                tooltipStack = currentFocus;
            }
            // РќР°РІРµРґРµРЅРёРµ РЅР° РѕРґРµС‚С‹Р№ С„РѕРєСѓСЃ РІ С†РµРЅС‚СЂРµ вЂ” СЃРЅСЏС‚СЊ РµРіРѕ РїСЂРё РѕС‚РїСѓСЃРєР°РЅРёРё,
            // РєР°Рє РЅР°РґРµРІР°РЅРёРµ РЅР°РІРµРґРµРЅРёРµРј (РєР»РёРєР° РЅРµ РЅСѓР¶РЅРѕ).
            if (!centerHover) {
                centerArmed = true;
            }
            centerRemoveHover = centerHover && centerArmed
                    && KeyHandler.radialActive && !KeyHandler.radialLock;
        } else {
            centerRemoveHover = false;
        }

        float currentRot = -90f * radialHudScale;
        if (!fociItem.isEmpty()) {
            float pieSlice = 360f / fociItem.size();
            List<String> keys = new ArrayList<>(foci.keySet());
            for (String key : keys) {
                double xx = Math.cos(Math.toRadians(currentRot)) * width * radialHudScale;
                double yy = Math.sin(Math.toRadians(currentRot)) * width * radialHudScale;
                currentRot += pieSlice;
                float scale = fociScale.getOrDefault(key, 1f);
                int ix = (int) (cx + xx) - 8;
                int iy = (int) (cy + yy) - 8;
                graphics.pose().pushMatrix();
                graphics.pose().translate((float) (cx + xx), (float) (cy + yy));
                graphics.pose().scale(scale,  scale);
                arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, fociItem.get(key), -8, -8);
                graphics.pose().popMatrix();

                if (!KeyHandler.radialLock && KeyHandler.radialActive) {
                    if (Math.abs(mx - (cx + xx)) <= 10 && Math.abs(my - (cy + yy)) <= 10) {
                        fociHover.put(key, true);
                        tooltipStack = fociItem.get(key);
                        if (mouseDown) {
                            KeyHandler.radialActive = false;
                            KeyHandler.radialLock = true;
                            PacketHandler.sendToServer(new PacketFocusChangeToServer(key));
                            break;
                        }
                    } else {
                        fociHover.put(key, false);
                    }
                }
            }
        }

        if (tooltipStack != null) {
            graphics.setTooltipForNextFrame(mc.font, tooltipStack, (int) mx, (int) my);
            // NEOFORGE_PORT (26.3): deferred tooltips are only flushed by Screen rendering; on
            // the HUD (no screen) it was never drawn. Flush it here (Forge drew it immediately).
            graphics.extractDeferredElements((int) mx, (int) my, partialTick);
        }
    }
}