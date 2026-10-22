package thaumictinker.elements;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import arcaneresearch.api.aspects.Aspect;

 



 
public class InfusedPotionItem extends Item {

    public static final int POTION_DURATION = 3600;

    private final Aspect aspect;
    private final List<MobEffectInstance> effects;

    public InfusedPotionItem(Aspect aspect, MobEffect effect, Properties properties) {
        super(properties);
        this.aspect = aspect;
        this.effects = List.of(new MobEffectInstance(effect, POTION_DURATION));
    }

    public Aspect getAspect() {
        return aspect;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        for (MobEffectInstance instance : effects) {
            entity.addEffect(new MobEffectInstance(instance));
        }
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (!level.isClientSide()) {
            level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_DRINK,
                    SoundSource.PLAYERS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(aspect.getName()));
    }
}
