package thaumictinker.gear;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModSounds;

 



 
public class CleansingTalismanItem extends Item implements ICurioItem {

    public static final int USES = 100;
    private static final int DEFAULT_DAMAGE = 1;
    private static final int WARP_DAMAGE = 10;

    public CleansingTalismanItem(Properties properties) {
        super(properties);
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("thaumictinker:active");
    }

    public static void flipEnabled(ItemStack stack) {
        if (isEnabled(stack)) {
            if (stack.hasTag()) {
                stack.getTag().remove("thaumictinker:active");
            }
        } else {
            stack.getOrCreateTag().putBoolean("thaumictinker:active", true);
        }
    }

    public static boolean isWarpEffect(MobEffectInstance effect) {
        return effect.getEffect() == ModEffects.BLURRED_VISION.get()
                || effect.getEffect() == ModEffects.DEATH_GAZE.get()
                || effect.getEffect() == ModEffects.INFECTIOUS_VIS_EXHAUST.get()
                || effect.getEffect() == ModEffects.SUN_SCORNED.get()
                || effect.getEffect() == ModEffects.UNNATURAL_HUNGER.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (!level.isClientSide()) {
            flipEnabled(player.getItemInHand(hand));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.3F, 0.1F);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(isEnabled(stack) ? "ttinkerer.ttmisc.active" : "ttinkerer.ttmisc.inactive"));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("necklace");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level() instanceof ServerLevel level && entity instanceof Player
                && isEnabled(stack) && entity.tickCount % 20 == 0) {
            cleanse(level, stack, entity);
        }
    }

    public static boolean cleanse(ServerLevel level, ItemStack stack, LivingEntity wearer) {
        boolean removed = false;
        int damage = DEFAULT_DAMAGE;
        if (wearer.isOnFire()) {
            wearer.extinguishFire();
            removed = true;
        } else {
            for (MobEffectInstance effect : new ArrayList<>(wearer.getActiveEffects())) {
                if (effect.getEffect().getCategory() != MobEffectCategory.HARMFUL) {
                    continue;
                }
                MobEffect id = effect.getEffect();
                wearer.removeEffect(id);
                removed = true;
                if (isWarpEffect(effect)) {
                    damage = WARP_DAMAGE;
                }
                break;
            }
        }
        if (removed) {
            stack.hurtAndBreak(damage, wearer, broken -> {
            });
            level.playSound(null, wearer.getX(), wearer.getY(), wearer.getZ(), ModSounds.WAND.get(),
                    SoundSource.PLAYERS, 0.3F, 0.1F);
        }
        return removed;
    }
}
