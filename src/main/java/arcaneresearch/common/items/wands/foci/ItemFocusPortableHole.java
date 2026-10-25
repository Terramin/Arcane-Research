package arcaneresearch.common.items.wands.foci;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.blocks.misc.BlockHole;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.init.ModBlocks;
public class ItemFocusPortableHole extends ItemFocusBasic {
    private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 25).add(Aspect.AIR, 25)
            .add(Aspect.EARTH, 25);
    public ItemFocusPortableHole() {
        super("hole", 594985);
    }
    public static boolean createHole(Level level, BlockPos pos, Direction side, byte count, int max) {
        if (level.isClientSide) {
            return false;
        }
        BlockState bs = level.getBlockState(pos);
        net.minecraft.world.level.block.Block holeBlock;
        try {
            holeBlock = ModBlocks.HOLE.get();
        } catch (Exception e) {
            return false;
        }
        if (level.getBlockEntity(pos) != null
                || BlockUtils.isPortableHoleBlackListed(bs)
                || bs.is(Blocks.BEDROCK) || bs.is(holeBlock)
                || !(bs.isAir() || !bs.canBeReplaced())
                || bs.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        return BlockHole.createHole(level, pos, bs, (short) max, count, side);
    }
    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return COST.copy();
    }
    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand) || !(hit instanceof BlockHitResult mop)
                || !(entity instanceof Player player)) {
            return hit instanceof BlockHitResult;
        }
        net.minecraft.world.level.block.Block holeBlock;
        try {
            holeBlock = ModBlocks.HOLE.get();
        } catch (Exception e) {
            return false;
        }
        int enlarge = wand.getFocusEnlarge(wandstack);
        int maxdis = 33 + enlarge * 8;
        BlockPos pos = mop.getBlockPos();
        int distance = 0;
        for (distance = 0; distance < maxdis; distance++) {
            BlockState bi = level.getBlockState(pos);
            if (BlockUtils.isPortableHoleBlackListed(bi) || bi.is(Blocks.BEDROCK) || bi.is(holeBlock)
                    || bi.isAir() || bi.getDestroySpeed(level, pos) < 0.0F) {
                break;
            }
            pos = pos.relative(mop.getDirection().getOpposite());
        }
        int extend = getUpgradeLevel(wand.getFocusStack(wandstack), FocusUpgradeType.extend);
        short dur = (short) (120 + 60 * extend);
        createHole(level, mop.getBlockPos(), mop.getDirection(), (byte) (distance + 1), dur);
        if (!level.isClientSide) {
            level.playSound(null, mop.getBlockPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge,
                        FocusUpgradeType.extend};
            default:
                break;
        }
        return null;
    }
}
