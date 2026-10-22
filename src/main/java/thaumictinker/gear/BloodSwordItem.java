package thaumictinker.gear;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

 



 
public class BloodSwordItem extends SwordItem {

    public static final UUID SPEED_UUID = UUID.fromString("54ac2ac0-2ded-4d33-b8bd-70e94d1ff65d");

    public BloodSwordItem(Tier tier, Properties properties) {
        super(tier, 10, -2.4F, properties);
    }

    public static boolean isActivated(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("thaumictinker:active");
    }

    public static void setActivated(ItemStack stack, boolean activated) {
        if (activated) {
            stack.getOrCreateTag().putBoolean("thaumictinker:active", true);
        } else if (stack.hasTag()) {
            stack.getTag().remove("thaumictinker:active");
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !level.isClientSide()) {
            boolean activated = isActivated(stack);
            setActivated(stack, !activated);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(activated
                    ? "ttinkerer.ttmisc.bloodSword.deactivateEssentiaHarvest"
                    : "ttinkerer.ttmisc.bloodSword.activateEssentiaHarvest"));
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? ATTRIBUTES : super.getDefaultAttributeModifiers(slot);
    }

    private static final Multimap<Attribute, AttributeModifier> ATTRIBUTES = ImmutableMultimap.<Attribute, AttributeModifier>builder()
            .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Blood sword damage",
                    10.0D, AttributeModifier.Operation.ADDITION))
            .put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Blood sword speed",
                    -2.4D, AttributeModifier.Operation.ADDITION))
            .put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_UUID, "Blood sword movement",
                    0.25D, AttributeModifier.Operation.MULTIPLY_BASE))
            .build();
}
