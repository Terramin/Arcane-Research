package arcaneresearch.client.lib.events;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
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
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FocusRadialHandler {
    private static final ResourceLocation R1 =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/radial.png");
    private static final ResourceLocation R2 =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/radial2.png");
    private static final TreeMap<String, Integer> foci = new TreeMap<>();
    private static final HashMap<String, ItemStack> fociItem = new HashMap<>();
    private static final HashMap<String, Boolean> fociHover = new HashMap<>();
    private static final HashMap<String, Float> fociScale = new HashMap<>();
    private static float radialHudScale = 0f;
    private static long lastTime = 0L;
    private static boolean lastState = false;
    private static boolean centerArmed = false;
    private static boolean centerRemoveHover = false;
    private static boolean mouseReleased = false;
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
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
        if (mc.screen != null) {
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
        renderFocusRadialHUD(event.getGuiGraphics(), time, event.getPartialTick());
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
        List<ItemStack> inv = player.getInventory().items;
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
    private static void renderFocusRadialHUD(GuiGraphics graphics, long time, float partialTick) {
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
        ItemStack equipped = caster.getFocusStack(held);
        if (fociItem.isEmpty() && (equipped == null || equipped.isEmpty())) {
            return;
        }
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        double mx = mc.mouseHandler.xpos() * sw / (double) mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * sh / (double) mc.getWindow().getScreenHeight();
        boolean mouseDown = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT)
                == GLFW.GLFW_PRESS;
        int cx = sw / 2;
        int cy = sh / 2;
        float width = 16f + fociItem.size() * 2.5f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float angle = (player.tickCount + partialTick) * 0.5f;
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        graphics.pose().scale(radialHudScale, radialHudScale, 1f);
        int ring = (int) (width * 2.75f);
        RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 0.5f);
        graphics.blit(R1, -ring / 2, -ring / 2, 0, 0, ring, ring, ring, ring);
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-angle));
        graphics.pose().scale(radialHudScale, radialHudScale, 1f);
        int ring2 = (int) (width * 2.55f);
        graphics.blit(R2, -ring2 / 2, -ring2 / 2, 0, 0, ring2, ring2, ring2, ring2);
        graphics.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ItemStack currentFocus = caster.getFocusStack(held);
        ItemStack tooltipStack = null;
        if (currentFocus != null && !currentFocus.isEmpty()) {
            arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, currentFocus, cx - 8, cy - 8);
            boolean centerHover = Math.abs(mx - cx) <= 10 && Math.abs(my - cy) <= 10;
            if (centerHover) {
                tooltipStack = currentFocus;
            }
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
                graphics.pose().pushPose();
                graphics.pose().translate(cx + xx, cy + yy, 0);
                graphics.pose().scale(scale, scale, 1f);
                arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, fociItem.get(key), -8, -8);
                graphics.pose().popPose();
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
            graphics.renderTooltip(mc.font, tooltipStack, (int) mx, (int) my);
        }
        RenderSystem.disableBlend();
    }
}
