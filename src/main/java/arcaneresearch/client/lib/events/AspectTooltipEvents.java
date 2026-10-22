package arcaneresearch.client.lib.events;

import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.common.lib.events.PlayerEvents;

@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class AspectTooltipEvents {

    static boolean shouldShowTags() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() == null || !(mc.gui.screen() instanceof AbstractContainerScreen)) {
            return false;
        }
        if (mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        return Minecraft.getInstance().hasShiftDown() != ModConfig.CONFIG_GRAPHICS.showTags;
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        try {
            int charge = PlayerEvents.getRunicCharge(stack);
            if (charge > 0) {
                event.getToolTip().add(Component.literal(ChatFormatting.GOLD
                        + Component.translatable("item.runic.charge").getString() + " +" + charge));
            }
            Player player = event.getEntity();
            int warp = player != null ? PlayerEvents.getFinalWarp(stack, player) : 0;
            if (warp > 0) {
                event.getToolTip().add(Component.literal(ChatFormatting.DARK_PURPLE
                        + Component.translatable("item.warping").getString() + " " + warp));
            }
            int discount = player != null ? PlayerEvents.getFinalDiscount(stack, player) : 0;
            if (discount > 0) {
                event.getToolTip().add(Component.literal(ChatFormatting.DARK_PURPLE
                        + Component.translatable("tc.visdiscount").getString() + ": " + discount + "%"));
            }
            if (stack.getItem() instanceof IRechargable) {
                int c = RechargeHelper.getCharge(stack);
                if (c >= 0) {
                    event.getToolTip().add(Component.literal(ChatFormatting.YELLOW
                            + Component.translatable("tc.charge").getString() + " " + c));
                }
            }
            if (stack.getItem() instanceof IEssentiaContainerItem essentiaItem) {
                AspectList aspects = essentiaItem.getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    for (Aspect tag : aspects.getAspectsSortedByName()) {

                        int tagColor = tag.getColor();
                        event.getToolTip().add(Component.literal(tag.getName() + " x" + aspects.getAmount(tag))
                                .withStyle(style -> style.withColor(tagColor)));
                    }
                }
            }
            ListTag infEnch = EnumInfusionEnchantment.getInfusionEnchantmentTagList(stack);
            if (infEnch != null) {
                for (int j = 0; j < infEnch.size(); ++j) {
                    
                    int k = infEnch.getCompoundOrEmpty(j).getShortOr("id", (short) 0);
                    int l = infEnch.getCompoundOrEmpty(j).getShortOr("lvl", (short) 0);
                    if (k >= 0 && k < EnumInfusionEnchantment.values().length) {
                        String s = ChatFormatting.GOLD
                                + Component.translatable("enchantment.infusion."
                                + EnumInfusionEnchantment.values()[k].toString()).getString();
                        if (EnumInfusionEnchantment.values()[k].maxLevel > 1) {
                            s = s + " " + Component.translatable("enchantment.level." + l).getString();
                        }
                        event.getToolTip().add(1, Component.literal(s));
                    }
                }
            }
        } catch (Exception ex) {
            Arcaneresearch.LOGGER.debug("Aspect tooltip failed", ex);
        }
    }

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (!shouldShowTags()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        try {

            AspectList tags = null;
            if (stack.getItem() instanceof IEssentiaContainerItem container
                    && !container.ignoreContainedAspects()) {
                AspectList contained = container.getAspects(stack);
                if (contained != null && contained.size() > 0) {
                    tags = contained;
                }
            }
            if (tags == null) {
                tags = ArcaneresearchCraftingManager.getObjectTags(stack);
            }
            if (tags == null || tags.size() == 0) {
                return;
            }
            event.getTooltipElements().add(Either.right(new AspectIconRow(tags)));
        } catch (Exception ex) {
            Arcaneresearch.LOGGER.debug("Aspect icon row failed", ex);
        }
    }

    public static void registerFactory(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AspectIconRow.class, ClientAspectIconRow::new);
    }

    public record AspectIconRow(AspectList tags) implements TooltipComponent {
    }

    public static class ClientAspectIconRow implements ClientTooltipComponent {
        private final AspectList tags;

        public ClientAspectIconRow(AspectIconRow row) {
            this.tags = row.tags();
        }

        @Override
        public int getHeight(Font font) {
            return 18;
        }

        @Override
        public int getWidth(Font font) {
            return Math.max(18, tags.size() * 18);
        }

        @Override
        public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
            int index = 0;
            for (Aspect tag : tags.getAspectsSortedByAmount()) {

                if (tag == null || tag.getImage() == null) {
                    continue;
                }
                try {
                    AspectRenderer.drawAspect(graphics, x + index * 18, y, tag, tags.getAmount(tag));
                } catch (Exception ignored) {
                }
                ++index;
            }
        }
    }
}