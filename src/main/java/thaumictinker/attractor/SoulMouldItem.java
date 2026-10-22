package thaumictinker.attractor;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

 


 
public class SoulMouldItem extends Item {

    private static final String PATTERN_TAG = "thaumictinker:soul_pattern";

    public SoulMouldItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static String getPatternName(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(PATTERN_TAG, CompoundTag.TAG_STRING)) {
            return null;
        }
        return stack.getTag().getString(PATTERN_TAG);
    }

    public static String patternOf(LivingEntity entity) {
        return EntityType.getKey(entity.getType()).toString();
    }

    public static Component patternName(String pattern) {
        return EntityType.byString(pattern)
                .<Component>map(EntityType::getDescription)
                .orElseGet(() -> Component.literal(pattern));
    }

    public static void setPattern(ItemStack stack, @Nullable String pattern) {
        if (!stack.hasTag()) {
            if (pattern == null) {
                return;
            }
            stack.getOrCreateTag();
        }
        if (pattern == null) {
            stack.getTag().remove(PATTERN_TAG);
            if (stack.getTag().isEmpty()) {
                stack.setTag(null);
            }
        } else {
            stack.getOrCreateTag().putString(PATTERN_TAG, pattern);
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof Player) {
            return InteractionResult.SUCCESS;
        }
        if (!player.level().isClientSide) {
            setPattern(stack, patternOf(target));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && getPatternName(stack) != null) {
            if (!level.isClientSide) {
                setPattern(stack, null);
            }
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return net.minecraft.world.InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String pattern = getPatternName(stack);
        if (pattern == null || pattern.isEmpty()) {
            tooltip.add(Component.translatable("ttinkerer.ttmisc.soulMould.nonAssigned"));
        } else {
            tooltip.add(Component.translatable("ttinkerer.ttmisc.soulMould.pattern", patternName(pattern)));
        }
    }
}
