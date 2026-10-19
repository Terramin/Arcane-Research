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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import arcaneresearch.common.menu.TurretMenu;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
public class EntityTurretCrossbow extends EntityOwnedConstruct implements RangedAttackMob {
    public float loadProgressForRender = 0.0f;
    protected int loadProgressInt = 0;
    protected boolean isLoadInProgress = false;
    protected float loadProgress = 0.0f;
    protected float prevLoadProgress = 0.0f;
    protected boolean attackedLastTick = false;
    protected int attackCount = 0;
    public EntityTurretCrossbow(EntityType<? extends EntityTurretCrossbow> type, Level level) {
        super(type, level);
        this.setMaxUpStep(0.0f);
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
    protected void defineSynchedData() {
        super.defineSynchedData();
    }
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack ammo = getMainHandItem();
        if (!ammo.isEmpty() && ammo.getCount() > 0) {
            Arrow arrow = new Arrow(level(), this);
            arrow.setBaseDamage(2.25 + distanceFactor * 2.0 + random.nextGaussian() * 0.25);
            if (ammo.getItem() == Items.TIPPED_ARROW) {
                arrow.setEffectsFromItem(ammo);
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
    @OnlyIn(Dist.CLIENT)
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
    @OnlyIn(Dist.CLIENT)
    public float getLoadProgress(float partialTicks) {
        float diff = loadProgress - prevLoadProgress;
        if (diff < 0.0f) {
            diff += 1.0f;
        }
        return prevLoadProgress + diff * partialTicks;
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
        if (!level().isClientSide && (getMainHandItem().isEmpty() || getMainHandItem().getCount() <= 0)) {
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
        if (!level().isClientSide) {
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
    @Override
    protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
        return dimensions.height * 0.66f;
    }
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
    public boolean hurt(DamageSource source, float amount) {
        setYRot(getYRot() + (float) (getRandom().nextGaussian() * 45.0));
        setXRot(getXRot() + (float) (getRandom().nextGaussian() * 20.0));
        return super.hurt(source, amount);
    }
    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength, x / 10.0, z / 10.0);
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
        if (!level().isClientSide && isOwner(player) && !isRemoved()) {
            if (player.isShiftKeyDown()) {
                playSound(ModSounds.ZAP.get(), 1.0f, 1.0f);
                dropAmmo();
                spawnAtLocation(new ItemStack(ModItems.TURRET_PLACER_BASIC.get()), 0.5f);
                discard();
                player.swing(hand);
                return InteractionResult.SUCCESS;
            } else {
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
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
        if (!level().isClientSide) {
            dropAmmo();
        }
    }
    protected void dropAmmo() {
        ItemStack held = getMainHandItem();
        if (!held.isEmpty()) {
            spawnAtLocation(held, 0.5f);
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        float bonus = looting * 0.15f;
        if (random.nextFloat() < 0.2f + bonus) spawnAtLocation(new ItemStack(ModItems.BRAIN_CLOCKWORK.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation(new ItemStack(ModItems.MECHANISM_SIMPLE.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation(new ItemStack(ModBlocks.GREATWOOD_PLANKS.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation(new ItemStack(ModBlocks.GREATWOOD_PLANKS.get()), 0.5f);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
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
            List<LivingEntity> targets = mob.level().getEntitiesOfClass(
                    LivingEntity.class,
                    searchBox,
                    entity -> {
                        if (!targetClass.isInstance(entity)) return false;
                        if (targetPredicate != null && !targetPredicate.test(entity)) return false;
                        if (!entity.isAlive()) return false;
                        if (entity == mob) return false;
                        if (mob instanceof EntityOwnedConstruct construct) {
                            if (construct.isAlliedTo(entity)) return false;
                        }
                        if (mustSee && !mob.getSensing().hasLineOfSight(entity)) return false;
                        return true;
                    }
            );
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
