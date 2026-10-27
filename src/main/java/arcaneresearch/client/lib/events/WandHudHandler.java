package arcaneresearch.client.lib.events;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IFocusPicker;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.items.wands.WandManager;
import java.text.DecimalFormat;
import java.util.HashMap;
@OnlyIn(Dist.CLIENT)
public class WandHudHandler {
    public static AspectList currentAura = new AspectList();
    final ResourceLocation hudTex = new ResourceLocation("arcaneresearch:textures/gui/wand_hud.png");
    HashMap<Integer, AspectList> oldvals = new HashMap<>();
    long nextsync = 0L;
    DecimalFormat secondsFormatter = new DecimalFormat("#######.#");
    ItemStack lastItem = null;
    int lastCount = 0;
    long lastTradeSync = 0L;
    @OnlyIn(Dist.CLIENT)
    public void renderCastingWandHud(Minecraft mc, GuiGraphics graphics, float partialTicks,
                                     Player player, long time, ItemStack wandstack) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return;
        }
        int selected = player.getInventory().selected;
        if (!this.oldvals.containsKey(selected)) {
            this.oldvals.put(selected, wand.getAllVis(wandstack));
        } else if (this.nextsync <= time) {
            this.oldvals.put(selected, wand.getAllVis(wandstack));
            this.nextsync = time + 1000L;
        }
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        int h = mc.getWindow().getGuiScaledHeight();
        int dialY = ModConfig.CONFIG_GRAPHICS.dialBottom ? h - 32 : 0;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, dialY, 0.0F);
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        graphics.blit(this.hudTex, 0, 0, 0, 0, 64, 64, 256, 256);
        graphics.pose().popPose();
        graphics.pose().translate(16.0F, 16.0F, 0.0F);
        int max = wand.getMaxVis(wandstack);
        ItemFocusBasic focus = wand.getFocus(wandstack);
        ItemStack focusStack = wand.getFocusStack(wandstack);
        int count = 0;
        AspectList aspects = wand.getAllVis(wandstack);
        AspectList old = this.oldvals.get(selected);
        for (Aspect aspect : aspects.getAspects()) {
            int amt = aspects.getAmount(aspect);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            graphics.pose().pushPose();
            if (!ModConfig.CONFIG_GRAPHICS.dialBottom) {
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(-15 + count * 24));
            graphics.pose().translate(0.0F, -32.0F, 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            int loc = max > 0 ? (int) (30.0F * amt / max) : 0;
            int color = aspect.getColor();
            graphics.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F,
                    (color & 255) / 255.0F, 0.8F);
            graphics.blit(this.hudTex, -4, 35 - loc, 104, 0, 8, loc, 256, 256);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(this.hudTex, -8, -3, 72, 0, 16, 42, 256, 256);
            int sh = 0;
            if (focus != null && !focusStack.isEmpty()
                    && focus.getVisCost(focusStack).getAmount(aspect) > 0) {
                graphics.blit(this.hudTex, -4, -8, 136, 0, 8, 8, 256, 256);
                sh = 8;
            }
            if (old != null) {
                if (old.getAmount(aspect) > amt) {
                    graphics.blit(this.hudTex, -4, -8 - sh, 128, 0, 8, 8, 256, 256);
                } else if (old.getAmount(aspect) < amt) {
                    graphics.blit(this.hudTex, -4, -8 - sh, 120, 0, 8, 8, 256, 256);
                }
            }
            if (player.isCrouching()) {
                graphics.pose().mulPose(Axis.ZP.rotationDegrees(-90.0F));
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200.0F);
                graphics.drawString(mc.font, String.valueOf(amt), -32, -4, 0xFFFFFF, true);
                if (focus != null && !focusStack.isEmpty()
                        && focus.getVisCost(focusStack).getAmount(aspect) > 0) {
                    float mod = wand.getConsumptionModifier(wandstack, player, aspect, false);
                    String msg = this.secondsFormatter.format(focus.getVisCost(focusStack)
                            .getAmount(aspect) * mod);
                    graphics.drawString(mc.font, msg, 8, -4, 0xFFFFFF, true);
                }
                graphics.pose().popPose();
            }
            graphics.pose().popPose();
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
                graphics.pose().pushPose();
                graphics.pose().translate(-24.0F, -24.0F, 90.0F);
                graphics.renderItem(wand.getFocusStack(wandstack), 16, 16);
                graphics.pose().popPose();
                float cooldown = WandManager.getCooldown(player);
                if (cooldown > 0.0F) {
                    String secs = this.secondsFormatter.format(cooldown) + "s";
                    int w = mc.font.width(secs) / 2;
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.5F, 0.5F, 1.0F);
                    graphics.pose().translate(0.0F, 0.0F, 300.0F);
                    graphics.drawString(mc.font, secs, -w, -4, 0xFFFFFF, true);
                    graphics.pose().popPose();
                }
            }
        }
        graphics.pose().popPose();
    }
    @OnlyIn(Dist.CLIENT)
    public void renderWandTradeHud(Minecraft mc, GuiGraphics graphics, float partialTicks,
                                   Player player, long time, ItemStack picked) {
        if (picked.isEmpty()) {
            return;
        }
        int amount = this.lastCount;
        if (this.lastItem == null || !ItemStack.isSameItem(picked, this.lastItem)
                || time - this.lastTradeSync > 1000L) {
            amount = 0;
            for (ItemStack is : player.getInventory().items) {
                if (!is.isEmpty() && ItemStack.isSameItem(is, picked)) {
                    amount += is.getCount();
                }
            }
            this.lastItem = picked.copy();
            this.lastTradeSync = time;
        }
        this.lastCount = amount;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        graphics.renderItem(picked, -8, -8);
        String am = String.valueOf(amount);
        int sw = mc.font.width(am);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, -mc.font.lineHeight, 200.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(mc.font, am, 16 - sw, 24, 0xFFFFFF, true);
        graphics.pose().popPose();
    }
    public void renderAspectsInGui(AbstractContainerScreen<?> gui, Player player) {
    }
}
