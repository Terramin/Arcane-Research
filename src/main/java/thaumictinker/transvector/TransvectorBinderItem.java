package thaumictinker.transvector;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

 


 
public class TransvectorBinderItem extends Item {

    private static final String ORIGIN_TAG = "BinderOrigin";

    public TransvectorBinderItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static GlobalPos origin(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ORIGIN_TAG)) {
            return null;
        }
        CompoundTag origin = tag.getCompound(ORIGIN_TAG);
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(origin.getString("dim")));
        return GlobalPos.of(dimension, BlockPos.of(origin.getLong("pos")));
    }

    public static void setOrigin(ItemStack stack, @Nullable GlobalPos givenOrigin) {
        if (givenOrigin == null) {
            if (stack.hasTag()) {
                stack.getTag().remove(ORIGIN_TAG);
            }
            return;
        }
        CompoundTag origin = new CompoundTag();
        origin.putString("dim", givenOrigin.dimension().location().toString());
        origin.putLong("pos", givenOrigin.pos().asLong());
        stack.getOrCreateTag().put(ORIGIN_TAG, origin);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();
        BlockEntity clickedEntity = level.getBlockEntity(clicked);
        GlobalPos boundOrigin = origin(stack);
        if (boundOrigin == null) {
            if (clickedEntity instanceof TransvectorBlockEntity) {
                setOrigin(stack, GlobalPos.of(level.dimension(), clicked));
                playSound(level, clicked);
                message(player, "set");
            } else {
                message(player, "notinterf");
            }
            return InteractionResult.SUCCESS;
        }
        BlockEntity originEntity = boundOrigin.dimension().equals(level.dimension()) ? level.getBlockEntity(boundOrigin.pos()) : null;
        if (!(originEntity instanceof TransvectorBlockEntity transvector)) {
            setOrigin(stack, null);
            message(player, "notpresent");
            return InteractionResult.SUCCESS;
        }
        if (clickedEntity instanceof TransvectorInterfaceBlockEntity && transvector instanceof TransvectorInterfaceBlockEntity) {
            message(player, "interffail");
            return InteractionResult.SUCCESS;
        }
        if (!transvector.isInRange(clicked)) {
            message(player, "toofar");
            return InteractionResult.SUCCESS;
        }
        transvector.bind(clicked);
        setOrigin(stack, null);
        playSound(level, clicked);
        message(player, "complete");
        return InteractionResult.SUCCESS;
    }

    private static void playSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void message(Player player, String key) {
        player.sendSystemMessage(Component.translatable("block.thaumictinker.connector." + key));
    }
}
