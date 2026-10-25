package arcaneresearch.common.items.wands.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.client.fx.WandFX;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.wands.WandSounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ItemFocusExcavation extends ItemFocusBasic {
    public static final FocusUpgradeType dowsing = new FocusUpgradeType(
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "foci/dowsing.png"),
            "focus.upgrade.dowsing.name",
            "focus.upgrade.dowsing.text",
            new AspectList().add(Aspect.CRAFT, 1));
    static HashMap<String, Long> soundDelay = new HashMap<>();
    static HashMap<String, Float> breakcount = new HashMap<>();
    static HashMap<String, Integer> lastX = new HashMap<>();
    static HashMap<String, Integer> lastY = new HashMap<>();
    static HashMap<String, Integer> lastZ = new HashMap<>();
    static HashMap<String, Object> beam = new HashMap<>();


    public ItemFocusExcavation() {
        super("excavate", 409606);
    }

    private static void resetTracker(String key) {
        lastX.put(key, Integer.MAX_VALUE);
        lastY.put(key, Integer.MAX_VALUE);
        lastZ.put(key, Integer.MAX_VALUE);
        breakcount.put(key, 0.0F);
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        AspectList cost = new AspectList().add(Aspect.EARTH, 1);
        if (isUpgradedWith(focusstack, FocusUpgradeType.silktouch)) {
            AspectList silk = new AspectList().add(Aspect.AIR, 1).add(Aspect.FIRE, 1).add(Aspect.WATER, 1)
                    .add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1);
            silk.add(cost);
            silk.add(Aspect.EARTH, getUpgradeLevel(focusstack, FocusUpgradeType.enlarge));
            return silk;
        }
        if (isUpgradedWith(focusstack, dowsing)) {
            AspectList dowse = new AspectList().add(Aspect.FIRE, 1).add(Aspect.ORDER, 1);
            dowse.add(cost);
            dowse.add(Aspect.EARTH, getUpgradeLevel(focusstack, FocusUpgradeType.enlarge));
            return dowse;
        }
        cost.add(Aspect.EARTH, getUpgradeLevel(focusstack, FocusUpgradeType.enlarge));
        return cost;
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focusstack) {
        return true;
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        return 0;
    }

    @Override
    public boolean onFocusActivation(ItemStack stack, Level level, LivingEntity caster, HitResult hit, int count) {
        if (!(stack.getItem() instanceof IWand wand) || !(caster instanceof Player player)) {
            return false;
        }
        String name = player.getGameProfile().getName();
        String key = (level.isClientSide ? "R" : "S") + name;
        if (!level.isClientSide) {
            soundDelay.putIfAbsent(key, 0L);
        }
        breakcount.putIfAbsent(key, 0.0F);
        lastX.putIfAbsent(key, Integer.MAX_VALUE);
        lastY.putIfAbsent(key, Integer.MAX_VALUE);
        lastZ.putIfAbsent(key, Integer.MAX_VALUE);

        BlockHitResult mop = BlockUtils.getTargetBlock(level, caster, false);
        Vec3 look = caster.getLookAngle();
        double tx = caster.getX() + look.x * 10.0D;
        double ty = caster.getY() + caster.getEyeHeight(caster.getPose()) + look.y * 10.0D;
        double tz = caster.getZ() + look.z * 10.0D;
        int impact = 0;
        if (mop.getType() == HitResult.Type.BLOCK) {
            tx = mop.getLocation().x;
            ty = mop.getLocation().y;
            tz = mop.getLocation().z;
            impact = 5;
            if (!level.isClientSide && soundDelay.get(key) < System.currentTimeMillis()) {
                level.playSound(null, BlockPos.containing(tx, ty, tz),
                        WandSounds.rumble(), SoundSource.PLAYERS, 0.3F, 1.0F);
                soundDelay.put(key, System.currentTimeMillis() + 1200L);
            }
        } else {
            soundDelay.put(key, 0L);
        }
        if (level.isClientSide) {
            beam.put(key, WandFX.beamCont(level, caster, tx, ty, tz, 2, 65382,
                    false, impact > 0 ? 2.0F : 0.0F, beam.get(key), impact));
        }
        if (mop.getType() == HitResult.Type.BLOCK && player.mayUseItemAt(mop.getBlockPos(), mop.getDirection(), stack)) {
            BlockPos target = mop.getBlockPos();
            BlockState bs = level.getBlockState(target);
            float hardness = bs.getDestroySpeed(level, target);
            if (hardness >= 0.0F) {
                int pot = wand.getFocusPotency(stack);
                float speed = 0.05F + pot * 0.1F;
                if (bs.isAir() || bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.DIRT) || bs.is(Blocks.COARSE_DIRT)
                        || bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND) || bs.is(Blocks.GRAVEL) || bs.is(Blocks.CLAY)
                        || bs.is(BlockTags.LEAVES) || bs.is(BlockTags.PLANKS)
                        || bs.is(BlockTags.LOGS)) {
                    speed = 0.25F + pot * 0.25F;
                }
                if (bs.is(Blocks.OBSIDIAN)) {
                    speed *= 3.0F;
                }
                if (lastX.get(key) == target.getX() && lastY.get(key) == target.getY()
                        && lastZ.get(key) == target.getZ()) {
                    float bc = breakcount.get(key);
                    if (level.isClientSide) {
                        if (bc > 0.0F && !bs.isAir()) {
                            WandFX.excavateFX(level, target, player, (int) (bc / hardness * 9.0F));
                        }
                        breakcount.put(key, bc >= hardness ? 0.0F : bc + speed);
                    } else if (bc >= hardness) {
                        if (excavate(level, stack, player, bs, target)) {
                            for (int a = 0; a < wand.getFocusEnlarge(stack); a++) {
                                breakNeighbour(player, target, bs, stack);
                            }
                        }
                        resetTracker(key);
                    } else {
                        breakcount.put(key, bc + speed);
                    }
                } else {
                    lastX.put(key, target.getX());
                    lastY.put(key, target.getY());
                    lastZ.put(key, target.getZ());
                    breakcount.put(key, 0.0F);
                }
            }
        } else {
            resetTracker(key);
        }
        return true;
    }

    private boolean excavate(Level level, ItemStack stack, Player player, BlockState bs, BlockPos pos) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)
                || !(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        ServerLevel server = serverPlayer.serverLevel();
        if (level != server) {
            return false;
        }

        GameType gt = serverPlayer.gameMode.getGameModeForPlayer();
        if (gt == GameType.SPECTATOR) {
            return false;
        }

        int breakEventResult = ForgeHooks.onBlockBreakEvent(server, gt, serverPlayer, pos);
        if (breakEventResult < 0) {
            return false;
        }

        int fortune = wand.getFocusTreasure(stack);
        boolean silk = isUpgradedWith(wand.getFocusStack(stack), FocusUpgradeType.silktouch);
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack tool = stack.copy();
        if (silk) {
            tool.enchant(Enchantments.SILK_TOUCH, 1);
        } else if (fortune > 0) {
            tool.enchant(Enchantments.BLOCK_FORTUNE, Math.min(fortune, 3));
        }

        LootParams.Builder loot = new LootParams.Builder(server)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, be);
        List<ItemStack> drops = bs.getDrops(loot);
        for (ItemStack drop : drops) {
            Block.popResource(server, pos, drop);
        }

        int exp = bs.getExpDrop(server, server.random, pos, silk ? 0 : fortune, silk ? 1 : 0);
        if (exp > 0) {
            bs.getBlock().popExperience(server, pos, exp);
        }

        server.removeBlock(pos, false);
        server.levelEvent(2001, pos, Block.getId(bs));
        return true;
    }

    private boolean breakNeighbour(LivingEntity caster, BlockPos pos, BlockState bs, ItemStack stack) {
        if (!(caster instanceof Player player)) {
            return false;
        }
        Level level = caster.level();
        List<Direction> faces = new ArrayList<>(List.of(Direction.values()));
        Collections.shuffle(faces);

        for (Direction dir : faces) {
            BlockPos target = pos.relative(dir);
            if (level.getBlockState(target) == bs && excavate(level, stack, player, bs, target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.treasure};
            case 2:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.enlarge};
            case 3:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.treasure, dowsing};
            case 4:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.enlarge};
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        FocusUpgradeType.treasure, FocusUpgradeType.silktouch};
            default:
                break;
        }
        return null;
    }
}
