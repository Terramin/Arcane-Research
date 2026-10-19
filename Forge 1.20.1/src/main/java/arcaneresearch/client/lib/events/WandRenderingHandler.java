package arcaneresearch.client.lib.events;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.client.renderers.ArchitectOverlayRenderer;
import arcaneresearch.common.items.wands.ItemFocusPouch;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.HashMap;
import java.util.TreeMap;
@OnlyIn(Dist.CLIENT)
public class WandRenderingHandler {
    static float radialHudScale = 0.0F;
    public static ItemStack heldWand(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main != null && !main.isEmpty() && main.getItem() instanceof IWand) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off != null && !off.isEmpty() && off.getItem() instanceof IWand) {
            return off;
        }
        return ItemStack.EMPTY;
    }    final ResourceLocation radialTex = new ResourceLocation("arcaneresearch:textures/misc/radial.png");
    final ResourceLocation radial2Tex = new ResourceLocation("arcaneresearch:textures/misc/radial2.png");
    TreeMap<String, Integer> foci = new TreeMap<>();
    HashMap<String, ItemStack> fociItem = new HashMap<>();
    HashMap<String, Boolean> fociHover = new HashMap<>();
    HashMap<String, Float> fociScale = new HashMap<>();
    long lastTime = 0L;
    boolean lastState = false;
    boolean centerArmed = false;
    boolean centerRemoveHover = false;
    private static void drawSpinningRing(GuiGraphics graphics, ResourceLocation texture,
                                         int cx, int cy, float size, float spin) {
        if (size <= 0.5F) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(spin));
        graphics.setColor(0.5F, 0.5F, 0.5F, 0.78F);
        int s = (int) size;
        graphics.blit(texture, -s / 2, -s / 2, 0, 0, s, s, s, s);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().popPose();
    }
    @OnlyIn(Dist.CLIENT)
    public void handleFociRadial(Minecraft mc, long time, RenderGuiEvent.Pre event) {
        if (mc.player == null || heldWand(mc.player).isEmpty()) {
            if (!KeyHandler.radialActive && radialHudScale <= 0.0F) {
                return;
            }
        }
        if (!KeyHandler.radialActive && radialHudScale <= 0.0F) {
            return;
        }
        if (KeyHandler.radialActive) {
            if (mc.screen != null) {
                KeyHandler.radialActive = false;
                KeyHandler.radialLock = true;
                return;
            }
            if (radialHudScale == 0.0F) {
                this.foci.clear();
                this.fociItem.clear();
                this.fociHover.clear();
                this.fociScale.clear();
                this.centerArmed = false;
                int pouchcount = 0;
                Player player = mc.player;
                if (player != null) {
                    java.util.List<ItemStack> pouches = new java.util.ArrayList<>();
                    try {
                        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios()
                                .forEach((slotId, slotHandler) -> {
                                    for (int i = 0; i < slotHandler.getStacks().getSlots(); i++) {
                                        ItemStack worn = slotHandler.getStacks().getStackInSlot(i);
                                        if (!worn.isEmpty() && worn.getItem() instanceof ItemFocusPouch) {
                                            pouches.add(worn);
                                        }
                                    }
                                }));
                    } catch (Exception ignored) {
                    }
                    for (ItemStack pouchStack : pouches) {
                        pouchcount++;
                        int q = 0;
                        for (ItemStack item : ((ItemFocusPouch) pouchStack.getItem()).getInventory(pouchStack)) {
                            if (!item.isEmpty() && item.getItem() instanceof ItemFocusBasic basic) {
                                putFocus(basic.getSortingHelper(item), item, q + pouchcount * 1000);
                            }
                            q++;
                        }
                    }
                    for (int a = 0; a < player.getInventory().items.size(); a++) {
                        ItemStack item = player.getInventory().items.get(a);
                        if (!item.isEmpty() && item.getItem() instanceof ItemFocusBasic basic) {
                            putFocus(basic.getSortingHelper(item), item, a);
                        }
                        if (!item.isEmpty() && item.getItem() instanceof ItemFocusPouch) {
                            pouchcount++;
                            int q = 0;
                            for (ItemStack in : ((ItemFocusPouch) item.getItem()).getInventory(item)) {
                                if (!in.isEmpty() && in.getItem() instanceof ItemFocusBasic basic) {
                                    putFocus(basic.getSortingHelper(in), in, q + pouchcount * 1000);
                                }
                                q++;
                            }
                        }
                    }
                    if (!this.foci.isEmpty()) {
                        mc.mouseHandler.releaseMouse();
                    }
                }
            }
        } else if (mc.screen == null && this.lastState && mc.isWindowActive()) {
            mc.mouseHandler.grabMouse();
            this.lastState = false;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int sw = event.getWindow().getGuiScaledWidth();
        int sh = event.getWindow().getGuiScaledHeight();
        renderFocusRadialHUD(mc, graphics, sw, sh, time, event.getPartialTick());
        if (time > this.lastTime) {
            for (String key : this.fociHover.keySet()) {
                if (Boolean.TRUE.equals(this.fociHover.get(key))) {
                    if (!KeyHandler.radialActive && !KeyHandler.radialLock && mc.player != null) {
                        PacketHandler.sendToServer(new PacketWandFocusChangeToServer(mc.player, key));
                        KeyHandler.radialLock = true;
                    }
                    if (this.fociScale.get(key) < 1.3F) {
                        this.fociScale.put(key, this.fociScale.get(key) + 0.025F);
                    }
                    continue;
                }
                if (this.fociScale.get(key) > 1.0F) {
                    this.fociScale.put(key, this.fociScale.get(key) - 0.025F);
                }
            }
            if (this.centerRemoveHover && !KeyHandler.radialActive
                    && !KeyHandler.radialLock && mc.player != null) {
                this.centerRemoveHover = false;
                this.centerArmed = false;
                KeyHandler.radialLock = true;
                PacketHandler.sendToServer(new PacketWandFocusChangeToServer(mc.player, "REMOVE"));
            }
            if (!KeyHandler.radialActive) {
                for (String key : this.fociHover.keySet()) {
                    this.fociHover.put(key, false);
                }
            }
            if (!KeyHandler.radialActive) {
                radialHudScale -= 0.05F;
            } else if (radialHudScale < 1.0F) {
                radialHudScale += 0.05F;
            }
            if (radialHudScale > 1.0F) {
                radialHudScale = 1.0F;
            }
            if (radialHudScale < 0.0F) {
                radialHudScale = 0.0F;
                KeyHandler.radialLock = false;
            }
            this.lastTime = time + 5L;
            this.lastState = KeyHandler.radialActive;
        }
    }
    private void putFocus(String key, ItemStack item, int slot) {
        this.foci.put(key, slot);
        this.fociItem.put(key, item.copy());
        this.fociScale.put(key, 1.0F);
        this.fociHover.put(key, false);
    }
    @OnlyIn(Dist.CLIENT)
    private void renderFocusRadialHUD(Minecraft mc, GuiGraphics graphics, int sw, int sh,
                                      long time, float partialTicks) {
        ItemStack wandStack = mc.player == null ? ItemStack.EMPTY : heldWand(mc.player);
        if (mc.player == null || !(wandStack.getItem() instanceof IWand wand)) {
            return;
        }
        ItemFocusBasic focus = wand.getFocus(wandStack);
        double mouseX = mc.mouseHandler.xpos() * sw / mc.getWindow().getWidth();
        double mouseY = mc.mouseHandler.ypos() * sh / mc.getWindow().getHeight();
        boolean clicked = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (this.fociItem.isEmpty() && focus == null) {
            return;
        }
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        int cx = sw / 2;
        int cy = sh / 2;
        float width = 16.0F + this.fociItem.size() * 2.5F;
        float spin = partialTicks + (mc.player.tickCount % 720) / 2.0F;
        drawSpinningRing(graphics, this.radialTex, cx, cy, width * 2.75F * radialHudScale, spin);
        drawSpinningRing(graphics, this.radial2Tex, cx, cy, width * 2.55F * radialHudScale, -spin);
        ItemStack tooltipStack = null;
        if (focus != null) {
            ItemStack item = wand.getFocusStack(wandStack).copy();
            graphics.renderItem(item, cx - 8, cy - 8);
            int mx = (int) (mouseX - cx);
            int my = (int) (mouseY - cy);
            boolean centerHover = mx >= -10 && mx <= 10 && my >= -10 && my <= 10;
            if (centerHover) {
                tooltipStack = item;
            }
            if (!centerHover) {
                this.centerArmed = true;
            }
            this.centerRemoveHover = centerHover && this.centerArmed
                    && KeyHandler.radialActive && !KeyHandler.radialLock;
        } else {
            this.centerRemoveHover = false;
        }
        float currentRot = -90.0F * radialHudScale;
        if (!this.fociItem.isEmpty()) {
            float pieSlice = 360.0F / this.fociItem.size();
            String key = this.foci.firstKey();
            for (int a = 0; a < this.fociItem.size(); a++) {
                double xx = Mth.cos(currentRot / 180.0F * (float) Math.PI) * width * radialHudScale;
                double yy = Mth.sin(currentRot / 180.0F * (float) Math.PI) * width * radialHudScale;
                currentRot += pieSlice;
                float scale = this.fociScale.get(key);
                int ix = (int) (cx + xx) - 8;
                int iy = (int) (cy + yy) - 8;
                graphics.pose().pushPose();
                graphics.pose().translate(cx + xx, cy + yy, 0);
                graphics.pose().scale(scale, scale, 1.0F);
                graphics.pose().translate(-(cx + xx), -(cy + yy), 0);
                ItemStack item = this.fociItem.get(key).copy();
                graphics.renderItem(item, ix, iy);
                graphics.pose().popPose();
                if (!KeyHandler.radialLock && KeyHandler.radialActive) {
                    int mx = (int) (mouseX - cx - xx);
                    int my = (int) (mouseY - cy - yy);
                    if (mx >= -10 && mx <= 10 && my >= -10 && my <= 10) {
                        this.fociHover.put(key, true);
                        tooltipStack = this.fociItem.get(key);
                        if (clicked && mc.player != null) {
                            KeyHandler.radialActive = false;
                            KeyHandler.radialLock = true;
                            PacketHandler.sendToServer(new PacketWandFocusChangeToServer(mc.player, key));
                            break;
                        }
                    } else {
                        this.fociHover.put(key, false);
                    }
                }
                key = this.foci.higherKey(key);
                if (key == null) {
                    break;
                }
            }
        }
        if (tooltipStack != null) {
            graphics.renderTooltip(mc.font, tooltipStack, (int) mouseX, (int) mouseY);
        }
    }
    @OnlyIn(Dist.CLIENT)
    public boolean handleArchitectOverlay(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                          Player player, ItemStack stack, BlockHitResult fallback, float partialTicks) {
        return ArchitectOverlayRenderer.renderArchitect(poseStack, player, stack, partialTicks);
    }
}
