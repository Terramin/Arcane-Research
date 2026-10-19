package arcaneresearch.common.entities.monster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.curios.ItemPechWand;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.List;
public class EntityPech extends Monster implements RangedAttackMob {
    public static final int TYPE_REGULAR = 0;
    public static final int TYPE_MAGE = 1;
    public static final int TYPE_FORAGER = 2;
    private static final EntityDataAccessor<Byte> DATA_TYPE =
            SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_ANGER =
            SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TAMED =
            SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BOOLEAN);
    private static final java.util.HashMap<net.minecraft.world.item.Item, Integer> valuedItems = new java.util.HashMap<>();
    static {
        valuedItems.put(net.minecraft.world.item.Items.GOLD_INGOT, 4);
        valuedItems.put(net.minecraft.world.item.Items.DIAMOND, 8);
        valuedItems.put(net.minecraft.world.item.Items.EMERALD, 6);
        valuedItems.put(net.minecraft.world.item.Items.LAPIS_LAZULI, 2);
        valuedItems.put(net.minecraft.world.item.Items.AMETHYST_SHARD, 3);
        valuedItems.put(net.minecraft.world.item.Items.NETHERITE_INGOT, 16);
        valuedItems.put(net.minecraft.world.item.Items.NETHERITE_SCRAP, 12);
        valuedItems.put(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE, 20);
        valuedItems.put(net.minecraft.world.item.Items.GOLDEN_APPLE, 8);
        valuedItems.put(net.minecraft.world.item.Items.END_CRYSTAL, 15);
        valuedItems.put(net.minecraft.world.item.Items.DRAGON_EGG, 32);
        valuedItems.put(net.minecraft.world.item.Items.ECHO_SHARD, 10);
        valuedItems.put(net.minecraft.world.item.Items.RECOVERY_COMPASS, 8);
        valuedItems.put(net.minecraft.world.item.Items.PRISMARINE_CRYSTALS, 2);
        valuedItems.put(net.minecraft.world.item.Items.PRISMARINE_SHARD, 1);
        valuedItems.put(net.minecraft.world.item.Items.HEART_OF_THE_SEA, 10);
        valuedItems.put(net.minecraft.world.item.Items.NAUTILUS_SHELL, 3);
        valuedItems.put(net.minecraft.world.item.Items.TRIDENT, 12);
        valuedItems.put(net.minecraft.world.item.Items.SADDLE, 4);
        valuedItems.put(net.minecraft.world.item.Items.DIAMOND_HORSE_ARMOR, 8);
        valuedItems.put(net.minecraft.world.item.Items.GOLDEN_HORSE_ARMOR, 6);
        valuedItems.put(net.minecraft.world.item.Items.IRON_HORSE_ARMOR, 4);
    }
    public NonNullList<ItemStack> loot = NonNullList.withSize(9, ItemStack.EMPTY);
    public boolean trading = false;
    public float mumble = 0.0f;
    private int chargeCount = 0;
    private RangedAttackGoal aiArrowAttack;
    private RangedAttackGoal aiBlastAttack;
    private MeleeAttackGoal aiMeleeAttack;
    public EntityPech(EntityType<? extends EntityPech> type, Level level) {
        super(type, level);
        this.xpReward = 8;
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
        setDropChance(EquipmentSlot.MAINHAND, 0.2f);
        setDropChance(EquipmentSlot.OFFHAND, 0.2f);
    }
    public EntityPech(Level level) {
        this(ModEntities.PECH.get(), level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ARMOR, 2.0);
    }
    public static boolean checkPechSpawnRules(EntityType<? extends EntityPech> type, ServerLevelAccessor level,
                                              MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        return skyLight <= 12 && (blockLight >= 0 && blockLight <= 10);
    }
    public static boolean isPechWand(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof ItemPechWand) return true;
        return ModItems.PECH_WAND != null && stack.is(ModItems.PECH_WAND.get());
    }
    public static void addValuedItem(net.minecraft.world.item.Item item, int value) {
        valuedItems.put(item, value);
    }
    public float getMumble() {
        return mumble;
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE, (byte) 0);
        this.entityData.define(DATA_ANGER, 0);
        this.entityData.define(DATA_TAMED, false);
    }
    @Override
    protected void registerGoals() {
        aiArrowAttack = new RangedAttackGoal(this, 0.6, 20, 50, 15.0f);
        aiBlastAttack = new RangedAttackGoal(this, 0.6, 20, 50, 15.0f);
        aiMeleeAttack = new MeleeAttackGoal(this, 0.6, false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1,
                new arcaneresearch.common.entities.ai.pech.AIPechTradePlayer(this));
        this.goalSelector.addGoal(3,
                new arcaneresearch.common.entities.ai.pech.AIPechItemEntityGoto(this));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.5));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new arcaneresearch.common.entities.ai.pech.AINearestAttackableTargetPech(this, true));
    }
    public void setCombatTask() {
        if (level() != null && !level().isClientSide) {
            goalSelector.removeGoal(aiMeleeAttack);
            goalSelector.removeGoal(aiArrowAttack);
            goalSelector.removeGoal(aiBlastAttack);
            ItemStack weapon = getMainHandItem();
            if (weapon.getItem() instanceof BowItem) {
                goalSelector.addGoal(2, aiArrowAttack);
            } else if (isPechWand(weapon)) {
                goalSelector.addGoal(2, aiBlastAttack);
            } else {
                goalSelector.addGoal(2, aiMeleeAttack);
            }
        }
    }
    public int getPechType() {
        return this.entityData.get(DATA_TYPE);
    }
    public void setPechType(int type) {
        this.entityData.set(DATA_TYPE, (byte) type);
    }
    public int getAnger() {
        return this.entityData.get(DATA_ANGER);
    }
    public void setAnger(int anger) {
        this.entityData.set(DATA_ANGER, anger);
    }
    public boolean isTamed() {
        return this.entityData.get(DATA_TAMED);
    }
    public void setTamed(boolean tamed) {
        this.entityData.set(DATA_TAMED, tamed);
    }
    @Override
    public float getEyeHeight(net.minecraft.world.entity.Pose pose) {
        return getBbHeight() * 0.66f;
    }
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        setEquipmentBasedOnDifficulty(difficulty);
        ItemStack weapon = getMainHandItem();
        if (isPechWand(weapon)) {
            setPechType(TYPE_MAGE);
            setDropChance(EquipmentSlot.MAINHAND, 0.1f);
        } else if (!weapon.isEmpty()) {
            if (weapon.getItem() instanceof BowItem) {
                setPechType(TYPE_FORAGER);
            }
            populateDefaultEquipmentEnchantments(random, difficulty);
        }
        float diffFactor = difficulty.getSpecialMultiplier();
        setCanPickUpLoot(random.nextFloat() < 0.75f * diffFactor);
        setCombatTask();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
    }
    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
        int roll = random.nextInt(20);
        ItemStack weapon = switch (roll) {
            case 0, 12 ->
                    ModItems.PECH_WAND != null ? new ItemStack(ModItems.PECH_WAND.get()) : new ItemStack(Items.STICK);
            case 1 -> new ItemStack(Items.STONE_SWORD);
            case 3 -> new ItemStack(Items.STONE_AXE);
            case 5 -> new ItemStack(Items.IRON_SWORD);
            case 6 -> new ItemStack(Items.IRON_AXE);
            case 7 -> new ItemStack(Items.FISHING_ROD);
            case 8 -> new ItemStack(Items.STONE_PICKAXE);
            case 9 -> new ItemStack(Items.IRON_PICKAXE);
            case 2, 4, 10, 11, 13 -> new ItemStack(Items.BOW);
            default -> ItemStack.EMPTY;
        };
        if (!weapon.isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, weapon);
        }
    }
    private void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        populateDefaultEquipmentSlots(random, difficulty);
    }
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (getPechType() == TYPE_FORAGER) {
            Arrow arrow = new Arrow(level(), this);
            double dx = target.getX() - getX();
            double dy = target.getBoundingBox().minY + target.getBbHeight() / 3.0 - arrow.getY();
            double dz = target.getZ() - getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            arrow.shoot(dx, dy + dist * 0.2, dz, 1.6f, 14 - level().getDifficulty().getId() * 4);
            arrow.setBaseDamage(power * 2.0 + random.nextGaussian() * 0.25 + level().getDifficulty().getId() * 0.11);
            playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (getRandom().nextFloat() * 0.4f + 0.8f));
            level().addFreshEntity(arrow);
        } else if (getPechType() == TYPE_MAGE) {
            swing(getUsedItemHand());
            try {
                arcaneresearch.api.casters.FocusPackage p = new arcaneresearch.api.casters.FocusPackage(this);
                arcaneresearch.api.casters.FocusMediumRoot root =
                        new arcaneresearch.api.casters.FocusMediumRoot();
                double off = Math.sqrt(distanceToSqr(target)) / 6.0;
                root.setupFromCasterToTarget(this, target, off);
                p.addNode(root);
                arcaneresearch.common.items.casters.foci.FocusMediumProjectile fp =
                        new arcaneresearch.common.items.casters.foci.FocusMediumProjectile();
                fp.initialize();
                try {
                    fp.getSetting("speed").setValue(2);
                } catch (Exception ignored) {
                }
                p.addNode(fp);
                float r = random.nextFloat();
                if (r < 0.2f) p.addNode(new arcaneresearch.common.items.casters.foci.FocusEffectCurse());
                else if (r < 0.4f) p.addNode(new arcaneresearch.common.items.casters.foci.FocusEffectFlux());
                else if (r < 0.6f) p.addNode(new arcaneresearch.common.items.casters.foci.FocusEffectEarth());
                else if (r < 0.8f) p.addNode(new arcaneresearch.common.items.casters.foci.FocusEffectAir());
                else p.addNode(new arcaneresearch.common.items.casters.foci.FocusEffectFire());
                arcaneresearch.api.casters.FocusEngine.castFocusPackage(this, p, true);
            } catch (Exception e) {
                if (distanceToSqr(target) < 256) {
                    target.hurt(damageSources().indirectMagic(this, this), 4.0f + random.nextFloat() * 2.0f);
                }
            }
        }
    }
    private void becomeAngryAt(Entity target) {
        if (target instanceof Player player && player.isCreative()) {
            return;
        }
        if (getAnger() <= 0) {
            level().broadcastEntityEvent(this, (byte) 19);
            playSound(ModSounds.PECH_CHARGE.get(), getSoundVolume(), getVoicePitch());
        }
        if (target instanceof LivingEntity living) {
            setTarget(living);
        }
        setAnger(400 + random.nextInt(400));
        setTamed(false);
        setCombatTask();
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof Player) {
            AABB searchBox = getBoundingBox().inflate(32.0, 16.0, 32.0);
            List<EntityPech> nearbyPechs = level().getEntitiesOfClass(EntityPech.class, searchBox);
            for (EntityPech pech : nearbyPechs) {
                pech.becomeAngryAt(attacker);
            }
            becomeAngryAt(attacker);
        }
        return super.hurt(source, amount);
    }
    @Override
    public void tick() {
        if (mumble > 0.0f) {
            mumble *= 0.75f;
        }
        if (getAnger() > 0) {
            setAnger(getAnger() - 1);
        }
        if (getAnger() > 0 && getTarget() != null) {
            if (chargeCount > 0) {
                --chargeCount;
            }
            if (chargeCount == 0) {
                chargeCount = 100;
                playSound(ModSounds.PECH_CHARGE.get(), getSoundVolume(), getVoicePitch());
            }
            level().broadcastEntityEvent(this, (byte) 17);
        }
        if (level().isClientSide && random.nextInt(15) == 0 && getAnger() > 0) {
            double dx = random.nextGaussian() * 0.02;
            double dy = random.nextGaussian() * 0.02;
            double dz = random.nextGaussian() * 0.02;
            level().addParticle(ParticleTypes.ANGRY_VILLAGER,
                    getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                    getY() + 0.5 + random.nextFloat() * getBbHeight(),
                    getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                    dx, dy, dz);
        }
        if (level().isClientSide && random.nextInt(25) == 0 && isTamed()) {
            double dx = random.nextGaussian() * 0.02;
            double dy = random.nextGaussian() * 0.02;
            double dz = random.nextGaussian() * 0.02;
            level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                    getY() + 0.5 + random.nextFloat() * getBbHeight(),
                    getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                    dx, dy, dz);
        }
        super.tick();
    }
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 40 == 0) {
            heal(1.0f);
        }
    }
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (isTamed()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                trading = true;
                net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer,
                        new net.minecraft.world.MenuProvider() {
                            @Override
                            public net.minecraft.network.chat.Component getDisplayName() {
                                return EntityPech.this.getDisplayName();
                            }
                            @Override
                            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                                    int containerId,
                                    net.minecraft.world.entity.player.Inventory inventory,
                                    Player p) {
                                return new arcaneresearch.common.menu.PechMenu(
                                        containerId, inventory, EntityPech.this);
                            }
                        }, buf -> buf.writeInt(getId()));
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
    @Override
    public void playAmbientSound() {
        if (!level().isClientSide) {
            if (random.nextInt(3) == 0) {
                AABB searchBox = getBoundingBox().inflate(4.0, 2.0, 4.0);
                List<EntityPech> nearbyPechs = level().getEntitiesOfClass(EntityPech.class, searchBox, e -> e != this);
                if (!nearbyPechs.isEmpty()) {
                    level().broadcastEntityEvent(this, (byte) 17);
                    playSound(ModSounds.PECH_TRADE.get(), getSoundVolume(), getVoicePitch());
                    return;
                }
            }
            level().broadcastEntityEvent(this, (byte) 16);
        }
        super.playAmbientSound();
    }
    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PECH_IDLE.get();
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PECH_HIT.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PECH_DEATH.get();
    }
    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 16 -> mumble = (float) Math.PI;
            case 17 -> mumble = (float) (Math.PI * 2);
            case 18 -> {
                for (int i = 0; i < 5; ++i) {
                    double dx = random.nextGaussian() * 0.02;
                    double dy = random.nextGaussian() * 0.02;
                    double dz = random.nextGaussian() * 0.02;
                    level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                            getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                            getY() + 0.5 + random.nextFloat() * getBbHeight(),
                            getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                            dx, dy, dz);
                }
            }
            case 19 -> {
                for (int i = 0; i < 5; ++i) {
                    double dx = random.nextGaussian() * 0.02;
                    double dy = random.nextGaussian() * 0.02;
                    double dz = random.nextGaussian() * 0.02;
                    level().addParticle(ParticleTypes.ANGRY_VILLAGER,
                            getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                            getY() + 0.5 + random.nextFloat() * getBbHeight(),
                            getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth(),
                            dx, dy, dz);
                }
                mumble = (float) (Math.PI * 2);
            }
            default -> super.handleEntityEvent(id);
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("PechType", (byte) getPechType());
        tag.putShort("Anger", (short) getAnger());
        tag.putBoolean("Tamed", isTamed());
        net.minecraft.world.ContainerHelper.saveAllItems(tag, loot);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PechType")) {
            setPechType(tag.getByte("PechType"));
        }
        setAnger(tag.getShort("Anger"));
        setTamed(tag.getBoolean("Tamed"));
        loot = NonNullList.withSize(9, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(tag, loot);
        setCombatTask();
    }
    @Override
    public boolean removeWhenFarAway(double distance) {
        try {
            if (loot == null) return true;
            int count = 0;
            for (ItemStack stack : loot) {
                if (!stack.isEmpty()) {
                    ++count;
                }
            }
            return count < 5;
        } catch (Exception e) {
            return true;
        }
    }
    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }
    public boolean isValued(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        if (valuedItems.containsKey(item.getItem())) {
            return true;
        }
        AspectList al = ArcaneresearchCraftingManager.getObjectTags(item);
        return al.getAmount(Aspect.DESIRE) > 1;
    }
    public int getValue(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return 0;
        }
        if (valuedItems.containsKey(item.getItem())) {
            return valuedItems.get(item.getItem());
        }
        AspectList al = ArcaneresearchCraftingManager.getObjectTags(item);
        return Math.min(32, al.getAmount(Aspect.DESIRE) / 2);
    }
    public boolean canPickup(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!isTamed() && valuedItems.containsKey(stack.getItem())) {
            return true;
        }
        for (int i = 0; i < loot.size(); i++) {
            if (!loot.get(i).isEmpty() && loot.get(i).getCount() <= 0) {
                loot.set(i, ItemStack.EMPTY);
            }
            if (loot.get(i).isEmpty()) {
                return true;
            }
            if (arcaneresearch.common.lib.utils.InventoryUtils.areItemStacksEqualStrict(stack, loot.get(i))
                    && stack.getCount() + loot.get(i).getCount() <= loot.get(i).getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }
    public ItemStack pickupItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (isTamed() || !isValued(stack)) {
            for (int i = 0; i < loot.size(); i++) {
                if (!loot.get(i).isEmpty() && loot.get(i).getCount() <= 0) {
                    loot.set(i, ItemStack.EMPTY);
                }
                if (!stack.isEmpty() && stack.getCount() > 0
                        && !loot.get(i).isEmpty()
                        && loot.get(i).getCount() < loot.get(i).getMaxStackSize()
                        && arcaneresearch.common.lib.utils.InventoryUtils.areItemStacksEqualStrict(stack, loot.get(i))) {
                    if (stack.getCount() + loot.get(i).getCount() <= loot.get(i).getMaxStackSize()) {
                        loot.get(i).grow(stack.getCount());
                        return ItemStack.EMPTY;
                    }
                    int fit = Math.min(stack.getCount(),
                            loot.get(i).getMaxStackSize() - loot.get(i).getCount());
                    loot.get(i).grow(fit);
                    stack.shrink(fit);
                }
                if (!stack.isEmpty() && stack.getCount() <= 0) {
                    stack = ItemStack.EMPTY;
                }
            }
            for (int i = 0; i < loot.size(); i++) {
                if (!loot.get(i).isEmpty() && loot.get(i).getCount() <= 0) {
                    loot.set(i, ItemStack.EMPTY);
                }
                if (!stack.isEmpty() && stack.getCount() > 0 && loot.get(i).isEmpty()) {
                    loot.set(i, stack.copy());
                    return ItemStack.EMPTY;
                }
            }
            if (!stack.isEmpty() && stack.getCount() <= 0) {
                stack = ItemStack.EMPTY;
            }
            return stack;
        }
        if (random.nextInt(10) < getValue(stack)) {
            setTamed(true);
            setCombatTask();
            level().broadcastEntityEvent(this, (byte) 18);
        }
        stack.shrink(1);
        if (stack.getCount() <= 0) {
            return ItemStack.EMPTY;
        }
        return stack;
    }
}
