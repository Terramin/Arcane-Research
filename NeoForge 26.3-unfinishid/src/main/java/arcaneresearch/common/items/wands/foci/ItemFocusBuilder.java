package arcaneresearch.common.items.wands.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.api.wands.*;
import arcaneresearch.common.lib.events.ServerEvents;
import arcaneresearch.common.lib.utils.BlockUtils;

import net.minecraft.nbt.NbtOps;
import java.util.ArrayList;

public class ItemFocusBuilder extends ItemFocusBasic implements IArchitectExtended, IFocusPicker {
    private static final AspectList COST = new AspectList().add(Aspect.AIR, 1).add(Aspect.EARTH, 1)
            .add(Aspect.ORDER, 1);

    public ItemFocusBuilder(net.minecraft.world.item.Item.Properties props) {
        super("builder", 8776595, props);
    }

    private static ItemStack createStackedBlock(BlockState bs) {
        if (bs == null || bs.isAir()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(bs.getBlock());
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return COST.copy();
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand) || !(entity instanceof Player player)) {
            return false;
        }
        if (player.isCrouching() && hit instanceof BlockHitResult blockHit
                && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState state = level.getBlockState(blockHit.getBlockPos());
            if (!level.isClientSide() && level.getBlockEntity(blockHit.getBlockPos()) == null) {
                ItemStack picked = new ItemStack(state.getBlock());
                try {
                    if (!state.is(Blocks.BEDROCK)) {
                        ItemStack stacked = createStackedBlock(state);
                        if (stacked != null && !stacked.isEmpty()) {
                            picked = stacked.copy();
                        }
                    }
                } catch (Exception ignored) {
                }
                storePickedBlock(wandstack, picked);
            }
        } else {
            BlockHitResult mop = getArchitectMOP(wandstack, level, entity);
            ItemStack picked = getPickedBlock(wandstack);
            if (!picked.isEmpty() && level.isClientSide()) {
                return true;
            } else if (!picked.isEmpty() && mop != null && mop.getType() == HitResult.Type.BLOCK) {
                ArrayList<BlockPos> blocks = getArchitectBlocks(wandstack, level, mop.getBlockPos(),
                        mop.getDirection(), player);
                for (BlockPos target : blocks) {

                    ServerEvents.addSwapper(level, target, level.getBlockState(target), picked, true, 0, player,
                            true, false, 8038177, true, false, 0, ServerEvents.DEFAULT_PREDICATE, 0.0F);
                }
                return true;
            }
        }
        return false;
    }

    public void storePickedBlock(ItemStack stack, ItemStack picked) {
        if (!stack.isEmpty() && !picked.isEmpty()) {
            // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA (same "picked" key);
            // nested stack via ItemStack.CODEC (save(CompoundTag) removed in 26.x).
            CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            ItemStack.CODEC.encodeStart(arcaneresearch.common.lib.utils.RegistryNbt.ops(), picked.copy()).result().ifPresent(encoded -> {
                root.put("picked", encoded);
            });
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        }
    }

    @Override
    public ItemStack getPickedBlock(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("picked")) {
            ItemStack out = ItemStack.CODEC.parse(arcaneresearch.common.lib.utils.RegistryNbt.ops(), root.get("picked"))
                    .result().orElse(ItemStack.EMPTY);
            if (!out.isEmpty()) {
                return out;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
            default:
                break;
        }
        return null;
    }

    @Override
    public int getMaxAreaSize(ItemStack focusstack) {
        return 0;
    }

    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side,
                                                   Player player) {
        ArrayList<BlockPos> out = new ArrayList<>();
        if (!(stack.getItem() instanceof IWand wand) || player == null) {
            return out;
        }
        int length = 0;
        if (side.getAxis() == Direction.Axis.X) {
            length = (int) Math.abs(pos.getX() + 0.5D - player.getX());
        }
        if (side.getAxis() == Direction.Axis.Y) {
            length = (int) Math.abs(pos.getY() + 0.5D - player.getY());
        }
        if (side.getAxis() == Direction.Axis.Z) {
            length = (int) Math.abs(pos.getZ() + 0.5D - player.getZ());
        }
        length = Math.min(length,
                16 + getUpgradeLevel(wand.getFocusStack(stack), FocusUpgradeType.enlarge) * 8);
        for (int a = 1; a < length; a++) {
            out.add(pos.relative(side, a));
        }
        return out;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, IArchitect.EnumAxis axis) {
        return false;
    }

    @Override
    public BlockHitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity entity) {
        return BlockUtils.getTargetBlock(level, (Entity) entity, false, true, 64.0D);
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }
}
