package arcaneresearch.common.golems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import javax.annotation.Nullable;
import java.util.List;
public class ItemGolemPlacer extends Item implements ISealDisplayer {
    public ItemGolemPlacer() {
        super(new Item.Properties()
                .stacksTo(16)
                .rarity(Rarity.UNCOMMON));
    }
    public static ItemStack createGolemStack(Item item, IGolemProperties props, int xp) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putLong("props", props.toLong());
        if (xp > 0) {
            stack.getTag().putInt("xp", xp);
        }
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
        EntityArcaneresearchGolem golem = ModEntities.ARCANERESEARCH_GOLEM.get().create(serverLevel);
        if (golem != null) {
            golem.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0.0f, 0.0f);
            golem.setValidSpawn();
            golem.setOwned(true);
            golem.setOwnerUUID(player.getUUID());
            if (stack.hasTag() && stack.getTag().contains("props")) {
                IGolemProperties props = GolemProperties.fromLong(stack.getTag().getLong("props"));
                golem.setProperties(props);
            }
            if (stack.hasTag() && stack.getTag().contains("xp")) {
                golem.setRankXp(stack.getTag().getInt("xp"));
            }
            golem.restrictTo(spawnPos, 32);
            golem.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.SPAWN_EGG, null, null);
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains("props")) {
            IGolemProperties props = GolemProperties.fromLong(stack.getTag().getLong("props"));
            if (props.hasTrait(EnumGolemTrait.SMART)) {
                int rank = props.getRank();
                if (rank >= 10) {
                    tooltip.add(Component.translatable("golem.rank")
                            .append(" " + rank)
                            .withStyle(ChatFormatting.GOLD));
                } else {
                    int xp = stack.getTag().contains("xp") ? stack.getTag().getInt("xp") : 0;
                    int xpNeeded = (rank + 1) * (rank + 1) * EntityArcaneresearchGolem.XP_MULTIPLIER;
                    tooltip.add(Component.translatable("golem.rank")
                            .append(" " + rank + " ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal("(" + xp + "/" + xpNeeded + ")")
                                    .withStyle(ChatFormatting.DARK_GREEN)));
                }
            }
            tooltip.add(props.getMaterial().getLocalizedName().copy()
                    .withStyle(ChatFormatting.GREEN));
            for (EnumGolemTrait trait : props.getTraits()) {
                tooltip.add(Component.literal("-").append(trait.getLocalizedName())
                        .withStyle(ChatFormatting.BLUE));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
