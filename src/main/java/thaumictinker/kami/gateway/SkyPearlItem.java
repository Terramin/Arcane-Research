package thaumictinker.kami.gateway;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

 


 
public class SkyPearlItem extends Item {

    public SkyPearlItem(Properties properties) {
        super(properties);
    }

    public static boolean isAttuned(ItemStack stack) {
        return stack.getItem() instanceof SkyPearlItem && location(stack) != null;
    }

    @Nullable
    public static PearlLocation location(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("PearlLocation", Tag.TAG_COMPOUND)) {
            return null;
        }
        return PearlLocation.load(tag.getCompound("PearlLocation"));
    }

    public static void attune(ItemStack stack, PearlLocation location) {
        stack.getOrCreateTag().put("PearlLocation", location.save());
    }

    public static void clear(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove("PearlLocation");
        }
    }

    private static void chime(ServerLevel level, double x, double y, double z) {
        level.playSound(null, x, y, z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isAttuned(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        if (level instanceof ServerLevel serverLevel && !isAttuned(stack)
                && level.getBlockState(context.getClickedPos()).is(GatewayFeature.WARP_GATE)) {
            attune(stack, new PearlLocation(serverLevel.dimension(), context.getClickedPos()));
            Player player = context.getPlayer();
            if (player != null) {
                chime(serverLevel, player.getX(), player.getY(), player.getZ());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && isAttuned(stack)) {
            if (level instanceof ServerLevel serverLevel) {
                chime(serverLevel, player.getX(), player.getY(), player.getZ());
                clear(stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }
}
