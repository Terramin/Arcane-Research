package arcaneresearch.common.tiles.devices;

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
// NEOFORGE_PORT: PotionUtils (alchemy) removed -> PotionContents/DataComponents (see usages).
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.*;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.config.ConfigAspects;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;

import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TilePotionSprayer extends TileArcaneresearchInventory implements IAspectContainer, IEssentiaTransport, WorldlyContainer {

    private static final int[] SLOTS = {0};

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
        // NEOFORGE_PORT: Forge body; PotionUtils.getMobEffects -> POTION_CONTENTS getAllEffects.
        tile.counter++;
        Direction facing = tile.getFacing();

        if (tile.counter % 5 == 0) {
            tile.currentSuction = null;

            if (tile.getItem(0).isEmpty() || tile.charges >= 8) {
                return;
            }

            boolean done = true;
            for (Aspect aspect : tile.recipe.getAspectsSortedByName()) {
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
                tile.setChanged();
            } else if (tile.currentSuction != null) {

                tile.fill();
            }
        }

        boolean isEnabled = state.hasProperty(BlockStateProperties.ENABLED)
                ? state.getValue(BlockStateProperties.ENABLED)
                : !level.hasNeighborSignal(pos);

        if (!isEnabled) {
            if (!tile.activated && tile.charges > 0) {
                tile.charges--;

                java.util.List<MobEffectInstance> effects = new java.util.ArrayList<>();
                tile.getItem(0).getOrDefault(DataComponents.POTION_CONTENTS,
                        net.minecraft.world.item.alchemy.PotionContents.EMPTY)
                        .getAllEffects().forEach(effects::add);
                if (!effects.isEmpty()) {

                    int area = 1;
                    BlockPos targetPos = pos.relative(facing, 2);
                    AABB box = new AABB(targetPos).inflate(area);
                    List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box);

                    for (LivingEntity target : targets) {

                        if (target.isAlive()) {
                            for (MobEffectInstance effect : effects) {
                                // NEOFORGE_PORT: getEffect() returns Holder; instantenous typo
                                // fixed upstream (isInstantaneous/applyInstantaneousEffect + ServerLevel).
                                net.minecraft.core.Holder<MobEffect> holder = effect.getEffect();
                                MobEffect potion = holder.value();
                                if (potion.isInstantaneous()) {
                                    potion.applyInstantaneousEffect((net.minecraft.server.level.ServerLevel) level,
                                            null, null, target, effect.getAmplifier(), 1.0);
                                } else {
                                    target.addEffect(new MobEffectInstance(holder,
                                            effect.getDuration(), effect.getAmplifier()));
                                }
                            }
                        }
                    }
                }

                level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS,
                        0.25f, 2.6f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8f);
                level.blockEvent(pos, state.getBlock(), 0, 0);
                tile.syncTile(false);
                tile.setChanged();
            }
            tile.activated = true;
        } else if (tile.activated) {
            tile.activated = false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TilePotionSprayer tile) {
        Direction facing = tile.getFacing();

        if (tile.venting > 0) {
            tile.venting--;
            for (int a = 0; a < tile.venting / 2; a++) {
                float fx = 0.1f - level.getRandom().nextFloat() * 0.2f;
                float fy = 0.1f - level.getRandom().nextFloat() * 0.2f;
                float fz = 0.1f - level.getRandom().nextFloat() * 0.2f;
                float fx2 = (float) (level.getRandom().nextGaussian() * 0.06);
                float fy2 = (float) (level.getRandom().nextGaussian() * 0.06);
                float fz2 = (float) (level.getRandom().nextGaussian() * 0.06);

                FXDispatcher.INSTANCE.drawVentParticles2(
                        pos.getX() + 0.5f + fx + facing.getStepX() / 2.0f,
                        pos.getY() + 0.5f + fy + facing.getStepY() / 2.0f,
                        pos.getZ() + 0.5f + fz + facing.getStepZ() / 2.0f,
                        fx2 + facing.getStepX() * 0.25,
                        fy2 + facing.getStepY() * 0.25,
                        fz2 + facing.getStepZ() * 0.25,
                        tile.color, 4.0f);
            }
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
        if (id >= 0) {
            if (level != null && level.isClientSide()) {
                venting = 15;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    private void fill() {
        Direction facing = getFacing();

        for (int y = 0; y <= 1; y++) {
            for (Direction dir : Direction.values()) {
                if (dir != facing) {
                    BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition.above(y), dir);
                    if (te instanceof IEssentiaTransport transport) {
                        Direction opposite = dir.getOpposite();
                        if (transport.getEssentiaAmount(opposite) > 0 &&
                                transport.getSuctionAmount(opposite) < getSuctionAmount(null) &&
                                getSuctionAmount(null) >= transport.getMinimumSuction()) {

                            int taken = transport.takeEssentia(currentSuction, 1, opposite);
                            if (taken > 0) {
                                addToContainer(currentSuction, taken);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        recalcAspects();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = super.removeItem(slot, amount);
        recalcAspects();
        return result;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {

        return arcaneresearch.common.menu.slot.PotionSlot.isValidPotion(stack);
    }

    private void recalcAspects() {
        if (level == null || level.isClientSide()) return;

        ItemStack stack = getItem(0);
        color = 0x333333;

        if (!level.isClientSide()) {
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
            setChanged();
        }
    }

    public int getPotionColor(ItemStack itemstack) {
        // NEOFORGE_PORT: PotionUtils.getPotion/getColor removed ->
        //   PotionContents.
        var contents = itemstack.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                net.minecraft.world.item.alchemy.PotionContents.EMPTY);
        // NEOFORGE_PORT (26.3): reference PotionUtils.getPotion never returned null (EMPTY ->
        // getColor(Potions.EMPTY) = 16253176) and PotionUtils.getColor(Potion) blended only the
        // base potion's effects (no custom colour/effects; none -> 3694022). Same RGB, no alpha.
        if (contents.potion().isEmpty()) {
            return 16253176;
        }
        java.util.List<net.minecraft.world.effect.MobEffectInstance> baseEffects =
                contents.potion().get().value().getEffects();
        if (baseEffects.isEmpty()) {
            return 3694022;
        }
        return net.minecraft.world.item.alchemy.PotionContents.getColorOptional(baseEffects).orElse(0) & 0xFFFFFF;
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        recipe.writeToNBT(tag, "recipe");
        recipeProgress.writeToNBT(tag, "progress");
        tag.putInt("charges", charges);
        tag.putInt("color", color);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        recipe = new AspectList();
        recipe.readFromNBT(tag, "recipe");
        recipeProgress = new AspectList();
        recipeProgress.readFromNBT(tag, "progress");
        charges = tag.getIntOr("charges", 0);
        color = tag.getIntOr("color", 0);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
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
        int needed = recipe.getAmount(tag) - recipeProgress.getAmount(tag);
        if (needed <= 0) return amount;

        int add = Math.min(needed, amount);
        recipeProgress.add(tag, add);
        syncTile(false);
        setChanged();
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
        return currentSuction != null ? 128 : 0;
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
