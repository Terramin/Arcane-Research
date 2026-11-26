package arcaneresearch.common.items.wands.foci;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.api.wands.FocusItem;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IArchitectExtended;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXWardSparkle;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.wands.WandSounds;
import arcaneresearch.common.wards.WardControlState;
import arcaneresearch.common.wards.WardData;
import arcaneresearch.common.wards.WardEntry;
import arcaneresearch.common.wards.WardManager;
import arcaneresearch.common.wards.WardStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wand Focus: Warding. Port of Thaumcraft's ItemFocusWarding: right-clicking a
 * solid block seals it; right-clicking an owned seal lifts it. The architect
 * upgrade extends the effect to a connected area.
 */
public class ItemFocusWarding extends FocusItem implements IArchitectExtended {
    private static final AspectList COST = new AspectList()
            .add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.WATER, 10);
    private static final Map<String, Long> DELAY = new HashMap<>();
    private static final long DELAY_MS = 500L;
    private static final int SPARKLE_COLOR = 0xFCA000;
    private static final int MAX_GROUP = 4096;

    public ItemFocusWarding() {
        super("warding", 16771535);
    }

    @Override
    public String getSortingHelper(ItemStack focusstack) {
        return "BWA" + super.getSortingHelper(focusstack);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return COST.copy();
    }

    @Override
    public boolean handlesOwnVisCost(ItemStack focusstack) {
        // TC4 ItemWandCasting never pre-charges; each focus pays per block,
        // and lifting a ward costs nothing at all.
        return true;
    }

    /** TC4 displays this focus' vis cost divided by 100 (e.g. 25 -> 0.25). */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        AspectList al = getVisCost(stack);
        if (al != null && al.size() > 0) {
            tooltip.add(Component.translatable(isVisCostPerTick(stack)
                    ? "item.Focus.cost2" : "item.Focus.cost1"));
            java.text.DecimalFormat format = new java.text.DecimalFormat("#####.##");
            for (Aspect aspect : al.getAspectsSortedByName()) {
                String amount = format.format(al.getAmount(aspect) / 100.0);
                tooltip.add(Component.literal(" \u00A7" + aspect.getChatcolor()
                        + aspect.getName() + "\u00A7r x " + amount));
            }
        }
        addFocusInformation(stack, level, tooltip, flag);
    }

    @Override
    public void addFocusInformation(ItemStack focusstack, @Nullable Level level,
                                     List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.arcaneresearch.wand_focus_warding.tooltip")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.arcaneresearch.wand_focus_warding.tooltip.group")
                .withStyle(ChatFormatting.DARK_PURPLE));
        super.addFocusInformation(focusstack, level, tooltip, flag);
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity,
                                      HitResult hit, int useCount) {
        if (level.isClientSide) {
            return true;
        }
        if (!(level instanceof ServerLevel serverLevel)
                || !(entity instanceof Player player)
                || !(wandstack.getItem() instanceof IFocusHost)
                || !(hit instanceof BlockHitResult blockHit)
                || hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        player.swing(InteractionHand.MAIN_HAND);

        BlockPos pos = blockHit.getBlockPos();
        String key = pos.getX() + ":" + pos.getY() + ":" + pos.getZ() + ":"
                + WardManager.dimensionKey(level);
        long now = System.currentTimeMillis();
        Long last = DELAY.get(key);
        if (last != null && last > now) {
            return false;
        }
        if (DELAY.size() > 4096) {
            DELAY.clear();
        }
        DELAY.put(key, now + DELAY_MS);

        WardStorage storage = WardManager.get(serverLevel);
        // The registry is the single source of truth; no block is ever replaced.
        WardEntry originEntry = WardManager.getEntry(level, pos);
        boolean unwardOnly = originEntry != null && originEntry.isOwnedBy(player);

        // Shift+Ctrl + right-click lifts the whole connected group of the
        // player's wards, regardless of the architect upgrade / area size.
        boolean removeGroup = unwardOnly && player.isCrouching()
                && WardControlState.isControlDown(player);
        List<BlockPos> targets;
        if (removeGroup) {
            targets = WardData.connectedGroup(storage.entries(), pos, player.getUUID(), MAX_GROUP);
        } else {
            targets = getArchitectBlocks(wandstack, level, pos,
                    blockHit.getDirection(), player);
            if (targets.isEmpty()) {
                targets.add(pos);
            }
        }

        boolean changed = false;
        for (BlockPos target : targets) {
            WardEntry entry = WardManager.getEntry(level, target);
            if (!unwardOnly && entry == null && !storage.isWarded(target) && canWard(level, target)
                    && wandstack.getItem() instanceof IFocusHost wand
                    && !wand.consumeAllVis(wandstack, player, COST, false, false)) {
                break; // TC4 stops charging further blocks when vis runs out
            }
            boolean changedHere = false;
            if (entry != null && entry.isOwnedBy(player)) {
                boolean removed = storage.remove(target, player)
                        || storage.get(target) == null; // entry already pruned
                if (removed) {
                    WardManager.syncRemove(serverLevel, target, entry);
                    changedHere = true;
                }
            } else if (!unwardOnly && entry == null && canWard(level, target)
                    && storage.add(target, player)) {
                WardManager.syncAdd(serverLevel, target, storage.get(target));
                changedHere = true;
            }
            if (changedHere) {
                PacketHandler.sendToNear(
                        new PacketFXWardSparkle(target, SPARKLE_COLOR),
                        new PacketDistributor.TargetPoint(target.getX() + 0.5, target.getY() + 0.5,
                                target.getZ() + 0.5, 32.0, level.dimension()));
                changed = true;
            }
        }

        if (changed) {
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    WandSounds.zap(), SoundSource.PLAYERS, 0.25F, 1.0F);
        }
        // This focus charges vis itself in the loop above (nothing when unwarding).
        return changed;
    }

    /**
     * TC4 only accepts simple, full opaque cubes. Blocks with a block entity
     * (chests and other complex blocks) are accepted as a registry-only ward so
     * their data survives; everything else with a partial/transparent shape is
     * rejected, exactly as in the original.
     */
    private static boolean canWard(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        if (level.getBlockEntity(pos) != null) {
            return true;
        }
        // TC4's isBlockSolid(..., true) rejects translucent / partial cubes.
        if (state.is(net.minecraft.tags.BlockTags.LEAVES)
                || state.getBlock() instanceof net.minecraft.world.level.block.GlassBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock) {
            return false;
        }
        return state.isSolid()
                && state.getShape(level, pos).equals(net.minecraft.world.phys.shapes.Shapes.block())
                && state.getLightBlock(level, pos) == 15;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal};
            case 2:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.architect};
            case 3:
            case 4:
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
            default:
                return null;
        }
    }

    @Override
    public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
        return !type.equals(FocusUpgradeType.enlarge) || isUpgradedWith(focusstack, FocusUpgradeType.architect);
    }

    @Override
    public int getMaxAreaSize(ItemStack focusstack) {
        return 3 + getUpgradeLevel(focusstack, FocusUpgradeType.enlarge);
    }

    private int area(ItemStack wand, String key, int fallback, ItemStack focus) {
        int max = getMaxAreaSize(focus);
        CompoundTag tag = wand.getTag();
        int value = tag != null && tag.contains(key) ? tag.getInt(key) : fallback;
        return Math.max(0, Math.min(value, max));
    }

    @Override
    public BlockHitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity entity) {
        return BlockUtils.getTargetBlock(level, entity, false, true, 5.0D);
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return true;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side,
                             IArchitect.EnumAxis axis) {
        CompoundTag tag = stack.getTag();
        int dim = tag != null && tag.contains("aread") ? tag.getInt("aread") : 0;
        if (dim == 0) {
            return true;
        }
        // Original TC4 showAxis(): dim 1 = X size, 2 = Z size, 3 = Y size,
        // remapped per face so the visible arrow matches the edited axis.
        int axisIndex;
        switch (side.getAxis()) {
            case Y:
                axisIndex = (axis == IArchitect.EnumAxis.X) ? 1
                        : (axis == IArchitect.EnumAxis.Z) ? 2 : 3;
                break;
            case Z:
                axisIndex = (axis == IArchitect.EnumAxis.Y) ? 1
                        : (axis == IArchitect.EnumAxis.X) ? 2 : 3;
                break;
            default:
                axisIndex = (axis == IArchitect.EnumAxis.Y) ? 1
                        : (axis == IArchitect.EnumAxis.Z) ? 2 : 3;
                break;
        }
        return axisIndex == dim;
    }

    /** Connected blocks around the target, with TC4's per-axis sizes and tile mode. */
    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos origin,
                                                   Direction side, Player player) {
        ArrayList<BlockPos> out = new ArrayList<>();
        if (origin == null || level == null) {
            return out;
        }
        ItemStack focus = stack.getItem() instanceof IFocusHost host
                ? host.getFocusStack(stack) : stack;
        int sizeX = 0;
        int sizeY = 0;
        int sizeZ = 0;
        if (isUpgradedWith(focus, FocusUpgradeType.architect)) {
            int max = getMaxAreaSize(focus);
            sizeX = area(stack, "areax", max, focus);
            sizeY = area(stack, "areay", max, focus);
            sizeZ = area(stack, "areaz", max, focus);
        }
        WardEntry originWard = WardManager.getEntry(level, origin);
        boolean tiles = originWard != null && originWard.isOwnedBy(player);

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        int limit = 1024;
        while (!queue.isEmpty() && out.size() < limit) {
            BlockPos current = queue.removeFirst();
            boolean xFace = side.getAxis() == Direction.Axis.X;
            int dx = Math.abs(current.getX() - origin.getX());
            int dy = Math.abs(current.getY() - origin.getY());
            int dz = Math.abs(current.getZ() - origin.getZ());
            if (xFace ? (dy > sizeX || dz > sizeZ || dx > sizeY)
                    : (side.getAxis() == Direction.Axis.Z
                            // TC4 passes (sizeZ, sizeY, sizeX) for Z faces.
                            ? (dx > sizeZ || dy > sizeX || dz > sizeY)
                            : (dx > sizeX || dy > sizeY || dz > sizeZ))) {
                continue;
            }
            WardEntry entry = WardManager.getEntry(level, current);
            if (tiles) {
                if (entry == null || !entry.isOwnedBy(player)) {
                    continue;
                }
            } else if (entry != null || !canWard(level, current)) {
                continue;
            }
            out.add(current);
            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return out;
    }
}
