package arcaneresearch.common.golems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.construct.EntityOwnedConstruct;
import arcaneresearch.common.golems.ai.*;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;

import java.nio.ByteBuffer;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityArcaneresearchGolem extends EntityOwnedConstruct implements IGolemAPI, RangedAttackMob {

    public static final int XP_MULTIPLIER = 1000;
    private static final EntityDataAccessor<Integer> DATA_PROPS1 =
            SynchedEntityData.defineId(EntityArcaneresearchGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PROPS2 =
            SynchedEntityData.defineId(EntityArcaneresearchGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PROPS3 =
            SynchedEntityData.defineId(EntityArcaneresearchGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_CLIMBING =
            SynchedEntityData.defineId(EntityArcaneresearchGolem.class, EntityDataSerializers.BYTE);
    private static final byte FLAG_FOLLOWING = 0x02;
    private static final byte FLAG_COMBAT = 0x08;
    public boolean redrawParts = false;
    protected Task task = null;
    protected int taskID = Integer.MAX_VALUE;
    private int rankXp = 0;
    private boolean firstRun = true;
    public EntityArcaneresearchGolem(EntityType<? extends EntityArcaneresearchGolem> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityOwnedConstruct.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROPS1, 0);
        builder.define(DATA_PROPS2, 0);
        builder.define(DATA_PROPS3, 0);
        builder.define(DATA_CLIMBING, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        rebuildAI();
    }

    @Override
    public IGolemProperties getProperties() {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(this.entityData.get(DATA_PROPS1));
        bb.putInt(this.entityData.get(DATA_PROPS2));
        // NEOFORGE_PORT: ByteBuffer.getLongOr doesn't exist -> getLong.
        return GolemProperties.fromLong(bb.getLong(0));
    }

    @Override
    public void setProperties(IGolemProperties prop) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(prop.toLong());
        bb.rewind();
        this.entityData.set(DATA_PROPS1, bb.getInt());
        this.entityData.set(DATA_PROPS2, bb.getInt());
    }

    @Override
    public byte getGolemColor() {
        int props3 = this.entityData.get(DATA_PROPS3);
        return (byte) (props3 & 0xFF);
    }

    public void setGolemColor(byte color) {
        int props3 = this.entityData.get(DATA_PROPS3);
        props3 = (props3 & 0xFFFFFF00) | (color & 0xFF);
        this.entityData.set(DATA_PROPS3, props3);
    }

    public byte getFlags() {
        int props3 = this.entityData.get(DATA_PROPS3);
        return (byte) ((props3 >> 8) & 0xFF);
    }

    public void setFlags(byte flags) {
        int props3 = this.entityData.get(DATA_PROPS3);
        props3 = (props3 & 0xFFFF00FF) | ((flags & 0xFF) << 8);
        this.entityData.set(DATA_PROPS3, props3);
    }

    private void updateEntityAttributes() {
        IGolemProperties props = getProperties();

        int maxHealth = 10 + props.getMaterial().healthMod;
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) {
            maxHealth = (int) (maxHealth * 0.75);
        }
        maxHealth += props.getRank();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);

        // NEOFORGE_PORT: setMaxUpStep removed in 26.x -> STEP_HEIGHT attribute
        // (same values; Thaumaturge oracle).
        var stepHeight = getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null) {
            stepHeight.setBaseValue(props.hasTrait(EnumGolemTrait.WHEELED) ? 0.5 : 0.6);
        }

        int homeRange = props.hasTrait(EnumGolemTrait.SCOUT) ? 48 : 32;
        setHomeTo(getHomePosition().equals(BlockPos.ZERO) ? blockPosition() : getHomePosition(), homeRange);

        double followRange = props.hasTrait(EnumGolemTrait.SCOUT) ? 56.0 : 40.0;
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(followRange);

        this.navigation = createGolemNavigation();

        if (props.hasTrait(EnumGolemTrait.FLYER)) {
            this.moveControl = new FlightMoveHelper(this);
        }

        if (props.hasTrait(EnumGolemTrait.FIGHTER)) {
            double damage = props.getMaterial().damage;
            if (props.hasTrait(EnumGolemTrait.BRUTAL)) {
                damage = Math.max(damage * 1.5, damage + 1.0);
            }
            damage += props.getRank() * 0.25;
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        } else {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.0);
        }

        rebuildAI();
    }

    private void rebuildAI() {
        this.goalSelector.removeAllGoals(g -> true);
        this.targetSelector.removeAllGoals(g -> true);

        IGolemProperties props = getProperties();

        if (isFollowingOwner()) {
            this.goalSelector.addGoal(4, new AIFollowOwner(this, 1.0, 10.0f, 2.0f));
        } else {
            this.goalSelector.addGoal(3, new AIGotoEntity(this));
            this.goalSelector.addGoal(4, new AIGotoBlock(this));
            this.goalSelector.addGoal(5, new AIGotoHome(this));
        }

        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        if (props.hasTrait(EnumGolemTrait.FIGHTER)) {
            if (this.navigation instanceof GroundPathNavigation) {
                this.goalSelector.addGoal(0, new FloatGoal(this));
            }
            if (props.hasTrait(EnumGolemTrait.RANGED) && props.getArms().function != null) {
                Goal rangedAI = props.getArms().function.getRangedAttackAI(this);
                if (rangedAI != null) {
                    this.goalSelector.addGoal(1, rangedAI);
                }
            }

            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));

            if (isFollowingOwner()) {
                this.targetSelector.addGoal(1, new AIOwnerHurtByTarget(this));
                this.targetSelector.addGoal(2, new AIOwnerHurtTarget(this));
            }

            this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        }
    }

    private PathNavigation createGolemNavigation() {
        IGolemProperties props = getProperties();
        if (props.hasTrait(EnumGolemTrait.FLYER)) {
            return new PathNavigateGolemAir(this, level());
        } else if (props.hasTrait(EnumGolemTrait.CLIMBER)) {
            return new WallClimberNavigation(this, level());
        } else {
            return new PathNavigateGolemGround(this, level());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (getProperties().hasTrait(EnumGolemTrait.FLYER)) {
            setNoGravity(true);
        }

        if (!level().isClientSide()) {
            if (firstRun) {
                firstRun = false;
                if (hasHome() && !blockPosition().equals(getHomePosition())) {
                    teleportToHome();
                }
            }

            if (task != null && task.isSuspended()) {
                task = null;
            }

            if (getTarget() != null && getTarget().isDeadOrDying()) {
                setTarget(null);
            }

            if (getTarget() instanceof net.minecraft.world.entity.player.Player
                    && level() instanceof net.minecraft.server.level.ServerLevel pvpLevel && !pvpLevel.isPvpAllowed()) {
                setTarget(null);
            }

            if (getTarget() != null && getProperties().hasTrait(EnumGolemTrait.RANGED) && distanceToSqr(getTarget()) > 1024.0) {
                setTarget(null);
            }

            int healInterval = getProperties().hasTrait(EnumGolemTrait.REPAIR) ? 40 : 100;
            if (tickCount % healInterval == 0) {
                heal(1.0f);
            }

            if (getProperties().hasTrait(EnumGolemTrait.CLIMBER)) {
                setBesideClimbableBlock(horizontalCollision);
            }
        } else {
            if (tickCount < 20 || tickCount % 20 == 0) {
                redrawParts = true;
            }
        }

        IGolemProperties props = getProperties();
        if (props.getHead().function != null) {
            props.getHead().function.onUpdateTick(this);
        }
        if (props.getArms().function != null) {
            props.getArms().function.onUpdateTick(this);
        }
        if (props.getLegs().function != null) {
            props.getLegs().function.onUpdateTick(this);
        }
        if (props.getAddon().function != null) {
            props.getAddon().function.onUpdateTick(this);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    public float getGolemMoveSpeed() {
        IGolemProperties props = getProperties();
        float speed = 1.0f + props.getRank() * 0.025f;
        if (props.hasTrait(EnumGolemTrait.LIGHT)) speed += 0.2f;
        if (props.hasTrait(EnumGolemTrait.HEAVY)) speed -= 0.175f;
        if (props.hasTrait(EnumGolemTrait.FLYER)) speed -= 0.33f;
        if (props.hasTrait(EnumGolemTrait.WHEELED)) speed += 0.25f;
        return speed;
    }

    @Override
    public boolean onClimbable() {
        return isBesideClimbableBlock();
    }

    public boolean isBesideClimbableBlock() {
        return (this.entityData.get(DATA_CLIMBING) & 0x01) != 0;
    }

    public void setBesideClimbableBlock(boolean climbing) {
        byte b = this.entityData.get(DATA_CLIMBING);
        if (climbing) {
            b |= 0x01;
        } else {
            b &= 0xFE;
        }
        this.entityData.set(DATA_CLIMBING, b);
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        if (getProperties().hasTrait(EnumGolemTrait.HEAVY) && !getProperties().hasTrait(EnumGolemTrait.FLYER)) {
            super.playStepSound(pos, state);
        }
    }

    // NEOFORGE_PORT: causeFallDamage(float, float, DamageSource) -> (double, float, DamageSource).
    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        if (getProperties().hasTrait(EnumGolemTrait.FLYER) || getProperties().hasTrait(EnumGolemTrait.CLIMBER)) {
            return false;
        }
        return super.causeFallDamage(distance, multiplier, source);
    }

    private void teleportToHome() {
        if (!hasHome()) return;

        BlockPos home = getHomePosition();
        double oldX = getX();
        double oldY = getY();
        double oldZ = getZ();

        double newX = home.getX() + 0.5;
        double newY = home.getY();
        double newZ = home.getZ() + 0.5;
        boolean foundCeiling = false;
        BlockPos checkPos = home;
        // NEOFORGE_PORT: getMaxBuildHeight gone -> getMaxY (LevelHeightAccessor);
        // blocksMotion gone -> isSolid (same motion-blocking intent).
        // NEOFORGE_PORT (26.3): getMaxY() is inclusive (= getMaxBuildHeight() - 1), and 1.20.1
        // blocksMotion() was isSolid() minus cobweb and bamboo sapling.
        while (!foundCeiling && checkPos.getY() <= level().getMaxY()) {
            net.minecraft.world.level.block.state.BlockState ceiling = level().getBlockState(checkPos.above());
            if (ceiling.isSolid() && !ceiling.is(net.minecraft.world.level.block.Blocks.COBWEB)
                    && !ceiling.is(net.minecraft.world.level.block.Blocks.BAMBOO_SAPLING)) {
                foundCeiling = true;
            } else {
                newY++;
                checkPos = checkPos.above();
            }
        }

        boolean placed = false;
        if (foundCeiling) {
            setPos(newX, newY, newZ);
            if (level().noCollision(this)) {
                placed = true;
            }
        }
        if (!placed) {
            setPos(oldX, oldY, oldZ);
        } else {
            getNavigation().stop();
        }

        resetFallDistance();
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE) && getProperties().hasTrait(EnumGolemTrait.FIREPROOF)) {
            return false;
        }
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) && getProperties().hasTrait(EnumGolemTrait.BLASTPROOF)) {
            amount = Math.min(getMaxHealth() / 2.0f, amount * 0.3f);
        }
        // NEOFORGE_PORT (26.3): match by damage type like 1.20.1 (identity checks against the
        // cached damageSources() instances missed any equal-typed source created elsewhere).
        if (source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)) {
            return false;
        }
        if (hasHome() && (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL) || source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD))) {
            teleportToHome();
        }
        return super.hurtServer(serverLevel, source, amount);
    }

    @Override
    public int getArmorValue() {
        int armor = getProperties().getMaterial().armor;
        if (getProperties().hasTrait(EnumGolemTrait.ARMORED)) {
            armor = (int) Math.max(armor * 1.5, armor + 1);
        }
        if (getProperties().hasTrait(EnumGolemTrait.FRAGILE)) {
            armor = (int) (armor * 0.75);
        }
        return armor;
    }

    @Override
    public void setTarget(LivingEntity target) {
        super.setTarget(target);
        setInCombat(getTarget() != null);
    }

    @Override
    public boolean isInCombat() {
        return (getFlags() & FLAG_COMBAT) != 0;
    }

    public void setInCombat(boolean combat) {
        byte flags = getFlags();
        if (combat) {
            flags |= FLAG_COMBAT;
        } else {
            flags &= ~FLAG_COMBAT;
        }
        setFlags(flags);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel serverLevel, Entity target) {
        // NEOFORGE_PORT: 26.x enchant-combat rework (verified against Mob.doHurtTarget bytecode).
        // getDamageBonus(weapon, mobType)/getKnockbackBonus/getFireAspect/doPostHurtEffects removed;
        // equivalents: modifyDamage (incl. smite/bane), modifyKnockback, doPostAttackEffects
        // (covers fire aspect). Custom golem kb math + DEFT trait + arms hook preserved 1:1.
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        ItemStack weapon = getMainHandItem();
        net.minecraft.world.damagesource.DamageSource source = damageSources().mobAttack(this);
        damage = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDamage(
                serverLevel, weapon, target, source, damage);
        float kb = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyKnockback(
                serverLevel, weapon, target, source, 0.0f);

        boolean hit = target.hurtServer(serverLevel, source, damage);
        if (hit) {
            if (target instanceof LivingEntity living && getProperties().hasTrait(EnumGolemTrait.DEFT)) {
                living.setInvulnerableTime(100);
            }
            if (kb > 0) {
                double rad = getYRot() * Math.PI / 180.0;
                target.setDeltaMovement(target.getDeltaMovement().add(
                        -net.minecraft.util.Mth.sin((float) rad) * kb * 0.5, 0.1,
                        net.minecraft.util.Mth.cos((float) rad) * kb * 0.5));
                Vec3 selfMotion = getDeltaMovement();
                setDeltaMovement(new Vec3(selfMotion.x * 0.6, selfMotion.y, selfMotion.z * 0.6));
            }
            net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(
                    serverLevel, target, source);

            if (getProperties().getArms().function != null) {
                getProperties().getArms().function.onMeleeAttack(this, target);
            }

            if (target instanceof Mob && !target.isAlive()) {
                addRankXp(8);
            }
        }
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (getProperties().getArms().function != null) {
            getProperties().getArms().function.onRangedAttack(this, target, distanceFactor);
        }
    }

    public boolean isFollowingOwner() {
        return (getFlags() & FLAG_FOLLOWING) != 0;
    }

    public void setFollowingOwner(boolean following) {
        byte flags = getFlags();
        boolean wasFollowing = (flags & FLAG_FOLLOWING) != 0;
        if (following) {
            flags |= FLAG_FOLLOWING;
        } else {
            flags &= ~FLAG_FOLLOWING;
        }
        setFlags(flags);

        if (!level().isClientSide() && wasFollowing != following) {
            if (task != null) {
                task.setReserved(false);
                task = null;
            }
            rebuildAI();
        }
    }

    public Task getTask() {
        if (task == null && taskID != Integer.MAX_VALUE) {
            task = TaskHandler.getTask(level().dimension(), taskID);
            taskID = Integer.MAX_VALUE;
        }
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getRankXp() {
        return rankXp;
    }

    public void setRankXp(int xp) {
        this.rankXp = xp;
    }

    @Override
    public void addRankXp(int xp) {
        if (!getProperties().hasTrait(EnumGolemTrait.SMART) || level().isClientSide()) {
            return;
        }

        int rank = getProperties().getRank();
        if (rank < 10) {
            rankXp += xp;
            int xpNeeded = (rank + 1) * (rank + 1) * EntityArcaneresearchGolem.XP_MULTIPLIER;
            if (rankXp >= xpNeeded) {
                rankXp -= xpNeeded;
                IGolemProperties props = getProperties();
                props.setRank(rank + 1);
                setProperties(props);
                if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.showGolemEmotes) {
                    playSound(SoundEvents.PLAYER_LEVELUP, 0.25f, 1.0f);
                    level().broadcastEntityEvent(this, (byte) 9);
                }
            }
        }
    }

    @Override
    public ItemStack holdItem(ItemStack stack) {
        if (stack.isEmpty()) return stack;

        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            EquipmentSlot slot = EquipmentSlot.values()[i];
            ItemStack current = getItemBySlot(slot);

            if (current.isEmpty()) {
                setItemSlot(slot, stack.copy());
                return ItemStack.EMPTY;
            }

            if (ItemStack.isSameItemSameComponents(current, stack) && current.getCount() < current.getMaxStackSize()) {
                int space = current.getMaxStackSize() - current.getCount();
                int toAdd = Math.min(stack.getCount(), space);
                current.grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack dropItem(ItemStack stack) {
        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        ItemStack result = ItemStack.EMPTY;

        for (int i = 0; i < slots; i++) {
            EquipmentSlot slot = EquipmentSlot.values()[i];
            ItemStack current = getItemBySlot(slot);

            if (!current.isEmpty()) {
                if (stack == null || stack.isEmpty()) {
                    result = current.copy();
                    setItemSlot(slot, ItemStack.EMPTY);
                    break;
                } else if (ItemStack.isSameItemSameComponents(current, stack)) {
                    int toDrop = Math.min(stack.getCount(), current.getCount());
                    result = current.copy();
                    result.setCount(toDrop);
                    current.shrink(toDrop);
                    if (current.isEmpty()) {
                        setItemSlot(slot, ItemStack.EMPTY);
                    }
                    break;
                }
            }
        }

        if (getProperties().hasTrait(EnumGolemTrait.HAULER)) {
            ItemStack first = getItemBySlot(EquipmentSlot.values()[0]);
            ItemStack second = getItemBySlot(EquipmentSlot.values()[1]);
            if (first.isEmpty() && !second.isEmpty()) {
                setItemSlot(EquipmentSlot.values()[0], second.copy());
                setItemSlot(EquipmentSlot.values()[1], ItemStack.EMPTY);
            }
        }

        return result;
    }

    @Override
    public int canCarryAmount(ItemStack stack) {
        int total = 0;
        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;

        for (int i = 0; i < slots; i++) {
            ItemStack current = getItemBySlot(EquipmentSlot.values()[i]);
            if (current.isEmpty()) {
                total += stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(current, stack)) {
                total += stack.getMaxStackSize() - current.getCount();
            }
        }
        return total;
    }

    @Override
    public boolean canCarry(ItemStack stack, boolean partial) {
        int amount = canCarryAmount(stack);
        if (amount > 0) {
            return partial || amount >= stack.getCount();
        }
        return false;
    }

    @Override
    public boolean isCarrying(ItemStack stack) {
        if (stack.isEmpty()) return false;

        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            ItemStack current = getItemBySlot(EquipmentSlot.values()[i]);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NonNullList<ItemStack> getCarrying() {
        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        NonNullList<ItemStack> list = NonNullList.withSize(slots, ItemStack.EMPTY);
        for (int i = 0; i < slots; i++) {
            list.set(i, getItemBySlot(EquipmentSlot.values()[i]));
        }
        return list;
    }

    @Override
    public LivingEntity getGolemEntity() {
        return this;
    }

    @Override
    public Level getGolemWorld() {
        return level();
    }

    // NEOFORGE_PORT: render states read the arm-wiggle phase through this accessor;
    // it is the old LivingEntity.getAttackAnim(partialTicks) (26.x: SwingState animation).
    public float getArmSwingPhase(float partialTicks) {
        return getSwingAnimation(partialTicks);
    }

    // NEOFORGE_PORT (26.3): LivingEntity.swinging/swingTime are now the private SwingState.
    // The previous port kept shadow fields that never ticked and broadcast entity event 0,
    // which no 26.x client handles, so golem arms never animated. Vanilla swing() is the
    // same logic as the 1.20.1 body: restart only when idle, before the first tick or past
    // duration/2 (= 3 of 6 ticks), then ClientboundSwingAnimationPacket(MAIN_HAND) to
    // trackers and self (old broadcastAndSend(ClientboundAnimatePacket(this, 0))).
    @Override
    public void swingArm() {
        swing(InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved()) return InteractionResult.PASS;
        if (player.getItemInHand(hand).getItem() == Items.NAME_TAG) return InteractionResult.PASS;

        if (!level().isClientSide() && isOwner(player)) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (player.isShiftKeyDown()) {
                playSound(ModSounds.ZAP.get(), 1.0f, 1.0f);
                if (task != null) {
                    task.setReserved(false);
                }
                dropCarried();

                ItemStack placer = new ItemStack(ModItems.GOLEM_PLACER.get());
                // NEOFORGE_PORT: stack CompoundTag removed -> CUSTOM_DATA (copy + write-back).
                net.minecraft.nbt.CompoundTag placerTag =
                        arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(placer);
                placerTag.putLong("props", getProperties().toLong());
                placerTag.putInt("xp", rankXp);
                arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(placer, placerTag);
                spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), placer, 0.5f);

                discard();
                player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
                return InteractionResult.SUCCESS;
            }

            if (heldItem.getItem() instanceof DyeItem) {
                // NEOFORGE_PORT: DyeItem.getDyeColor removed; dye color is the DYE component now.
                DyeColor color = heldItem.get(net.minecraft.core.component.DataComponents.DYE);
                if (color == null) {
                    return InteractionResult.PASS;
                }

                setGolemColor((byte) (color.getId() + 1));
                heldItem.shrink(1);
                playSound(ModSounds.ZAP.get(), 1.0f, 1.0f);
                player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
                return InteractionResult.SUCCESS;
            }

            if (heldItem.getItem() == ModItems.GOLEM_BELL.get()
                    && arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities
                    .isResearchKnown(player, "GOLEMDIRECT")) {
                if (task != null) {
                    task.setReserved(false);
                }
                playSound(ModSounds.SCAN.get(), 1.0f, 1.0f);
                boolean following = !isFollowingOwner();
                setFollowingOwner(following);
                if (following) {
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("golem.follow"));
                    if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.showGolemEmotes) {
                        level().broadcastEntityEvent(this, (byte) 5);
                    }
                    clearHome();
                } else {
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("golem.stay"));
                    if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.showGolemEmotes) {
                        level().broadcastEntityEvent(this, (byte) 8);
                    }
                    setHomeTo(blockPosition(),
                            getProperties().hasTrait(EnumGolemTrait.SCOUT) ? 48 : 32);
                }
                updateEntityAttributes();
                player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void dropCarried() {
        for (ItemStack stack : getCarrying()) {
            if (!stack.isEmpty()) {
                spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), stack, 0.25f);
            }
        }
        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            setItemSlot(EquipmentSlot.values()[i], ItemStack.EMPTY);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id >= 5 && id <= 9) {
            spawnGolemFX(id);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnGolemFX(byte id) {
        if (id == 5) {
            FXDispatcher.INSTANCE.drawGenericParticles(getX(), getY() + getBbHeight() + 0.1, getZ(),
                    0.0, 0.0, 0.0, 1.0f, 1.0f, 1.0f, 0.5f, false,
                    704 + (random.nextBoolean() ? 0 : 3), 3, 1, 6, 0, 2.0f, 0.0f, 1);
        } else if (id == 6) {
            FXDispatcher.INSTANCE.drawGenericParticles(getX(), getY() + getBbHeight() + 0.1, getZ(),
                    0.0, 0.025, 0.0, 0.1f, 1.0f, 1.0f, 0.5f, false, 15, 1, 1, 10, 0, 2.0f, 0.0f, 1);
        } else if (id == 7) {
            FXDispatcher.INSTANCE.drawGenericParticles(getX(), getY() + getBbHeight() + 0.1, getZ(),
                    0.0, 0.05, 0.0, 1.0f, 1.0f, 1.0f, 0.5f, false, 640, 10, 1, 10, 0, 2.0f, 0.0f, 1);
        } else if (id == 8) {
            FXDispatcher.INSTANCE.drawGenericParticles(getX(), getY() + getBbHeight() + 0.1, getZ(),
                    0.0, 0.01, 0.0, 1.0f, 1.0f, 0.1f, 0.5f, false, 14, 1, 1, 20, 0, 2.0f, 0.0f, 1);
        } else {
            for (int a = 0; a < 5; ++a) {
                FXDispatcher.INSTANCE.drawGenericParticles(getX(), getY() + getBbHeight(), getZ(),
                        random.nextGaussian() * 0.01, random.nextFloat() * 0.02, random.nextGaussian() * 0.01,
                        1.0f, 1.0f, 1.0f, 0.5f, false, 13, 1, 1, 20 + random.nextInt(20), 0,
                        0.3f + random.nextFloat() * 0.4f, 0.0f, 1);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (task != null) {
            task.setReserved(false);
        }
        super.die(source);
        if (!level().isClientSide()) {
            dropCarried();
        }
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean recentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        super.dropCustomDeathLoot(serverLevel, source, recentlyHit);
        float bonus = looting * 0.15f;

        for (ItemStack stack : getProperties().generateComponents()) {
            ItemStack drop = stack.copy();
            if (random.nextFloat() < 0.3f + bonus) {
                if (drop.getCount() > 1) {
                    drop.shrink(random.nextInt(drop.getCount()));
                }
                spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), drop, 0.25f);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("props", getProperties().toLong());
        tag.putLong("homepos", getHomePosition().asLong());
        tag.putByte("gflags", getFlags());
        tag.putInt("rankXP", rankXp);
        tag.putByte("color", getGolemColor());
    }

    // NEOFORGE_PORT: finalizeSpawn 5-arg (CompoundTag) removed in 26.x -> 4-arg.
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            net.minecraft.world.entity.EntitySpawnReason reason,
            @javax.annotation.Nullable net.minecraft.world.entity.SpawnGroupData data) {

        if (!hasHome()) {
            setHomeTo(blockPosition(), 32);
        }
        updateEntityAttributes();
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setProperties(GolemProperties.fromLong(tag.getLongOr("props", 0L)));
        setHomeTo(BlockPos.of(tag.getLongOr("homepos", 0L)), 32);
        setFlags(tag.getByteOr("gflags", (byte) 0));
        rankXp = tag.getIntOr("rankXP", 0);
        setGolemColor(tag.getByteOr("color", (byte) 0));
        updateEntityAttributes();
    }

    // NEOFORGE_PORT: getStandingEyeHeight removed in 26.x (getEyeHeight final);
    // eye height 0.7f moved to the EntityType builder (see ModEntities).

    // NEOFORGE_PORT: getBoundingBoxForCulling() removed in 26.x (culling now via
    // render state). The inflated cull box (0.6, 1.2, 0.6) returns in the render phase.
    public net.minecraft.world.phys.AABB getCullingBox() {

        return getBoundingBox().inflate(0.6, 1.2, 0.6);
    }

}
