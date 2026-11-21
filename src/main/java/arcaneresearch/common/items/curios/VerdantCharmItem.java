package arcaneresearch.common.items.curios;

import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;
import arcaneresearch.init.ModEffects;
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
import top.theillusivec4.curios.api.SlotContext;

import java.util.function.Consumer;

public class VerdantCharmItem extends EquippableCurioItem implements IRechargable {
    private final CharmType type;

    public VerdantCharmItem(Item.Properties props, CharmType type) {
        super(props
                .rarity(Rarity.RARE), CurioSlot.CHARM);
        this.type = type;
    }

    public static VerdantCharmItem createBasic(Item.Properties props) {
        return new VerdantCharmItem(props, CharmType.BASIC);
    }

    public static VerdantCharmItem createLife(Item.Properties props) {
        return new VerdantCharmItem(props, CharmType.LIFE);
    }

    public static VerdantCharmItem createSustain(Item.Properties props) {
        return new VerdantCharmItem(props, CharmType.SUSTAIN);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && !player.level().isClientSide()) {
            tickWorn(player, stack);
        }
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
        if (player.hasEffect(ModEffects.FLUX_TAINT)
                && RechargeHelper.consumeCharge(stack, player, 5)) {
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
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
