package arcaneresearch.common.golems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.init.ModEntities;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGolemPlacer extends Item implements ISealDisplayer {

    public ItemGolemPlacer(Item.Properties props) {
        super(props
                .stacksTo(16)
                .rarity(Rarity.UNCOMMON));
    }

    public static ItemStack createGolemStack(Item item, IGolemProperties props, int xp) {
        ItemStack stack = new ItemStack(item);
        
        
        net.minecraft.nbt.CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        tag.putLong("props", props.toLong());
        if (xp > 0) {
            tag.putInt("xp", xp);
        }
        ArcaneresearchInvHelper.setCustomTag(stack, tag);
        return stack;
    }

    public static ItemStack createDefaultStack(Item item) {
        return createGolemStack(item, new GolemProperties(), 0);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
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
        EntityArcaneresearchGolem golem = ModEntities.ARCANERESEARCH_GOLEM.get().create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);

        if (golem != null) {
            golem.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0f, 0.0f);

            golem.setValidSpawn();

            golem.setOwned(true);
            golem.setOwnerUUID(player.getUUID());

            if (ArcaneresearchInvHelper.hasCustomTag(stack)
                    && ArcaneresearchInvHelper.getCustomTag(stack).contains("props")) {
                IGolemProperties props = GolemProperties.fromLong(
                        ArcaneresearchInvHelper.getCustomTag(stack).getLongOr("props", 0L));
                golem.setProperties(props);
            }

            if (ArcaneresearchInvHelper.hasCustomTag(stack)
                    && ArcaneresearchInvHelper.getCustomTag(stack).contains("xp")) {
                golem.setRankXp(ArcaneresearchInvHelper.getCustomTag(stack).getIntOr("xp", 0));
            }

            
            golem.setHomeTo(spawnPos, 32);

            golem.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos),
                    EntitySpawnReason.SPAWN_ITEM_USE, null);

            if (serverLevel.addFreshEntity(golem)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.FAIL;
    }

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        java.util.List<Component> lines = new java.util.ArrayList<>();
        if (ArcaneresearchInvHelper.hasCustomTag(stack)
                && ArcaneresearchInvHelper.getCustomTag(stack).contains("props")) {
            IGolemProperties props = GolemProperties.fromLong(
                    ArcaneresearchInvHelper.getCustomTag(stack).getLongOr("props", 0L));

            if (props.hasTrait(EnumGolemTrait.SMART)) {
                int rank = props.getRank();
                if (rank >= 10) {
                    lines.add(Component.translatable("golem.rank")
                            .append(" " + rank)
                            .withStyle(ChatFormatting.GOLD));
                } else {
                    int xp = ArcaneresearchInvHelper.getCustomTag(stack).contains("xp")
                            ? ArcaneresearchInvHelper.getCustomTag(stack).getIntOr("xp", 0) : 0;
                    int xpNeeded = (rank + 1) * (rank + 1) * EntityArcaneresearchGolem.XP_MULTIPLIER;
                    lines.add(Component.translatable("golem.rank")
                            .append(" " + rank + " ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal("(" + xp + "/" + xpNeeded + ")")
                                    .withStyle(ChatFormatting.DARK_GREEN)));
                }
            }

            lines.add(props.getMaterial().getLocalizedName().copy()
                    .withStyle(ChatFormatting.GREEN));

            for (EnumGolemTrait trait : props.getTraits()) {
                lines.add(Component.literal("-").append(trait.getLocalizedName())
                        .withStyle(ChatFormatting.BLUE));
            }
        }

        super.appendHoverText(stack, context, display, tooltip, flag);
        lines.forEach(tooltip);
    }
}