package arcaneresearch.common.golems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.init.ModEntities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemGolemPlacer extends Item implements ISealDisplayer {

    public ItemGolemPlacer() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    public static ItemStack createGolemStack(Item item, IGolemProperties props, int xp) {
        Objects.requireNonNull(props, "props");
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong("props", props.toLong());
        if (xp > 0) {
            tag.putInt("xp", xp);
        }
        return stack;
    }

    public static ItemStack createDefaultStack(Item item) {
        return createGolemStack(item, new GolemProperties(), 0);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        BlockState blockState = level.getBlockState(pos);
        if (!blockState.isSolid()) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        BlockPos spawnPos = pos.relative(side);
        if (!player.mayUseItemAt(spawnPos, side, stack)) {
            return InteractionResult.FAIL;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        EntityArcaneresearchGolem golem = ModEntities.ARCANERESEARCH_GOLEM.get().create(serverLevel);
        if (golem == null) {
            return InteractionResult.FAIL;
        }

        golem.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0f, 0.0f);
        golem.setValidSpawn();
        golem.setOwned(true);
        golem.setOwnerUUID(player.getUUID());

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("props")) {
                IGolemProperties props = GolemProperties.fromLong(tag.getLong("props"));
                golem.setProperties(props);
            }
            if (tag.contains("xp")) {
                golem.setRankXp(tag.getInt("xp"));
            }
        }

        golem.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.SPAWN_EGG, null, null);
        golem.restrictTo(spawnPos, 32);

        if (serverLevel.addFreshEntity(golem)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("props")) {
            IGolemProperties props = GolemProperties.fromLong(tag.getLong("props"));
            if (props.hasTrait(EnumGolemTrait.SMART)) {
                int rank = props.getRank();
                if (rank >= 10) {
                    tooltip.add(Component.translatable("golem.rank")
                            .append(" " + rank)
                            .withStyle(ChatFormatting.GOLD));
                } else {
                    int xp = tag.contains("xp") ? tag.getInt("xp") : 0;
                    long xpNeeded = (long) (rank + 1) * (rank + 1) * EntityArcaneresearchGolem.XP_MULTIPLIER;
                    tooltip.add(Component.translatable("golem.rank")
                            .append(" " + rank + " ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal("(" + xp + "/" + xpNeeded + ")")
                                    .withStyle(ChatFormatting.DARK_GREEN)));
                }
            }
            if (props.getMaterial() != null && props.getMaterial().getLocalizedName() != null) {
                tooltip.add(props.getMaterial().getLocalizedName().copy()
                        .withStyle(ChatFormatting.GREEN));
            }
            for (EnumGolemTrait trait : props.getTraits()) {
                if (trait != null) {
                    tooltip.add(Component.literal("-").append(trait.getLocalizedName())
                            .withStyle(ChatFormatting.BLUE));
                }
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}