package arcaneresearch.common.items.wands.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
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

public class ItemFocusExcavation extends ItemFocusBasic {
    public static FocusUpgradeType dowsing = new FocusUpgradeType(
            Identifier.fromNamespaceAndPath("arcaneresearch", "foci/dowsing.png"), "focus.upgrade.dowsing.name",
            "focus.upgrade.dowsing.text", new AspectList().add(Aspect.CRAFT, 1));
    static HashMap<String, Long> soundDelay = new HashMap<>();
    static HashMap<String, Float> breakcount = new HashMap<>();
    static HashMap<String, Integer> lastX = new HashMap<>();
    static HashMap<String, Integer> lastY = new HashMap<>();
    static HashMap<String, Integer> lastZ = new HashMap<>();
    static HashMap<String, Object> beam = new HashMap<>();
    public ItemFocusExcavation(net.minecraft.world.item.Item.Properties props) {
        super("excavate", 409606, props);
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
        String name = player.getName().getString();
        String key = (level.isClientSide() ? "R" : "S") + name;
        soundDelay.putIfAbsent(key, 0L);
        breakcount.putIfAbsent(key, 0.0F);
        lastX.putIfAbsent(key, 0);
        lastY.putIfAbsent(key, 0);
        lastZ.putIfAbsent(key, 0);

        BlockHitResult mop = BlockUtils.getTargetBlock(level, caster, false);
        Vec3 look = caster.getLookAngle();
        double tx = caster.getX() + look.x * 10.0D;
        double ty = caster.getY() + caster.getEyeHeight() + look.y * 10.0D;
        double tz = caster.getZ() + look.z * 10.0D;
        int impact = 0;
        if (mop != null && mop.getType() == HitResult.Type.BLOCK) {
            tx = mop.getLocation().x;
            ty = mop.getLocation().y;
            tz = mop.getLocation().z;
            impact = 5;
            if (!level.isClientSide() && soundDelay.get(key) < System.currentTimeMillis()) {
                level.playSound(null, BlockPos.containing(tx, ty, tz),
                        WandSounds.rumble(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.3F, 1.0F);
                soundDelay.put(key, System.currentTimeMillis() + 1200L);
            }
        } else {
            soundDelay.put(key, 0L);
        }
        if (level.isClientSide()) {
            beam.put(key, WandFX.beamCont(level, caster, tx, ty, tz, 2, 65382,
                    false, impact > 0 ? 2.0F : 0.0F, beam.get(key), impact));
        }
        if (mop != null && mop.getType() == HitResult.Type.BLOCK
                && player.mayUseItemAt(mop.getBlockPos(), mop.getDirection(), stack)) {
            BlockPos target = mop.getBlockPos();
            BlockState bs = level.getBlockState(target);
            float hardness = bs.getDestroySpeed(level, target);
            if (hardness >= 0.0F) {
                int pot = wand.getFocusPotency(stack);
                float speed = 0.05F + pot * 0.1F;
                if (bs.isAir() || bs.is(Blocks.GRASS_BLOCK) || bs.is(Blocks.DIRT) || bs.is(Blocks.COARSE_DIRT)
                        || bs.is(Blocks.SAND) || bs.is(Blocks.RED_SAND) || bs.is(Blocks.GRAVEL) || bs.is(Blocks.CLAY)
                        || bs.is(net.minecraft.tags.BlockTags.LEAVES) || bs.is(net.minecraft.tags.BlockTags.PLANKS)
                        || bs.is(net.minecraft.tags.BlockTags.LOGS)) {
                    speed = 0.25F + pot * 0.25F;
                }
                if (bs.is(Blocks.OBSIDIAN)) {
                    speed *= 3.0F;
                }
                if (lastX.get(key) == target.getX() && lastY.get(key) == target.getY()
                        && lastZ.get(key) == target.getZ()) {
                    float bc = breakcount.get(key);
                    if (level.isClientSide()) {
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
                        lastX.put(key, Integer.MAX_VALUE);
                        lastY.put(key, Integer.MAX_VALUE);
                        lastZ.put(key, Integer.MAX_VALUE);
                        breakcount.put(key, 0.0F);
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
            lastX.put(key, Integer.MAX_VALUE);
            lastY.put(key, Integer.MAX_VALUE);
            lastZ.put(key, Integer.MAX_VALUE);
            breakcount.put(key, 0.0F);
        }
        return true;
    }

    private boolean excavate(Level level, ItemStack stack, Player player, BlockState bs, BlockPos pos) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)
                || !(stack.getItem() instanceof IWand wand)) {
            return false;
        }
        ServerLevel server = serverPlayer.level();
        // NEOFORGE_PORT: ForgeHooks.onBlockBreakEvent removed; post BreakBlockEvent
        // directly (cancel -> abort), like BlockUtils.harvestBlock.
        var breakEvent = new net.neoforged.neoforge.event.level.block.BreakBlockEvent(
                server, pos, bs, serverPlayer);
        // NEOFORGE_PORT (26.3): Forge's onBlockBreakEvent pre-canceled the event for spectators and
        // adventure players without a can_break match; same pre-cancel as CommonHooks.fireBlockBreak
        // (its canDestroyBlock(main hand) part is skipped: Forge's canAttackBlock was true for wands).
        breakEvent.setCanceled(serverPlayer.blockActionRestricted(server, pos,
                serverPlayer.gameMode.getGameModeForPlayer()));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(breakEvent);
        if (breakEvent.isCanceled()) {
            return false;
        }
        int fortune = wand.getFocusTreasure(stack);
        boolean silk = isUpgradedWith(wand.getFocusStack(stack), FocusUpgradeType.silktouch);
        BlockEntity be = level.getBlockEntity(pos);
        var enchantments = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack tool = stack.copy();
        if (silk) {
            tool.enchant(enchantments.getOrThrow(Enchantments.SILK_TOUCH), 1);
        } else if (fortune > 0) {
            tool.enchant(enchantments.getOrThrow(Enchantments.FORTUNE), Math.min(fortune, 3));
        }
        java.util.List<ItemStack> drops = Block.getDrops(bs, server, pos, be, player, tool);
        for (ItemStack drop : drops) {
            Block.popResource(server, pos, drop);
        }
        int exp = bs.getBlock().getExpDrop(bs, server, pos, be, player, tool);
        if (silk) {
            exp = 0;
        }
        server.removeBlock(pos, false);
        server.levelEvent(null, 2001, pos, Block.getId(bs));
        if (exp > 0) {
            bs.getBlock().popExperience(server, pos, exp);
        }
        return true;
    }

    boolean breakNeighbour(LivingEntity caster, BlockPos pos, BlockState bs, ItemStack stack) {
        if (!(caster instanceof Player player)) {
            return false;
        }
        ArrayList<Direction> faces = new ArrayList<>();
        faces.add(Direction.DOWN);
        faces.add(Direction.EAST);
        faces.add(Direction.NORTH);
        faces.add(Direction.SOUTH);
        faces.add(Direction.UP);
        faces.add(Direction.WEST);
        Collections.shuffle(faces);
        Collections.shuffle(faces);
        for (Direction dir : faces) {
            if (caster.level().getBlockState(pos.relative(dir)) == bs
                    && excavate(caster.level(), stack, player, bs, pos.relative(dir))) {
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
