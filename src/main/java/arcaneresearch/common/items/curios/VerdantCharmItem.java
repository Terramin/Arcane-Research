package arcaneresearch.common.items.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;
import arcaneresearch.init.ModEffects;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import javax.annotation.Nullable;
import java.util.List;

public class VerdantCharmItem extends EquippableCurioItem implements IRechargable {
    private final CharmType type;

    public VerdantCharmItem(CharmType type) {
        super(new Item.Properties()
                .rarity(Rarity.RARE), CurioSlot.CHARM);
        this.type = type;
    }

    public static VerdantCharmItem createBasic() {
        return new VerdantCharmItem(CharmType.BASIC);
    }

    public static VerdantCharmItem createLife() {
        return new VerdantCharmItem(CharmType.LIFE);
    }

    public static VerdantCharmItem createSustain() {
        return new VerdantCharmItem(CharmType.SUSTAIN);
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
        if (player.hasEffect(ModEffects.FLUX_TAINT.get()) && RechargeHelper.consumeCharge(stack, player, 5)) {
            player.removeEffect(ModEffects.FLUX_TAINT.get());
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
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        switch (type) {
            case LIFE -> tooltip.add(Component.translatable("item.arcaneresearch.verdant_charm.life.text")
                    .withStyle(ChatFormatting.GOLD));
            case SUSTAIN -> tooltip.add(Component.translatable("item.arcaneresearch.verdant_charm.sustain.text")
                    .withStyle(ChatFormatting.GOLD));
            default -> {
            }
        }
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
