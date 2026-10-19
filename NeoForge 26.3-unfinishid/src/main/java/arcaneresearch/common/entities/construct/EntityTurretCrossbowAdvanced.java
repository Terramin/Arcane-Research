package arcaneresearch.common.entities.construct;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import arcaneresearch.common.menu.TurretMenu;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityTurretCrossbowAdvanced extends EntityTurretCrossbow {

    private static final EntityDataAccessor<Byte> DATA_FLAGS =
            SynchedEntityData.defineId(EntityTurretCrossbowAdvanced.class, EntityDataSerializers.BYTE);

    private static final byte FLAG_TARGET_ANIMAL = 0x01;
    private static final byte FLAG_TARGET_MOB = 0x02;
    private static final byte FLAG_TARGET_PLAYER = 0x04;
    private static final byte FLAG_TARGET_FRIENDLY = 0x08;

    private int targetUnseenTicks = 0;

    public EntityTurretCrossbowAdvanced(EntityType<? extends EntityTurretCrossbowAdvanced> type, Level level) {
        super(type, level);
    }

    public EntityTurretCrossbowAdvanced(EntityType<? extends EntityTurretCrossbowAdvanced> type, Level level, BlockPos pos) {
        super(type, level, pos);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityTurretCrossbow.createAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.removeAllGoals(g -> true);
        this.targetSelector.removeAllGoals(g -> true);

        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 0.0, 20, 40, 24.0f));
        this.goalSelector.addGoal(2, new AdvancedWatchTargetGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new AdvancedTargetGoal(this, LivingEntity.class, 5, true, false));

        setTargetMob(true);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS, (byte) 0);
    }
    // NEOFORGE_PORT: getStandingEyeHeight removed in 26.3 (eye height via EntityDimensions.withEyeHeight).

    private byte getFlags() {
        return this.entityData.get(DATA_FLAGS);
    }

    private void setFlags(byte flags) {
        this.entityData.set(DATA_FLAGS, flags);
    }

    private boolean getFlag(byte flag) {
        return (getFlags() & flag) != 0;
    }

    private void setFlag(byte flag, boolean value) {
        byte flags = getFlags();
        if (value) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }
        setFlags(flags);
        setTarget(null);
    }

    public boolean getTargetAnimal() {
        return getFlag(FLAG_TARGET_ANIMAL);
    }

    public void setTargetAnimal(boolean value) {
        setFlag(FLAG_TARGET_ANIMAL, value);
    }

    public boolean getTargetMob() {
        return getFlag(FLAG_TARGET_MOB);
    }

    public void setTargetMob(boolean value) {
        setFlag(FLAG_TARGET_MOB, value);
    }

    public boolean getTargetPlayer() {
        return getFlag(FLAG_TARGET_PLAYER);
    }

    public void setTargetPlayer(boolean value) {
        setFlag(FLAG_TARGET_PLAYER, value);
    }

    public boolean getTargetFriendly() {
        return getFlag(FLAG_TARGET_FRIENDLY);
    }

    public void setTargetFriendly(boolean value) {
        setFlag(FLAG_TARGET_FRIENDLY, value);
    }

    public boolean canAttackType(LivingEntity entity) {

        if (entity instanceof Animal && !(entity instanceof Enemy)) {
            return getTargetAnimal();
        }

        if (entity instanceof Enemy) {
            return getTargetMob();
        }

        if (entity instanceof Player) {
            if (!getTargetPlayer()) {
                return false;
            }

            // NEOFORGE_PORT: MinecraftServer.isPvpAllowed moved to ServerLevel in 26.x.
            if (level() instanceof net.minecraft.server.level.ServerLevel pvpLevel && !pvpLevel.isPvpAllowed()) {
                setTargetPlayer(false);
                return false;
            }
            return true;
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();

        // NEOFORGE_PORT: 1:1 from Forge; MinecraftServer.isPvpAllowed moved to
        // ServerLevel.isPvpAllowed in 26.x (same pattern as below in this file).
        if (!level().isClientSide() && level() instanceof net.minecraft.server.level.ServerLevel pvpLevel
                && !pvpLevel.isPvpAllowed() && getTarget() instanceof Player && getTarget() != getOwner()) {
            setTarget(null);
        }
    }

    @Override
    public void move(MoverType type, Vec3 motion) {

        super.move(type, new Vec3(motion.x / 15.0, motion.y, motion.z / 15.0));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && isOwner(player) && !isRemoved()) {
            if (player.isShiftKeyDown()) {

                playSound(ModSounds.ZAP.get(), 1.0f, 1.0f);
                dropAmmo();

                spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.TURRET_PLACER_ADVANCED.get()), 0.5f);
                discard();
                player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
                return InteractionResult.SUCCESS;
            } else {

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.openMenu( new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("item.arcaneresearch.turret_placer_advanced");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                            return new TurretMenu(containerId, playerInventory, EntityTurretCrossbowAdvanced.this);
                        }
                    }, buf -> buf.writeInt(getId()));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.TURRET_PLACER_ADVANCED.get());
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean recentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        float bonus = looting * 0.15f;

        if (random.nextFloat() < 0.2f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.BRAIN_CURIOUS.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.MECHANISM_SIMPLE.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModBlocks.GREATWOOD_PLANKS.get()), 0.5f);
        if (random.nextFloat() < 0.5f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModBlocks.GREATWOOD_PLANKS.get()), 0.5f);
        if (random.nextFloat() < 0.3f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.PLATE_IRON.get()), 0.5f);
        if (random.nextFloat() < 0.4f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.PLATE_BRASS.get()), 0.5f);
        if (random.nextFloat() < 0.4f + bonus) spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.PLATE_BRASS.get()), 0.5f);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("targets", getFlags());
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        // TODO_PORT: ValueInput.contains gone in 26.3; getByteOr default covers missing keys.
        setFlags(tag.getByteOr("targets", (byte) 0));
    }

    protected class AdvancedWatchTargetGoal extends Goal {
        protected final Mob watcher;
        protected Entity target;
        private int lookTime;

        public AdvancedWatchTargetGoal(Mob mob) {
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

    protected class AdvancedTargetGoal extends TargetGoal {
        protected final Class<? extends LivingEntity> targetClass;
        protected final int randomInterval;
        protected LivingEntity target;
        private int targetUnseenTicks;

        public AdvancedTargetGoal(Mob mob, Class<? extends LivingEntity> targetClass,
                                  int interval, boolean mustSee, boolean mustReach) {
            super(mob, mustSee, mustReach);
            this.targetClass = targetClass;
            this.randomInterval = interval;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (randomInterval > 0 && mob.getRandom().nextInt(randomInterval) != 0) {
                return false;
            }

            double range = getFollowDistance();
            AABB searchBox = mob.getBoundingBox().inflate(range, 4.0, range);

            EntityTurretCrossbowAdvanced turret = (EntityTurretCrossbowAdvanced) mob;

            List<LivingEntity> targets = mob.level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), searchBox, entity -> {
                        if (!targetClass.isInstance(entity)) return false;
                        if (!entity.isAlive()) return false;
                        if (entity == mob) return false;

                        if (!turret.canAttackType(entity)) return false;

                        Team ourTeam = mob.getTeam();
                        Team theirTeam = entity.getTeam();

                        if (ourTeam != null && theirTeam == ourTeam && !turret.getTargetFriendly()) {
                            return false;
                        }
                        if (ourTeam != null && theirTeam != ourTeam && turret.getTargetFriendly()) {
                            return false;
                        }

                        if (turret.isOwned()) {
                            UUID ownerUUID = turret.getOwnerUUID();

                            if (entity == turret.getOwner() && !turret.getTargetFriendly()) {
                                return false;
                            }

                            if (entity instanceof OwnableEntity owned) {
                                // TODO_PORT: OwnableEntity.getOwnerUUID gone in 26.3 -> owner reference UUID.
                                UUID theirOwner = owned.getOwnerReference() == null ? null : owned.getOwnerReference().getUUID();
                                if (ownerUUID != null && ownerUUID.equals(theirOwner) && !turret.getTargetFriendly()) {
                                    return false;
                                }
                                if (ownerUUID != null && !ownerUUID.equals(theirOwner) && turret.getTargetFriendly()) {
                                    return false;
                                }
                            } else if (!(entity instanceof Player) && turret.getTargetFriendly()) {

                                return false;
                            }
                        }

                        if (entity instanceof Player player) {
                            if (player.getAbilities().invulnerable && !turret.getTargetFriendly()) {
                                return false;
                            }
                        }

                        if (mustSee && !mob.getSensing().hasLineOfSight(entity)) {
                            return false;
                        }

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
        public boolean canContinueToUse() {
            LivingEntity currentTarget = mob.getTarget();
            if (currentTarget == null || !currentTarget.isAlive()) {
                return false;
            }

            EntityTurretCrossbowAdvanced turret = (EntityTurretCrossbowAdvanced) mob;

            Team ourTeam = mob.getTeam();
            Team theirTeam = currentTarget.getTeam();

            if (ourTeam != null && theirTeam == ourTeam && !turret.getTargetFriendly()) {
                return false;
            }
            if (ourTeam != null && theirTeam != ourTeam && turret.getTargetFriendly()) {
                return false;
            }

            double range = getFollowDistance();
            if (mob.distanceToSqr(currentTarget) > range * range) {
                return false;
            }

            if (mustSee) {
                if (mob.getSensing().hasLineOfSight(currentTarget)) {
                    targetUnseenTicks = 0;
                } else if (++targetUnseenTicks > 60) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void start() {
            mob.setTarget(target);
            targetUnseenTicks = 0;
            super.start();
        }

        @Override
        public void stop() {
            target = null;
            super.stop();
        }
    }
}
