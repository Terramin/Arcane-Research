package arcaneresearch.client.lib.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IFocusPicker;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.items.wands.WandManager;

import java.text.DecimalFormat;
import java.util.HashMap;

public class WandHudHandler {
    public static AspectList currentAura = new AspectList();

    final Identifier hudTex = Identifier.parse("arcaneresearch:textures/gui/wand_hud.png");

    HashMap<Integer, AspectList> oldvals = new HashMap<>();
    long nextsync = 0L;
    DecimalFormat secondsFormatter = new DecimalFormat("#######.#");

    ItemStack lastItem = null;
    int lastCount = 0;
    long lastTradeSync = 0L;

    public void renderCastingWandHud(Minecraft mc, GuiGraphicsExtractor graphics, float partialTicks,
                                     Player player, long time, ItemStack wandstack) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return;
        }
        int selected = player.getInventory().getSelectedSlot();
        if (!this.oldvals.containsKey(selected)) {
            this.oldvals.put(selected, wand.getAllVis(wandstack));
        } else if (this.nextsync <= time) {
            this.oldvals.put(selected, wand.getAllVis(wandstack));
            this.nextsync = time + 1000L;
        }

        int h = mc.getWindow().getGuiScaledHeight();
        int dialY = ModConfig.CONFIG_GRAPHICS.dialBottom ? h - 32 : 0;

        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F,  dialY);

        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5F,  0.5F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.hudTex, 0, 0, 0, 0, 64, 64, 256, 256);
        graphics.pose().popMatrix();

        graphics.pose().translate(16.0F,  16.0F);

        int max = wand.getMaxVis(wandstack);
        ItemFocusBasic focus = wand.getFocus(wandstack);
        ItemStack focusStack = wand.getFocusStack(wandstack);
        int count = 0;

        AspectList aspects = wand.getAllVis(wandstack);
        AspectList old = this.oldvals.get(selected);
        for (Aspect aspect : aspects.getAspects()) {
            int amt = aspects.getAmount(aspect);

            graphics.pose().pushMatrix();
            if (!ModConfig.CONFIG_GRAPHICS.dialBottom) {
                graphics.pose().rotate((float) Math.toRadians(90.0F));
            }
            graphics.pose().rotate((float) Math.toRadians(-15 + count * 24));
            graphics.pose().translate(0.0F,  -32.0F);
            graphics.pose().scale(0.5F,  0.5F);

            int loc = max > 0 ? (int) (30.0F * amt / max) : 0;

            int color = aspect.getColor();
            
            int tint = (0xCC << 24) | (color & 0x00FFFFFF);
            graphics.blit(RenderPipelines.GUI_TEXTURED, this.hudTex, -4, 35 - loc, 104, 0, 8, loc, 256, 256, tint);

            graphics.blit(RenderPipelines.GUI_TEXTURED, this.hudTex, -8, -3, 72, 0, 16, 42, 256, 256);

            int sh = 0;
            if (focus != null && !focusStack.isEmpty()
                    && focus.getVisCost(focusStack).getAmount(aspect) > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, this.hudTex, -4, -8, 136, 0, 8, 8, 256, 256);
                sh = 8;
            }

            if (old != null) {
                if (old.getAmount(aspect) > amt) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, this.hudTex, -4, -8 - sh, 128, 0, 8, 8, 256, 256);
                } else if (old.getAmount(aspect) < amt) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, this.hudTex, -4, -8 - sh, 120, 0, 8, 8, 256, 256);
                }
            }

            if (player.isCrouching()) {
                graphics.pose().rotate((float) Math.toRadians(-90.0F));

                graphics.pose().pushMatrix();
                graphics.pose().translate(0.0F,  0.0F);
                graphics.text(mc.font, String.valueOf(amt), -32, -4, 0xFFFFFFFF, true);
                if (focus != null && !focusStack.isEmpty()
                        && focus.getVisCost(focusStack).getAmount(aspect) > 0) {
                    float mod = wand.getConsumptionModifier(wandstack, player, aspect, false);
                    String msg = this.secondsFormatter.format(focus.getVisCost(focusStack)
                            .getAmount(aspect) * mod);
                    graphics.text(mc.font, msg, 8, -4, 0xFFFFFFFF, true);
                }
                graphics.pose().popMatrix();
            }

            graphics.pose().popMatrix();
            count++;
        }

        if (focus != null) {
            ItemStack picked = ItemStack.EMPTY;
            if (focus instanceof IFocusPicker picker) {
                picked = picker.getPickedBlock(wandstack);
                if (!picked.isEmpty()) {
                    renderWandTradeHud(mc, graphics, partialTicks, player, time, picked);
                }
            }
            if (picked.isEmpty()) {
                graphics.pose().pushMatrix();
                graphics.pose().translate(-24.0F,  -24.0F);
                graphics.item(wand.getFocusStack(wandstack), 16, 16);
                graphics.pose().popMatrix();

                float cooldown = WandManager.getCooldown(player);
                if (cooldown > 0.0F) {
                    String secs = this.secondsFormatter.format(cooldown) + "s";
                    int w = mc.font.width(secs) / 2;
                    graphics.pose().pushMatrix();
                    graphics.pose().scale(0.5F,  0.5F);
                    graphics.pose().translate(0.0F,  0.0F);
                    graphics.text(mc.font, secs, -w, -4, 0xFFFFFFFF, true);
                    graphics.pose().popMatrix();
                }
            }
        }

        graphics.pose().popMatrix();
    }

    public void renderWandTradeHud(Minecraft mc, GuiGraphicsExtractor graphics, float partialTicks,
                                   Player player, long time, ItemStack picked) {
        if (picked.isEmpty()) {
            return;
        }
        int amount = this.lastCount;
        if (this.lastItem == null || !ItemStack.isSameItem(picked, this.lastItem)
                || time - this.lastTradeSync > 1000L) {
            amount = 0;
            
            for (ItemStack is : player.getInventory().getNonEquipmentItems()) {
                if (!is.isEmpty() && ItemStack.isSameItem(is, picked)) {
                    amount += is.getCount();
                }
            }
            this.lastItem = picked.copy();
            this.lastTradeSync = time;
        }
        this.lastCount = amount;

        graphics.item(picked, -8, -8);
        String am = String.valueOf(amount);
        int sw = mc.font.width(am);
        graphics.pose().pushMatrix();
        graphics.pose().translate(0.0F,  -mc.font.lineHeight);
        graphics.pose().scale(0.5F,  0.5F);
        graphics.text(mc.font, am, 16 - sw, 24, 0xFFFFFFFF, true);
        graphics.pose().popMatrix();
    }

    public void renderAspectsInGui(AbstractContainerScreen<?> gui, Player player) {

    }
}