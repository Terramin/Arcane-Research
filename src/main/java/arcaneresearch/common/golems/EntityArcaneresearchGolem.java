package arcaneresearch.common.golems;

import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.entities.construct.EntityOwnedConstruct;
import arcaneresearch.common.golems.ai.*;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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

    private static final EquipmentSlot[] STORAGE_SLOTS = { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND };

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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PROPS1, 0);
        this.entityData.define(DATA_PROPS2, 0);
        this.entityData.define(DATA_PROPS3, 0);
        this.entityData.define(DATA_CLIMBING, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        rebuildAI();
    }

    @Override
    public IGolemProperties getProperties() {
        int p1 = this.entityData.get(DATA_PROPS1);
        int p2 = this.entityData.get(DATA_PROPS2);
        long bits = ((long) p1 << 32) | (p2 & 0xFFFFFFFFL);
        if (bits == 0L) return new GolemProperties();
        IGolemProperties props = GolemProperties.fromLong(bits);
        return props != null ? props : new GolemProperties();
    }

    @Override
    public void setProperties(IGolemProperties prop) {
        if (prop == null) return;
        long bits = prop.toLong();
        this.entityData.set(DATA_PROPS1, (int) (bits >>> 32));
        this.entityData.set(DATA_PROPS2, (int) bits);
    }

    @Override
    public byte getGolemColor() {
        return (byte) (this.entityData.get(DATA_PROPS3) & 0xFF);
    }

    public void setGolemColor(byte color) {
        int props3 = this.entityData.get(DATA_PROPS3);
        this.entityData.set(DATA_PROPS3, (props3 & 0xFFFFFF00) | (color & 0xFF));
    }

    public byte getFlags() {
        return (byte) ((this.entityData.get(DATA_PROPS3) >> 8) & 0xFF);
    }

    public void setFlags(byte flags) {
        int props3 = this.entityData.get(DATA_PROPS3);
        this.entityData.set(DATA_PROPS3, (props3 & 0xFFFF00FF) | ((flags & 0xFF) << 8));
    }

    private void updateEntityAttributes() {
        IGolemProperties props = getProperties();
        var material = props.getMaterial();
        int maxHealth = 10 + (material != null ? material.healthMod : 0);
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) {
            maxHealth = (int) (maxHealth * 0.75);
        }
        maxHealth += props.getRank();
        Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealth);

        this.setMaxUpStep(props.hasTrait(EnumGolemTrait.WHEELED) ? 0.5f : 0.6f);

        if (hasRestriction()) {
            int homeRange = props.hasTrait(EnumGolemTrait.SCOUT) ? 48 : 32;
            restrictTo(getRestrictCenter(), homeRange);
        }

        double followRange = props.hasTrait(EnumGolemTrait.SCOUT) ? 56.0 : 40.0;
        Objects.requireNonNull(getAttribute(Attributes.FOLLOW_RANGE)).setBaseValue(followRange);

        boolean needsNewNav;
        if (props.hasTrait(EnumGolemTrait.FLYER)) {
            needsNewNav = !(this.navigation instanceof PathNavigateGolemAir);
        } else if (props.hasTrait(EnumGolemTrait.CLIMBER)) {
            needsNewNav = !(this.navigation instanceof WallClimberNavigation);
        } else {
            needsNewNav = !(this.navigation instanceof PathNavigateGolemGround);
        }
        if (needsNewNav) {
            this.navigation = createGolemNavigation();
        }

        if (props.hasTrait(EnumGolemTrait.FLYER) && !(this.moveControl instanceof FlightMoveHelper)) {
            this.moveControl = new FlightMoveHelper(this);
        }

        if (props.hasTrait(EnumGolemTrait.FIGHTER)) {
            double damage = material != null ? material.damage : 0.0;
            if (props.hasTrait(EnumGolemTrait.BRUTAL)) {
                damage = Math.max(damage * 1.5, damage + 1.0);
            }
            damage += props.getRank() * 0.25;
            Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(damage);
        } else {
            Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(0.0);
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
            if (props.hasTrait(EnumGolemTrait.RANGED)) {
                var arms = props.getArms();
                if (arms != null && arms.function != null) {
                    Goal rangedAI = arms.function.getRangedAttackAI(this);
                    if (rangedAI != null) {
                        this.goalSelector.addGoal(1, rangedAI);
                    }
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
        }
        return new PathNavigateGolemGround(this, level());
    }

    @Override
    public void tick() {
        super.tick();
        IGolemProperties props = getProperties();
        boolean flyer = props.hasTrait(EnumGolemTrait.FLYER);
        boolean climber = props.hasTrait(EnumGolemTrait.CLIMBER);
        boolean ranged = props.hasTrait(EnumGolemTrait.RANGED);
        boolean repair = props.hasTrait(EnumGolemTrait.REPAIR);

        setNoGravity(flyer);

        if (!level().isClientSide) {
            if (firstRun) {
                firstRun = false;
                if (hasRestriction() && !blockPosition().equals(getRestrictCenter())) {
                    teleportToHome();
                }
            }
            if (task != null && task.isSuspended()) {
                task = null;
            }
            LivingEntity target = getTarget();
            if (target != null) {
                if (target.isDeadOrDying()) {
                    setTarget(null);
                } else if (target instanceof Player
                        && level().getServer() != null
                        && !Objects.requireNonNull(level().getServer()).isPvpAllowed()) {
                    setTarget(null);
                } else if (ranged && distanceToSqr(target) > 1024.0) {
                    setTarget(null);
                }
            }
            int healInterval = repair ? 40 : 100;
            if (tickCount % healInterval == 0) {
                heal(1.0f);
            }
            if (climber) {
                setBesideClimbableBlock(horizontalCollision);
            } else if (isBesideClimbableBlock()) {
                setBesideClimbableBlock(false);
            }
        } else {
            if (tickCount < 20 || tickCount % 20 == 0) {
                redrawParts = true;
            }
        }

        var head = props.getHead();
        if (head != null && head.function != null) head.function.onUpdateTick(this);
        var arms = props.getArms();
        if (arms != null && arms.function != null) arms.function.onUpdateTick(this);
        var legs = props.getLegs();
        if (legs != null && legs.function != null) legs.function.onUpdateTick(this);
        var addon = props.getAddon();
        if (addon != null && addon.function != null) addon.function.onUpdateTick(this);
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
        if (climbing) b |= 0x01;
        else b = (byte) (b & 0xFE);
        this.entityData.set(DATA_CLIMBING, b);
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, net.minecraft.world.level.block.state.@NotNull BlockState state) {
        IGolemProperties props = getProperties();
        if (props.hasTrait(EnumGolemTrait.HEAVY) && !props.hasTrait(EnumGolemTrait.FLYER)) {
            super.playStepSound(pos, state);
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, @NotNull DamageSource source) {
        IGolemProperties props = getProperties();
        if (props.hasTrait(EnumGolemTrait.FLYER) || props.hasTrait(EnumGolemTrait.CLIMBER)) {
            return false;
        }
        return super.causeFallDamage(distance, multiplier, source);
    }

    private void teleportToHome() {
        if (!hasRestriction()) return;
        BlockPos home = getRestrictCenter();
        double oldX = getX();
        double oldY = getY();
        double oldZ = getZ();
        double newX = home.getX() + 0.5;
        double newY = home.getY();
        double newZ = home.getZ() + 0.5;
        boolean foundCeiling = false;
        BlockPos checkPos = home;
        while (!foundCeiling && checkPos.getY() < level().getMaxBuildHeight()) {
            if (level().getBlockState(checkPos.above()).blocksMotion()) {
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
    public boolean hurt(DamageSource source, float amount) {
        IGolemProperties props = getProperties();
        if (source.is(DamageTypeTags.IS_FIRE) && props.hasTrait(EnumGolemTrait.FIREPROOF)) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION) && props.hasTrait(EnumGolemTrait.BLASTPROOF)) {
            amount = Math.min(getMaxHealth() / 2.0f, amount * 0.3f);
        }
        if (source.is(DamageTypes.CACTUS)) {
            return false;
        }
        if (hasRestriction() && (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FELL_OUT_OF_WORLD))) {
            teleportToHome();
        }
        return super.hurt(source, amount);
    }

    @Override
    public int getArmorValue() {
        IGolemProperties props = getProperties();
        var material = props.getMaterial();
        int armor = material != null ? material.armor : 0;
        if (props.hasTrait(EnumGolemTrait.ARMORED)) {
            armor = (int) Math.max(armor * 1.5, armor + 1);
        }
        if (props.hasTrait(EnumGolemTrait.FRAGILE)) {
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
        if (combat) flags |= FLAG_COMBAT;
        else flags &= ~FLAG_COMBAT;
        setFlags(flags);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        float damage = (float) Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
        int kb = 0;
        if (target instanceof LivingEntity livingTarget) {
            damage += EnchantmentHelper.getDamageBonus(getMainHandItem(), livingTarget.getMobType());
            kb += EnchantmentHelper.getKnockbackBonus(this);
        }
        boolean hit = target.hurt(damageSources().mobAttack(this), damage);
        if (!hit) return false;

        IGolemProperties props = getProperties();
        if (target instanceof LivingEntity living && props.hasTrait(EnumGolemTrait.DEFT)) {
            living.invulnerableTime = 100;
        }
        if (kb > 0) {
            float rad = getYRot() * Mth.DEG_TO_RAD;
            target.setDeltaMovement(target.getDeltaMovement().add(
                    -Mth.sin(rad) * kb * 0.5,
                    0.1,
                    Mth.cos(rad) * kb * 0.5));
            Vec3 selfMotion = getDeltaMovement();
            setDeltaMovement(selfMotion.x * 0.6, selfMotion.y, selfMotion.z * 0.6);
        }
        int fire = EnchantmentHelper.getFireAspect(this);
        if (fire > 0) {
            target.setSecondsOnFire(fire * 4);
        }
        EnchantmentHelper.doPostHurtEffects(this, target);
        var arms = props.getArms();
        if (arms != null && arms.function != null) {
            arms.function.onMeleeAttack(this, target);
        }
        if (target instanceof Mob && !target.isAlive()) {
            addRankXp(8);
        }
        return true;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float distanceFactor) {
        var arms = getProperties().getArms();
        if (arms != null && arms.function != null) {
            arms.function.onRangedAttack(this, target, distanceFactor);
        }
    }

    public boolean isFollowingOwner() {
        return (getFlags() & FLAG_FOLLOWING) != 0;
    }

    public void setFollowingOwner(boolean following) {
        byte flags = getFlags();
        boolean wasFollowing = (flags & FLAG_FOLLOWING) != 0;
        if (following) flags |= FLAG_FOLLOWING;
        else flags &= ~FLAG_FOLLOWING;
        setFlags(flags);
        if (!level().isClientSide && wasFollowing != following) {
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
        if (level().isClientSide || xp <= 0) return;
        IGolemProperties props = getProperties();
        if (!props.hasTrait(EnumGolemTrait.SMART)) return;
        int rank = props.getRank();
        if (rank >= 10) return;

        rankXp += xp;
        boolean leveled = false;
        while (rank < 10) {
            int xpNeeded = (rank + 1) * (rank + 1) * XP_MULTIPLIER;
            if (rankXp < xpNeeded) break;
            rankXp -= xpNeeded;
            rank++;
            leveled = true;
        }
        if (!leveled) return;

        props.setRank(rank);
        setProperties(props);
        updateEntityAttributes();
        if (ModConfig.CONFIG_GRAPHICS.showGolemEmotes) {
            playSound(SoundEvents.PLAYER_LEVELUP, 0.25f, 1.0f);
            level().broadcastEntityEvent(this, (byte) 9);
        }
    }

    @Override
    public ItemStack holdItem(ItemStack stack) {
        if (stack.isEmpty()) return stack;
        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            EquipmentSlot slot = STORAGE_SLOTS[i];
            ItemStack current = getItemBySlot(slot);
            if (current.isEmpty()) {
                setItemSlot(slot, stack.copy());
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameTags(current, stack) && current.getCount() < current.getMaxStackSize()) {
                int space = current.getMaxStackSize() - current.getCount();
                int toAdd = Math.min(stack.getCount(), space);
                current.grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    @Override
    public ItemStack dropItem(@Nullable ItemStack stack) {
        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        ItemStack result = ItemStack.EMPTY;
        for (int i = 0; i < slots; i++) {
            EquipmentSlot slot = STORAGE_SLOTS[i];
            ItemStack current = getItemBySlot(slot);
            if (current.isEmpty()) continue;
            if (stack == null || stack.isEmpty()) {
                result = current.copy();
                setItemSlot(slot, ItemStack.EMPTY);
                break;
            }
            if (ItemStack.isSameItemSameTags(current, stack)) {
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
        if (getProperties().hasTrait(EnumGolemTrait.HAULER)) {
            ItemStack first = getItemBySlot(EquipmentSlot.MAINHAND);
            ItemStack second = getItemBySlot(EquipmentSlot.OFFHAND);
            if (first.isEmpty() && !second.isEmpty()) {
                setItemSlot(EquipmentSlot.MAINHAND, second.copy());
                setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        }
        return result;
    }

    @Override
    public int canCarryAmount(ItemStack stack) {
        int total = 0;
        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            ItemStack current = getItemBySlot(STORAGE_SLOTS[i]);
            if (current.isEmpty()) {
                total += stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameTags(current, stack)) {
                total += current.getMaxStackSize() - current.getCount();
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
            ItemStack current = getItemBySlot(STORAGE_SLOTS[i]);
            if (!current.isEmpty() && ItemStack.isSameItemSameTags(current, stack)) {
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
            list.set(i, getItemBySlot(STORAGE_SLOTS[i]));
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

    @Override
    public void swingArm() {
        if (!swinging || swingTime >= 3 || swingTime < 0) {
            swingTime = -1;
            swinging = true;
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.getChunkSource().broadcastAndSend(this,
                        new ClientboundAnimatePacket(this, 0));
            }
        }
    }

    @Override
    protected @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved()) return InteractionResult.PASS;
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() == Items.NAME_TAG) return InteractionResult.PASS;

        if (!level().isClientSide && isOwner(player)) {
            if (player.isShiftKeyDown()) {
                playSound(ModSounds.ZAP.get(), 1.0f, 1.0f);
                if (task != null) task.setReserved(false);
                dropCarried();
                ItemStack placer = new ItemStack(ModItems.GOLEM_PLACER.get());
                CompoundTag placerTag = placer.getOrCreateTag();
                placerTag.putLong("props", getProperties().toLong());
                placerTag.putInt("xp", rankXp);
                spawnAtLocation(placer, 0.5f);
                discard();
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
            if (heldItem.getItem() instanceof DyeItem dyeItem) {
                DyeColor color = dyeItem.getDyeColor();
                setGolemColor((byte) (color.getId() + 1));
                heldItem.shrink(1);
                playSound(ModSounds.ZAP.get(), 1.0f, 1.0f);
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
            if (heldItem.getItem() == ModItems.GOLEM_BELL.get()
                    && arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities
                    .isResearchKnown(player, "GOLEMDIRECT")) {
                if (task != null) task.setReserved(false);
                playSound(ModSounds.SCAN.get(), 1.0f, 1.0f);
                boolean following = !isFollowingOwner();
                setFollowingOwner(following);
                IGolemProperties props = getProperties();
                if (following) {
                    player.displayClientMessage(Component.translatable("golem.follow"), true);
                    if (ModConfig.CONFIG_GRAPHICS.showGolemEmotes) {
                        level().broadcastEntityEvent(this, (byte) 5);
                    }
                    clearRestriction();
                } else {
                    player.displayClientMessage(Component.translatable("golem.stay"), true);
                    if (ModConfig.CONFIG_GRAPHICS.showGolemEmotes) {
                        level().broadcastEntityEvent(this, (byte) 8);
                    }
                    restrictTo(blockPosition(), props.hasTrait(EnumGolemTrait.SCOUT) ? 48 : 32);
                }
                updateEntityAttributes();
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    private void dropCarried() {
        for (ItemStack stack : getCarrying()) {
            if (!stack.isEmpty()) {
                spawnAtLocation(stack, 0.25f);
            }
        }
        int slots = getProperties().hasTrait(EnumGolemTrait.HAULER) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            setItemSlot(STORAGE_SLOTS[i], ItemStack.EMPTY);
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
        double x = getX();
        double y = getY() + getBbHeight() + 0.1;
        double z = getZ();
        if (id == 5) {
            FXDispatcher.INSTANCE.drawGenericParticles(x, y, z,
                    0.0, 0.0, 0.0, 1.0f, 1.0f, 1.0f, 0.5f, false,
                    704 + (random.nextBoolean() ? 0 : 3), 3, 1, 6, 0, 2.0f, 0.0f, 1);
        } else if (id == 6) {
            FXDispatcher.INSTANCE.drawGenericParticles(x, y, z,
                    0.0, 0.025, 0.0, 0.1f, 1.0f, 1.0f, 0.5f, false, 15, 1, 1, 10, 0, 2.0f, 0.0f, 1);
        } else if (id == 7) {
            FXDispatcher.INSTANCE.drawGenericParticles(x, y, z,
                    0.0, 0.05, 0.0, 1.0f, 1.0f, 1.0f, 0.5f, false, 640, 10, 1, 10, 0, 2.0f, 0.0f, 1);
        } else if (id == 8) {
            FXDispatcher.INSTANCE.drawGenericParticles(x, y, z,
                    0.0, 0.01, 0.0, 1.0f, 1.0f, 0.1f, 0.5f, false, 14, 1, 1, 20, 0, 2.0f, 0.0f, 1);
        } else {
            for (int a = 0; a < 5; ++a) {
                FXDispatcher.INSTANCE.drawGenericParticles(x, y, z,
                        random.nextGaussian() * 0.01, random.nextFloat() * 0.02, random.nextGaussian() * 0.01,
                        1.0f, 1.0f, 1.0f, 0.5f, false, 13, 1, 1, 20 + random.nextInt(20), 0,
                        0.3f + random.nextFloat() * 0.4f, 0.0f, 1);
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        if (task != null) task.setReserved(false);
        super.die(source);
        if (!level().isClientSide) {
            dropCarried();
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        float bonus = looting * 0.15f;
        for (ItemStack stack : getProperties().generateComponents()) {
            ItemStack drop = stack.copy();
            if (random.nextFloat() < 0.3f + bonus) {
                if (drop.getCount() > 1) {
                    drop.shrink(random.nextInt(drop.getCount()));
                }
                spawnAtLocation(drop, 0.25f);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("props", getProperties().toLong());
        if (hasRestriction()) {
            tag.putLong("homepos", getRestrictCenter().asLong());
        }
        tag.putByte("gflags", getFlags());
        tag.putInt("rankXP", rankXp);
        tag.putByte("color", getGolemColor());
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.@NotNull ServerLevelAccessor level,
            net.minecraft.world.@NotNull DifficultyInstance difficulty,
            @NotNull MobSpawnType reason,
            @Nullable SpawnGroupData data,
            @Nullable CompoundTag tag) {
        if (!hasRestriction()) {
            restrictTo(blockPosition(), 32);
        }
        updateEntityAttributes();
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("props")) {
            IGolemProperties p = GolemProperties.fromLong(tag.getLong("props"));
            if (p != null) {
                setProperties(p);
            }
        }
        if (tag.contains("homepos")) {
            restrictTo(BlockPos.of(tag.getLong("homepos")), 32);
        }
        if (tag.contains("gflags")) {
            setFlags(tag.getByte("gflags"));
        }
        rankXp = tag.getInt("rankXP");
        if (tag.contains("color")) {
            setGolemColor(tag.getByte("color"));
        }
        updateEntityAttributes();
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
        return 0.7f;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(0.6, 1.2, 0.6);
    }
}