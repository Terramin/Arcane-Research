package arcaneresearch.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.entities.EntityFallingTaint;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSwarm;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModSounds;

import java.util.Collections;
import java.util.List;

public class BlockTaint extends Block implements ITaintBlock {

    private final TaintType type;

    public BlockTaint(BlockBehaviour.Properties props, TaintType type) {
        super(props
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(10.0f, 100.0f)
                .sound(ModSounds.GORE_TYPE)
                .randomTicks());
        this.type = type;
    }

    public static boolean canFallBelow(Level level, BlockPos pos) {
        BlockState bs = level.getBlockState(pos);
        Block block = bs.getBlock();

        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                for (int yy = -1; yy <= 1; yy++) {
                    BlockState checkState = level.getBlockState(pos.offset(xx, yy, zz));
                    if (checkState.is(net.minecraft.tags.BlockTags.LOGS)) {
                        return false;
                    }
                }
            }
        }

        if (bs.isAir()) return true;
        if (block == Blocks.FIRE) return true;
        if (bs.canBeReplaced()) return true;
        if (bs.liquid()) return true;

        if (block instanceof BlockTaintFibre) return true;

        if (ModBlocks.FLUX_GOO != null && block == ModBlocks.FLUX_GOO.get()) {
            return bs.getValue(BlockFluxGoo.LEVEL) < 4;
        }

        return false;
    }

    public TaintType getTaintType() {
        return type;
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        switch (type) {
            case ROCK -> {

                if (ModBlocks.POROUS_STONE != null) {
                    level.setBlockAndUpdate(pos, ModBlocks.POROUS_STONE.get().defaultBlockState());
                } else {
                    level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
                }
            }
            case SOIL -> level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            case CRUST, GEYSER -> {
                if (ModBlocks.FLUX_GOO != null) {
                    level.setBlockAndUpdate(pos, ModBlocks.FLUX_GOO.get().defaultBlockState());
                } else {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        if (!TaintHelper.isNearTaintSeed(level, pos) && random.nextInt(10) == 0) {
            die(level, pos, state);
            return;
        }

        switch (type) {
            case ROCK -> {

                TaintHelper.spreadFibres(level, pos);
            }
            case CRUST -> {

                if (tryToFall(level, pos, pos)) {
                    return;
                }

                if (level.isEmptyBlock(pos.above())) {
                    Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    boolean doIt = true;

                    for (int a = 1; a < 4; a++) {
                        if (!level.isEmptyBlock(pos.relative(dir).below(a))) {
                            doIt = false;
                            break;
                        }
                        if (level.getBlockState(pos.below(a)).getBlock() != this) {
                            doIt = false;
                            break;
                        }
                    }

                    if (doIt) {
                        tryToFall(level, pos, pos.relative(dir));
                    }
                }
            }
            case GEYSER -> {

                if (random.nextFloat() < 0.2f &&
                        level.hasNearbyAlivePlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 32.0) &&
                        EntityUtils.getEntitiesInRange(
                                level, pos, 32.0, EntityTaintSwarm.class).isEmpty()) {
                    EntityTaintSwarm swarm = new EntityTaintSwarm(level);
                    // NEOFORGE_PORT (26.3): Forge moveTo(x, y, z, yaw, pitch) == snapTo (also sets old pos/rot).
                    swarm.snapTo(pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, random.nextInt(360), 0.0f);
                    level.addFreshEntity(swarm);
                } else if (AuraHandler.getFlux(level, pos) < 2.0f) {

                    AuraHelper.polluteAura(level, pos, 0.25f, true);
                }
            }
            default -> {

            }
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living &&
                !living.isInvertedHealAndHarm() && level.getRandom().nextInt(250) == 0) {

            if (ModEffects.FLUX_TAINT != null) {
                living.addEffect(new MobEffectInstance(ModEffects.FLUX_TAINT, 200, 0, false, true));
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int eventId, int eventParam) {
        if (eventId == 1) {
            if (level.isClientSide()) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS,
                        0.1f, 0.9f + level.getRandom().nextFloat() * 0.2f, false);
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, eventId, eventParam);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {

        if (type == TaintType.ROCK) {
            RandomSource random = builder.getLevel().getRandom();
            int fortune = 0;
            // NEOFORGE_PORT: TOOL is ContextKey<ItemInstance> now (implemented by
            // ItemStack at runtime); FORTUNE resolved via holder (BLOCK_FORTUNE gone).
            net.minecraft.world.item.ItemInstance toolInstance = builder.getOptionalParameter(
                    net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL);
            if (toolInstance instanceof ItemStack tool && !tool.isEmpty()) {
                fortune = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                        builder.getLevel().registryAccess()
                                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),
                        tool);
            }
            int rr = random.nextInt(15) + fortune;
            if (rr > 13) {
                return Collections.singletonList(ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX));
            }
        }
        return Collections.emptyList();
    }

    private boolean tryToFall(Level level, BlockPos pos, BlockPos fallPos) {
        if (!BlockTaintFibre.isOnlyAdjacentToTaint(level, pos)) {
            return false;
        }

        if (canFallBelow(level, fallPos.below()) && fallPos.getY() >= level.getMinY()) {
            int range = 32;

            if (level.isAreaLoaded(fallPos, range)) {
                if (!level.isClientSide()) {
                    EntityFallingTaint fallingTaint = new EntityFallingTaint(
                            level,
                            fallPos.getX() + 0.5,
                            fallPos.getY() + 0.5,
                            fallPos.getZ() + 0.5,
                            level.getBlockState(pos),
                            pos);
                    level.addFreshEntity(fallingTaint);
                    return true;
                }
            } else {

                level.removeBlock(pos, false);
                BlockPos landPos = fallPos;
                while (canFallBelow(level, landPos.below()) && landPos.getY() > level.getMinY()) {
                    landPos = landPos.below();
                }
                if (landPos.getY() > level.getMinY()) {
                    level.setBlockAndUpdate(landPos, defaultBlockState());
                }
            }
        }
        return false;
    }

    public enum TaintType {
        SOIL,
        ROCK,
        CRUST,
        GEYSER
    }
}
