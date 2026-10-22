package thaumictinker.awakenedarmor;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import thaumictinker.kami.IchorclothArmorItem;

 




 
public class AwakenedArmorItem extends IchorclothArmorItem {

    public static final String PIECE_DISABLED_TAG = "PieceDisabled";

      
    public static final String ARMOR_TEXTURE = "thaumictinker:textures/model/ichorgem1.png";
    public static final String ARMOR_TEXTURE_OVERLAY = "thaumictinker:textures/model/ichorgem2.png";

    public AwakenedArmorItem(Type type, Properties properties, int visDiscount) {
        super(type, properties, visDiscount);
    }

    @Override
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
                                  EquipmentSlot slot, String type) {
        return type == null ? ARMOR_TEXTURE : ARMOR_TEXTURE_OVERLAY;
    }

    public static boolean isPieceDisabled(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(PIECE_DISABLED_TAG);
    }

    public static void setPieceDisabled(ItemStack stack, boolean disabled) {
        stack.getOrCreateTag().putBoolean(PIECE_DISABLED_TAG, disabled);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        boolean disabled = !isPieceDisabled(stack);
        setPieceDisabled(stack, disabled);
        if (player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            serverPlayer.displayClientMessage(
                    Component.translatable("ttinkerer.ttmisc.awakenedArmor" + (disabled ? 1 : 0)), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (isPieceDisabled(stack)) {
            tooltip.add(Component.translatable("ttinkerer.ttmisc.awakenedArmor1")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
