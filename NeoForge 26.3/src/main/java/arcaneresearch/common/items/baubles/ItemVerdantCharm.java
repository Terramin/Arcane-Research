package arcaneresearch.common.items.baubles;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;
import arcaneresearch.init.ModEffects;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.function.Consumer;

public class ItemVerdantCharm extends Item implements ICurioItem, IRechargable {

    private final CharmType type;

    public ItemVerdantCharm(Item.Properties props, CharmType type) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
        this.type = type;
    }

    public static ItemVerdantCharm createBasic(Item.Properties props) {
        return new ItemVerdantCharm(props, CharmType.BASIC);
    }

    public static ItemVerdantCharm createLife(Item.Properties props) {
        return new ItemVerdantCharm(props, CharmType.LIFE);
    }

    public static ItemVerdantCharm createSustain(Item.Properties props) {
        return new ItemVerdantCharm(props, CharmType.SUSTAIN);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof Player player && !player.level().isClientSide()) {
            tickWorn(player, stack);
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("charm");
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    public void tickWorn(Player player, ItemStack stack) {
        if (player.tickCount % 20 != 0) {
            return;
        }

        if (player.hasEffect(MobEffects.WITHER) && RechargeHelper.consumeCharge(stack, player, 20)) {
            player.removeEffect(MobEffects.WITHER);
            return;
        }

        if (player.hasEffect(MobEffects.POISON) && RechargeHelper.consumeCharge(stack, player, 10)) {
            player.removeEffect(MobEffects.POISON);
            return;
        }

        // NEOFORGE_PORT: DeferredHolder<MobEffect, MobEffect> IS a Holder -> pass directly (no .get()).
        if (player.hasEffect(ModEffects.FLUX_TAINT) && RechargeHelper.consumeCharge(stack, player, 5)) {
            player.removeEffect(ModEffects.FLUX_TAINT);
            return;
        }

        if (type == CharmType.LIFE) {
            if (player.getHealth() < player.getMaxHealth() && RechargeHelper.consumeCharge(stack, player, 5)) {
                player.heal(1.0f);
                return;
            }
        }

        if (type == CharmType.SUSTAIN) {

            if (player.getAirSupply() < 100 && RechargeHelper.consumeCharge(stack, player, 1)) {
                player.setAirSupply(300);
                return;
            }

            if (player.canEat(false) && RechargeHelper.consumeCharge(stack, player, 1)) {
                player.getFoodData().eat(1, 0.3f);
            }
        }
    }

    // NEOFORGE_PORT: appendHoverText 5-arg (TooltipContext, TooltipDisplay, Consumer<Component>).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {

        switch (type) {
            case LIFE -> tooltip.accept(Component.translatable("item.arcaneresearch.verdant_charm.life.text")
                    .withStyle(ChatFormatting.GOLD));
            case SUSTAIN -> tooltip.accept(Component.translatable("item.arcaneresearch.verdant_charm.sustain.text")
                    .withStyle(ChatFormatting.GOLD));
            default -> {
            }
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity entity) {
        return 200;
    }

    @Override
    public EnumChargeDisplay showInHud(ItemStack stack, LivingEntity entity) {
        return EnumChargeDisplay.NORMAL;
    }

    public enum CharmType {
        BASIC(0),
        LIFE(1),
        SUSTAIN(2);

        public final int id;

        CharmType(int id) {
            this.id = id;
        }

        public static CharmType fromId(int id) {
            return switch (id) {
                case 1 -> LIFE;
                case 2 -> SUSTAIN;
                default -> BASIC;
            };
        }
    }
}
