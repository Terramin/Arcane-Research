package arcaneresearch.common.tiles.devices;

import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.*;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.config.ConfigAspects;
import arcaneresearch.common.menu.slot.PotionSlot;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TilePotionSprayer extends TileArcaneresearchInventory implements IAspectContainer, IEssentiaTransport, WorldlyContainer {

    private static final int[] SLOTS = {0};
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MAX_CHARGES = 8;
    private static final int SUCTION_AMOUNT = 128;

    public AspectList recipe = new AspectList();
    public AspectList recipeProgress = new AspectList();
    public int charges = 0;
    public int color = 0x333333;

    private int counter = 0;
    private boolean activated = false;
    private int venting = 0;
    private Aspect currentSuction = null;

    public TilePotionSprayer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);
    }

    public TilePotionSprayer(BlockPos pos, BlockState state) {
        this(ModBlockEntities.POTION_SPRAYER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TilePotionSprayer tile) {
        tile.counter++;
        if (tile.counter % 5 == 0) {
            tile.currentSuction = null;
            if (!tile.getItem(0).isEmpty() && tile.charges < MAX_CHARGES) {
                boolean done = true;
                Aspect[] aspects = tile.recipe.getAspectsSortedByName();
                for (Aspect aspect : aspects) {
                    if (tile.recipeProgress.getAmount(aspect) < tile.recipe.getAmount(aspect)) {
                        tile.currentSuction = aspect;
                        done = false;
                        break;
                    }
                }
                if (done) {
                    tile.recipeProgress = new AspectList();
                    tile.charges++;
                    tile.syncTile(false);
                } else if (tile.currentSuction != null) {
                    tile.fill();
                }
            }
        }

        boolean isEnabled = state.hasProperty(BlockStateProperties.ENABLED)
                ? state.getValue(BlockStateProperties.ENABLED)
                : !level.hasNeighborSignal(pos);

        if (!isEnabled) {
            if (!tile.activated && tile.charges > 0) {
                tile.charges--;
                List<MobEffectInstance> effects = PotionUtils.getMobEffects(tile.getItem(0));
                if (!effects.isEmpty()) {
                    Direction facing = tile.getFacing();
                    BlockPos targetPos = pos.relative(facing, 2);
                    AABB box = new AABB(targetPos).inflate(1);
                    List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box);
                    for (LivingEntity target : targets) {
                        if (!target.isAlive()) continue;
                        for (MobEffectInstance effect : effects) {
                            MobEffect potion = effect.getEffect();
                            if (potion.isInstantenous()) {
                                potion.applyInstantenousEffect(null, null, target, effect.getAmplifier(), 1.0);
                            } else {
                                target.addEffect(new MobEffectInstance(potion, effect.getDuration(), effect.getAmplifier()));
                            }
                        }
                    }
                }
                level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS,
                        0.25f, 2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
                level.blockEvent(pos, state.getBlock(), 0, 0);
                tile.syncTile(false);
            }
            tile.activated = true;
        } else if (tile.activated) {
            tile.activated = false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TilePotionSprayer tile) {
        if (tile.venting <= 0) return;
        tile.venting--;
        Direction facing = tile.getFacing();
        double baseX = pos.getX() + 0.5 + facing.getStepX() / 2.0;
        double baseY = pos.getY() + 0.5 + facing.getStepY() / 2.0;
        double baseZ = pos.getZ() + 0.5 + facing.getStepZ() / 2.0;
        double pushX = facing.getStepX() * 0.25;
        double pushY = facing.getStepY() * 0.25;
        double pushZ = facing.getStepZ() * 0.25;
        int count = tile.venting / 2;
        for (int a = 0; a < count; a++) {
            float fx = 0.1f - level.random.nextFloat() * 0.2f;
            float fy = 0.1f - level.random.nextFloat() * 0.2f;
            float fz = 0.1f - level.random.nextFloat() * 0.2f;
            float fx2 = (float) (level.random.nextGaussian() * 0.06);
            float fy2 = (float) (level.random.nextGaussian() * 0.06);
            float fz2 = (float) (level.random.nextGaussian() * 0.06);
            FXDispatcher.INSTANCE.drawVentParticles2(
                    baseX + fx, baseY + fy, baseZ + fz,
                    fx2 + pushX, fy2 + pushY, fz2 + pushZ,
                    tile.color, 4.0f);
        }
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.NORTH;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 0) {
            if (level != null && level.isClientSide) {
                venting = 15;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    private void fill() {
        if (currentSuction == null) return;
        Direction facing = getFacing();
        int ourSuction = getSuctionAmount(null);
        for (int y = 0; y <= 1; y++) {
            BlockPos aboveY = worldPosition.above(y);
            for (Direction dir : DIRECTIONS) {
                if (dir == facing) continue;
                BlockEntity be = ArcaneresearchApiHelper.getConnectableTile(level, aboveY, dir);
                if (!(be instanceof IEssentiaTransport transport)) continue;
                Direction opposite = dir.getOpposite();
                if (transport.getEssentiaAmount(opposite) <= 0) continue;
                if (transport.getSuctionAmount(opposite) >= ourSuction) continue;
                if (ourSuction < transport.getMinimumSuction()) continue;
                int taken = transport.takeEssentia(currentSuction, 1, opposite);
                if (taken > 0) {
                    addToContainer(currentSuction, taken);
                    return;
                }
            }
        }
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        super.setItem(slot, stack);
        recalcAspects();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = super.removeItem(slot, amount);
        recalcAspects();
        return result;
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return PotionSlot.isValidPotion(stack);
    }

    private void recalcAspects() {
        if (level == null || level.isClientSide) return;
        ItemStack stack = getItem(0);
        color = 0x333333;
        if (stack.isEmpty()) {
            recipe = new AspectList();
        } else {
            recipe = ConfigAspects.getPotionAspects(stack);
            color = getPotionColor(stack);
        }
        charges = 0;
        recipe = AspectHelper.cullTags(recipe, 10);
        recipeProgress = new AspectList();
        syncTile(false);
    }

    public int getPotionColor(ItemStack itemstack) {
        net.minecraft.world.item.alchemy.Potion potion = PotionUtils.getPotion(itemstack);
        return PotionUtils.getColor(potion);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        recipe.writeToNBT(tag, "recipe");
        recipeProgress.writeToNBT(tag, "progress");
        tag.putInt("charges", charges);
        tag.putInt("color", color);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        recipe = new AspectList();
        recipe.readFromNBT(tag, "recipe");
        recipeProgress = new AspectList();
        recipeProgress.readFromNBT(tag, "progress");
        charges = tag.getInt("charges");
        color = tag.getInt("color");
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction direction) {
        return true;
    }

    @Override
    public AspectList getAspects() {
        return recipeProgress;
    }

    @Override
    public void setAspects(AspectList aspects) {
        recipeProgress = aspects;
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        if (tag == null || amount <= 0) return amount;
        int needed = recipe.getAmount(tag) - recipeProgress.getAmount(tag);
        if (needed <= 0) return amount;
        int add = Math.min(needed, amount);
        recipeProgress.add(tag, add);
        syncTile(false);
        return amount - add;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return recipeProgress.getAmount(tag) >= amount;
    }

    @Override
    public int containerContains(Aspect tag) {
        return recipeProgress.getAmount(tag);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != getFacing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != getFacing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
        currentSuction = aspect;
    }

    @Override
    public Aspect getSuctionType(Direction face) {
        return currentSuction;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return currentSuction != null ? SUCTION_AMOUNT : 0;
    }

    @Override
    public Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (canInputFrom(face)) {
            return amount - addToContainer(aspect, amount);
        }
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}