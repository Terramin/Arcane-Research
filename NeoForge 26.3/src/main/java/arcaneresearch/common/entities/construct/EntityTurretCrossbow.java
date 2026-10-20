package arcaneresearch.common.entities.construct;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.menu.TurretMenu;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityTurretCrossbow extends EntityOwnedConstruct implements RangedAttackMob {

    public float loadProgressForRender = 0.0f;
    protected int loadProgressInt = 0;
    protected boolean isLoadInProgress = false;
    protected float loadProgress = 0.0f;
    protected float prevLoadProgress = 0.0f;
    protected boolean attackedLastTick = false;
    protected int attackCount = 0;
    // NEOFORGE_PORT: LivingEntity.swinging/swingTime removed in 26.x (private SwingState);
    // this class drives its own swing timing for the draw animation, kept as local fields 1:1.
    protected boolean swinging;
    protected int swingTime;

    public EntityTurretCrossbow(EntityType<? extends EntityTurretCrossbow> type, Level level) {
        super(type, level);
        // NEOFORGE_PORT: setMaxUpStep removed in 26.x -> STEP_HEIGHT attribute (Thaumaturge oracle).
        var stepHeight = getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null) {
            stepHeight.setBaseValue(0.0);
        }
    }

    public EntityTurretCrossbow(EntityType<? extends EntityTurretCrossbow> type, Level level, BlockPos pos) {
        this(type, level);
        this.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityOwnedConstruct.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 0.0, 20, 60, 24.0f));
        this.goalSelector.addGoal(2, new WatchTargetGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestValidTargetGoal<>(this, Mob.class, 5, true, false,
                entity -> entity instanceof Enemy));
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack ammo = getMainHandItem();
        if (!ammo.isEmpty() && ammo.getCount() > 0) {
            Arrow arrow = new Arrow(level(), this, new ItemStack(net.minecraft.world.item.Items.ARROW), null);
            arrow.setBaseDamage(2.25 + distanceFactor * 2.0 + random.nextGaussian() * 0.25);

            if (ammo.getItem() == Items.TIPPED_ARROW) {
                // NEOFORGE_PORT: Arrow.setEffectsFromItem removed (setPotionContents is
                // private) -> copy PotionContents effects via public addEffect, same result.
                var contents = ammo.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
                if (contents != null) {
                    contents.forEachEffect(arrow::addEffect, 1.0F);
                }
            }

            Vec3 look = getLookAngle();
            if (!isPassenger()) {
                arrow.setPos(
                        arrow.getX() - look.x * 0.9,
                        arrow.getY() - look.y * 0.9,
                        arrow.getZ() - look.z * 0.9
                );
            } else {
                arrow.setPos(
                        arrow.getX() + look.x * 1.75,
                        arrow.getY() + look.y * 1.75,
                        arrow.getZ() + look.z * 1.75
                );
            }

            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

            double dx = target.getX() - getX();
            double dy = target.getBoundingBox().minY + target.getEyeHeight() + distanceFactor * distanceFactor * 3.0 - arrow.getY();
            double dz = target.getZ() - getZ();

            arrow.shoot(dx, dy, dz, 2.0f, 2.0f);
            level().addFreshEntity(arrow);

            level().broadcastEntityEvent(this, (byte) 16);
            playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (getRandom().nextFloat() * 0.4f + 0.8f));

            ammo.shrink(1);
            if (ammo.isEmpty()) {
                setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            if (!swinging) {
                swingTime = -1;
                swinging = true;
            }
        } else if (id == 17) {
            if (!isLoadInProgress) {
                loadProgressInt = -1;
                isLoadInProgress = true;
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    public float getLoadProgress(float partialTicks) {
        float diff = loadProgress - prevLoadProgress;
        if (diff < 0.0f) {
            diff += 1.0f;
        }
        return prevLoadProgress + diff * partialTicks;
    }

    // NEOFORGE_PORT: LivingEntity.getAttackAnim gone; the local swing fields
    // drive the same 0..1 shoot-flex phase for the render states.
    public float getShootSwingPhase(float partialTicks) {
        return swinging && swingTime >= 0
                ? Math.min(1.0F, (swingTime + partialTicks) / 6.0F) : 0.0F;
    }

    protected void updateSwingProgress() {
        if (swinging) {
            swingTime++;
            if (swingTime >= 6) {
                swingTime = 0;
                swinging = false;
            }
        } else {
            swingTime = 0;
        }

        if (isLoadInProgress) {
            loadProgressInt++;
            if (loadProgressInt >= 10) {
                loadProgressInt = 0;
                isLoadInProgress = false;
            }
        } else {
            loadProgressInt = 0;
        }
        loadProgress = loadProgressInt / 10.0f;
    }

    @Override
    public void aiStep() {
        prevLoadProgress = loadProgress;

        if (!level().isClientSide() && (getMainHandItem().isEmpty() || getMainHandItem().getCount() <= 0)) {
            BlockPos below = blockPosition().below();
            BlockEntity blockEntity = level().getBlockEntity(below);

            if (blockEntity instanceof DispenserBlockEntity dispenser) {
                BlockState state = level().getBlockState(below);
                if (state.getBlock() instanceof DispenserBlock) {

                    if (state.getValue(DirectionalBlock.FACING) == net.minecraft.core.Direction.UP) {
                        for (int i = 0; i < dispenser.getContainerSize(); i++) {
                            ItemStack stack = dispenser.getItem(i);
                            if (!stack.isEmpty() && stack.getItem() instanceof ArrowItem) {
                                setItemInHand(InteractionHand.MAIN_HAND, dispenser.removeItem(i, stack.getCount()));
                                playSound(ModSounds.TICKS.get(), 1.0f, 1.0f);
                                level().broadcastEntityEvent(this, (byte) 17);
                                break;
                            }
                        }
                    }
                }
            }
        }

        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();

        if (getTarget() != null && (getTarget().isDeadOrDying() || isAlliedTo(getTarget()))) {
            setTarget(null);
        }

        if (!level().isClientSide()) {
            setYRot(yHeadRot);

            if (tickCount % 80 == 0) {
                heal(1.0f);
            }

            int rk = net.minecraft.util.Mth.floor(getX());
            int rl = net.minecraft.util.Mth.floor(getY());
            int ri = net.minecraft.util.Mth.floor(getZ());
            if (net.minecraft.world.level.block.BaseRailBlock.isRail(level(), new BlockPos(rk, rl - 1, ri))) {
                rl--;
            }
            BlockState railState = level().getBlockState(new BlockPos(rk, rl, ri));
            if (net.minecraft.world.level.block.BaseRailBlock.isRail(railState)
                    && railState.is(arcaneresearch.init.ModBlocks.ACTIVATOR_RAIL.get())) {
                setNoAi(railState.getValue(net.minecraft.world.level.block.PoweredRailBlock.POWERED));
            }
        } else {
            updateSwingProgress();
        }
    }
    // NEOFORGE_PORT: getStandingEyeHeight removed in 26.3 (eye height via EntityDimensions.withEyeHeight).

    @Override
    public boolean isPushable() {

        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public int getMaxHeadXRot() {
        return 20;
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {

        setYRot(getYRot() + (float) (getRandom().nextGaussian() * 45.0));
        setXRot(getXRot() + (float) (getRandom().nextGaussian() * 20.0));
        return super.hurtServer(serverLevel, source, amount);
    }

    // NEOFORGE_PORT: LivingEntity.knockback gained (DamageSource, damage, comesFromEffect) in 26.x;
    // damped-turret behavior kept (1/10th horizontal push + upward clamp).
    // NEOFORGE_PORT (26.3): override the 6-arg variant -- the 5-arg one delegates to it, but attack
    // knockback (causeExtraKnockback, Player sprint/enchant) calls the 6-arg one directly; in Forge
    // every path went through the single 3-arg knockback.
    @Override
    public void knockback(double strength, double x, double z, DamageSource source, float damage, boolean comesFromEffect) {
        super.knockback(strength, x / 10.0, z / 10.0, source, damage, comesFromEffect);
        Vec3 delta = getDeltaMovement();
        if (delta.y > 0.1) {
            setDeltaMovement(delta.x, 0.1, delta.z);
        }
    }

    @Override
    public void move(MoverType type, Vec3 motion) {

        super.move(type, new Vec3(motion.x / 20.0, motion.y, motion.z / 20.0));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && isOwner(player) && !isRemoved()) {
            if (player.isShiftKeyDown()) {

                playSound(ModSounds.ZAP.get(), 1.0f, 1.0f);
                dropAmmo();

                spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.TURRET_PLACER_BASIC.get()), 0.5f);
                discard();
                player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
                return InteractionResult.SUCCESS;
            } else {

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.openMenu( new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("item.arcaneresearch.turret_placer_basic");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                            return new TurretMenu(containerId, playerInventory, EntityTurretCrossbow.this);
                        }
                    }, buf -> buf.writeInt(getId()));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.TURRET_PLACER_BASIC.get());
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide()) {
            dropAmmo();
        }
    }

    protected void dropAmmo() {
        ItemStack held = getMainHandItem();
        if (!held.isEmpty()) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), held, 0.5f);
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean recentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        super.dropCustomDeathLoot(serverLevel, source, recentlyHit);
        float bonus = looting * 0.15f;

        if (random.nextFloat() < 0.2f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.BRAIN_CLOCKWORK.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.MECHANISM_SIMPLE.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModBlocks.GREATWOOD_PLANKS.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModBlocks.GREATWOOD_PLANKS.get()), 0.5f);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
    }

    protected static class NearestValidTargetGoal<T extends LivingEntity> extends TargetGoal {
        protected final Class<T> targetClass;
        protected final int randomInterval;
        protected final Predicate<LivingEntity> targetPredicate;
        protected LivingEntity target;

        public NearestValidTargetGoal(Mob mob, Class<T> targetClass, int interval,
                                      boolean mustSee, boolean mustReach, Predicate<LivingEntity> predicate) {
            super(mob, mustSee, mustReach);
            this.targetClass = targetClass;
            this.randomInterval = interval;
            this.targetPredicate = predicate;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (randomInterval > 0 && mob.getRandom().nextInt(randomInterval) != 0) {
                return false;
            }

            double range = getFollowDistance();
            AABB searchBox = mob.getBoundingBox().inflate(range, 4.0, range);

            List<LivingEntity> targets = mob.level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), searchBox, entity -> {
                        if (!targetClass.isInstance(entity)) return false;
                        if (targetPredicate != null && !targetPredicate.test(entity)) return false;
                        if (!entity.isAlive()) return false;
                        if (entity == mob) return false;

                        if (mob instanceof EntityOwnedConstruct construct) {
                            if (construct.isAlliedTo(entity)) return false;
                        }

                        if (mustSee && !mob.getSensing().hasLineOfSight(entity)) return false;

                        return true;
                    });

            if (targets.isEmpty()) {
                return false;
            }

            targets.sort(Comparator.comparingDouble(e -> mob.distanceToSqr(e)));
            target = targets.get(0);
            return true;
        }

        @Override
        public void start() {
            mob.setTarget(target);
            super.start();
        }

        @Override
        public void stop() {
            target = null;
            super.stop();
        }
    }

    protected class WatchTargetGoal extends Goal {
        protected final Mob watcher;
        protected Entity target;
        private int lookTime;

        public WatchTargetGoal(Mob mob) {
            this.watcher = mob;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (watcher.getTarget() != null) {
                target = watcher.getTarget();
            }
            return target != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (target == null || !target.isAlive()) {
                return false;
            }
            double range = getFollowDistance();
            return watcher.distanceToSqr(target) <= range * range && lookTime > 0;
        }

        @Override
        public void start() {
            lookTime = 40 + watcher.getRandom().nextInt(40);
        }

        @Override
        public void stop() {
            target = null;
        }

        @Override
        public void tick() {
            watcher.getLookControl().setLookAt(
                    target.getX(),
                    target.getY() + target.getEyeHeight(),
                    target.getZ(),
                    10.0f,
                    watcher.getMaxHeadXRot()
            );
            lookTime--;
        }

        protected double getFollowDistance() {
            AttributeInstance attr = watcher.getAttribute(Attributes.FOLLOW_RANGE);
            return attr == null ? 16.0 : attr.getValue();
        }
    }
}