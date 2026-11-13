package arcaneresearch.common.items.misc;

import arcaneresearch.common.entities.EntityPortal;
import arcaneresearch.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ItemPortalScroll extends Item {
    private static final String TAG_X = "Pos_X";
    private static final String TAG_Y = "Pos_Y";
    private static final String TAG_Z = "Pos_Z";
    private static final String TAG_DIM = "Dim";
    private static final String TAG_W = "Width";
    private static final String TAG_H = "Height";
    private static final float DEFAULT_WIDTH = 2.4F;
    private static final float DEFAULT_HEIGHT = 4.2F;

    public ItemPortalScroll() {
        super(new Properties().stacksTo(1));
    }

    public static boolean hasSavedPosition(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(TAG_X)
                && tag.contains(TAG_Y)
                && tag.contains(TAG_Z);
    }

    public static BlockPos getSavedPosition(ItemStack stack) {
        if (!hasSavedPosition(stack)) return null;
        CompoundTag tag = stack.getOrCreateTag();
        return new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
    }

    public static ResourceKey<Level> getSavedDimension(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_DIM)) return Level.OVERWORLD;
        ResourceLocation dim = parseResourceLocation(tag.getString(TAG_DIM));
        if (dim == null) return Level.OVERWORLD;
        return ResourceKey.create(Registries.DIMENSION, dim);
    }

    private static ResourceLocation parseResourceLocation(String value) {
        if (value == null || value.isEmpty()) return null;
        int sep = value.indexOf(':');
        if (sep >= 0) {
            return ResourceLocation.fromNamespaceAndPath(value.substring(0, sep), value.substring(sep + 1));
        }
        return ResourceLocation.fromNamespaceAndPath("minecraft", value);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return hasSavedPosition(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        BlockPos pos = getSavedPosition(stack);
        if (pos != null) {
            ResourceKey<Level> dim = getSavedDimension(stack);
            tooltip.add(Component.translatable("key.arcaneresearch.scroll.teleport").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                            .withStyle(ChatFormatting.WHITE)));
            tooltip.add(Component.translatable("key.arcaneresearch.scroll.dimension").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(dim.location().toString()).withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.translatable("key.arcaneresearch.scroll.teleport").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("key.arcaneresearch.scroll.not_set").withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (player == null) return InteractionResult.PASS;

        if (!hasSavedPosition(stack)) {
            if (!level.isClientSide) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.putInt(TAG_X, clickedPos.getX());
                tag.putInt(TAG_Y, clickedPos.getY());
                tag.putInt(TAG_Z, clickedPos.getZ());
                tag.putString(TAG_DIM, level.dimension().location().toString());
                tag.putFloat(TAG_W, DEFAULT_WIDTH);
                tag.putFloat(TAG_H, DEFAULT_HEIGHT);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            BlockPos target = getSavedPosition(stack);
            if (target == null) return InteractionResult.FAIL;

            BlockPos spawnPos = clickedPos.relative(context.getClickedFace());

            EntityPortal portal = ModEntities.PORTAL.get().create(level);
            if (portal == null) return InteractionResult.FAIL;

            float width = stack.getOrCreateTag().contains(TAG_W) ? Objects.requireNonNull(stack.getTag()).getFloat(TAG_W) : DEFAULT_WIDTH;
            float height = stack.getOrCreateTag().contains(TAG_H) ? Objects.requireNonNull(stack.getTag()).getFloat(TAG_H) : DEFAULT_HEIGHT;

            portal.setSize(width, height);
            float yaw = Mth.wrapDegrees(Math.round(player.getYRot() / 90f) * 90f);
            portal.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, yaw, 0f);
            portal.setTeleportTarget(target);
            portal.setTeleportDimension(getSavedDimension(stack));
            level.addFreshEntity(portal);

            stack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}