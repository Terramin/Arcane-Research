package arcaneresearch.common.entities.monster.boss;
import arcaneresearch.common.world.maze.MazeFeature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.entities.IEldritchMob;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.monster.EntityEldritchGuardian;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.projectile.EntityEldritchOrb;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXSonic;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModSounds;

import javax.annotation.Nullable;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EntityEldritchWarden extends EntityArcaneresearchBoss implements RangedAttackMob, IEldritchMob {

    private static final EntityDataAccessor<Byte> DATA_TITLE =
            SynchedEntityData.defineId(EntityEldritchWarden.class, EntityDataSerializers.BYTE);
    private static final String[] TITLES = {
            "Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha",
            "Oorn", "Shaikorth", "Rhan-Tegoth", "Rhogog", "Shudde M'ell",
            "Vulthoom", "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon"
    };
    protected final ServerBossEvent bossEventShield;
    public float armLiftL = 0.0f;
    public float armLiftR = 0.0f;
    private boolean fieldFrenzy = false;
    private int fieldFrenzyCounter = 0;
    private boolean lastBlast = false;

    public EntityEldritchWarden(EntityType<? extends EntityEldritchWarden> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.bossEventShield = new ServerBossEvent(this.getUUID(), Component.literal(""), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    }

    public EntityEldritchWarden(Level level) {
        this(ModEntities.ELDRITCH_WARDEN.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EntityArcaneresearchBoss.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TITLE, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new arcaneresearch.common.entities.ai.combat.AILongRangeAttack(this, 3.0, 1.0, 20, 40, 24.0f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                if (isOuterLandsOnlyPlayers()
                        && !(this.mob.getLastHurtByMob() instanceof Player)) {
                    return false;
                }
                return super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                if (isOuterLandsOnlyPlayers() && !(this.mob.getTarget() instanceof Player)) {
                    this.mob.setTarget(null);
                    return false;
                }
                return super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true) {
            @Override
            public boolean canUse() {
                return !isOuterLandsOnlyPlayers() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isOuterLandsOnlyPlayers() && super.canContinueToUse();
            }
        });
    }

     



 
    private boolean isOuterLandsOnlyPlayers() {
        try {
            return level().dimension().equals(
                    arcaneresearch.common.world.maze.MazeFeature.DIMENSION);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void generateName() {
        int t = arcaneresearch.common.entities.monster.mods.ChampionModifier.getChampionId(this);
        if (t >= 0 && t < arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS.length) {
            setCustomName(Component.literal(String.format(
                    Component.translatable("entity.arcaneresearch.eldritch_warden.name.custom").getString(),
                    getTitle(), arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t].getModNameLocalized())));
        } else {
            setCustomName(Component.literal(getTitle() + " the Eldritch Warden"));
        }
    }

    public String getTitle() {

        return TITLES[Math.floorMod(this.entityData.get(DATA_TITLE), TITLES.length)];
    }

    public void setTitle(int title) {
        this.entityData.set(DATA_TITLE, (byte) Math.floorMod(title, TITLES.length));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEventShield.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEventShield.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("title", this.entityData.get(DATA_TITLE));
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        setTitle(tag.getByteOr("title", (byte) 0));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnData) {

        spawnTimer = 150;
        setTitle(random.nextInt(TITLES.length));

        float shieldAmount = (float) (getAttributeValue(Attributes.MAX_HEALTH) * 0.66);
        setAbsorptionAmount(shieldAmount);

        return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    
    

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {
        if (fieldFrenzyCounter == 0) {
            super.customServerAiStep(serverLevel);
        }

        int maxShield = (int) (getAttributeValue(Attributes.MAX_HEALTH) * 0.66);
        if (hurtTime <= 0 && tickCount % 25 == 0 && getAbsorptionAmount() < maxShield) {
            setAbsorptionAmount(getAbsorptionAmount() + 1.0f);
        }

        this.bossEventShield.setProgress(getAbsorptionAmount() / maxShield);
    }

    @Override
    public void tick() {
        if (spawnTimer == 150) {
            level().broadcastEntityEvent(this, (byte) 18);
        }

        super.tick();

        if (level().isClientSide()) {
            clientTick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clientTick() {
        if (armLiftL > 0.0f) {
            armLiftL -= 0.05f;
        }
        if (armLiftR > 0.0f) {
            armLiftR -= 0.05f;
        }

        float x = (float) (getX() + (random.nextFloat() - random.nextFloat()) * 0.2f);
        float z = (float) (getZ() + (random.nextFloat() - random.nextFloat()) * 0.2f);
        FXDispatcher.INSTANCE.wispFXEG(x, (float) (getY() + 0.25 * getBbHeight()), z, this);

        if (spawnTimer > 0) {
            float he = Math.max(1.0f, getBbHeight() * ((150 - spawnTimer) / 150.0f));
            for (int a = 0; a < 33; ++a) {
                FXDispatcher.INSTANCE.smokeSpiral(getX(), getBoundingBox().minY + he / 2.0f, getZ(),
                        he, random.nextInt(360), Mth.floor(getBoundingBox().minY) - 1, 2232623);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level().isClientSide()) {
            int i = Mth.floor(getX());
            int j = Mth.floor(getY());
            int k = Mth.floor(getZ());

            for (int l = 0; l < 4; ++l) {
                int bi = Mth.floor(getX() + (l % 2 * 2 - 1) * 0.25f);
                int bj = Mth.floor(getY());
                int bk = Mth.floor(getZ() + (l / 2 % 2 * 2 - 1) * 0.25f);
                BlockPos bp = new BlockPos(bi, bj, bk);

                if (level().isEmptyBlock(bp)) {
                    level().setBlock(bp, ModBlocks.EFFECT_SAP.get().defaultBlockState(), 3);
                    level().scheduleTick(bp, ModBlocks.EFFECT_SAP.get(), 20 + random.nextInt(20));
                }
            }

            if (fieldFrenzyCounter > 0) {
                if (fieldFrenzyCounter == 150) {
                    teleportHome();
                }
                performFieldFrenzy();
            }
        }
    }

    private void performFieldFrenzy() {
        if (fieldFrenzyCounter < 121 && fieldFrenzyCounter % 10 == 0) {
            level().broadcastEntityEvent(this, (byte) 17);

            double radius = (150 - fieldFrenzyCounter) / 8.0;
            int d = 1 + fieldFrenzyCounter / 8;
            int i = Mth.floor(getX());
            int j = Mth.floor(getY());
            int k = Mth.floor(getZ());

            for (int q = 0; q < 180 / d; ++q) {
                double radians = Math.toRadians(q * 2 * d);
                int deltaX = (int) (radius * Math.cos(radians));
                int deltaZ = (int) (radius * Math.sin(radians));
                BlockPos bp = new BlockPos(i + deltaX, j, k + deltaZ);

                if (level().isEmptyBlock(bp)) {
                    BlockState below = level().getBlockState(bp.below());
                    if (below.isSolidRender()) {
                        level().setBlock(bp, ModBlocks.EFFECT_SAP.get().defaultBlockState(), 3);
                        level().scheduleTick(bp, ModBlocks.EFFECT_SAP.get(), 20 + random.nextInt(40));
                    }
                }
            }

            playSound(ModSounds.ZAP.get(), 1.0f, 0.9f + random.nextFloat() * 0.1f);
        }
        --fieldFrenzyCounter;
    }

    private void teleportHome() {
        if (!hasHome()) return;

        BlockPos home = getHomePosition();
        double oldX = getX();
        double oldY = getY();
        double oldZ = getZ();

        int tries = 20;
        int x = home.getX();
        int y = home.getY();
        int z = home.getZ();

        while (tries > 0) {
            BlockPos checkPos = new BlockPos(x, y, z);
            BlockState below = level().getBlockState(checkPos.below());
            BlockState at = level().getBlockState(checkPos);

            
            if (below.isSolid() && !below.is(net.minecraft.world.level.block.Blocks.COBWEB)
                    && !below.is(net.minecraft.world.level.block.Blocks.BAMBOO_SAPLING)
                    && !(at.isSolid() && !at.is(net.minecraft.world.level.block.Blocks.COBWEB)
                    && !at.is(net.minecraft.world.level.block.Blocks.BAMBOO_SAPLING))) {

                teleportTo(x + 0.5, y + 0.1, z + 0.5);

                for (int i = 0; i < 128; i++) {
                    double progress = i / 127.0;
                    double px = oldX + (getX() - oldX) * progress + (random.nextDouble() - 0.5) * getBbWidth() * 2.0;
                    double py = oldY + (getY() - oldY) * progress + random.nextDouble() * getBbHeight();
                    double pz = oldZ + (getZ() - oldZ) * progress + (random.nextDouble() - 0.5) * getBbWidth() * 2.0;
                    level().addParticle(ParticleTypes.PORTAL, px, py, pz,
                            (random.nextFloat() - 0.5f) * 0.2f,
                            (random.nextFloat() - 0.5f) * 0.2f,
                            (random.nextFloat() - 0.5f) * 0.2f);
                }
                playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                return;
            }

            x = home.getX() + random.nextInt(8) - random.nextInt(8);
            z = home.getZ() + random.nextInt(8) - random.nextInt(8);
            --tries;
        }
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source) {

        return fieldFrenzyCounter > 0 || source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)
                || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER)
                || super.isInvulnerableTo(serverLevel, source);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {
        if (isInvulnerableTo(serverLevel, source)) {
            return false;
        }

        boolean wasHurt = super.hurtServer(serverLevel, source, amount);

        if (!level().isClientSide() && wasHurt && !fieldFrenzy && getAbsorptionAmount() <= 0.0f) {
            fieldFrenzy = true;
            fieldFrenzyCounter = 150;
        }

        return wasHurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (random.nextFloat() > 0.2f) {

            EntityEldritchOrb orb = new EntityEldritchOrb(level(), this);

            lastBlast = !lastBlast;
            level().broadcastEntityEvent(this, (byte) (lastBlast ? 16 : 15));

            int rotOffset = lastBlast ? 90 : 180;
            double xx = Mth.cos((getYRot() + rotOffset) * Mth.DEG_TO_RAD) * 0.5f;
            double yy = 0.13;
            double zz = Mth.sin((getYRot() + rotOffset) * Mth.DEG_TO_RAD) * 0.5f;
            orb.setPos(orb.getX() - xx, orb.getY() - yy, orb.getZ() - zz);

            double dx = target.getX() + target.getDeltaMovement().x - getX();
            double dy = target.getY() - getY() - target.getBbHeight() / 2.0;
            double dz = target.getZ() + target.getDeltaMovement().z - getZ();

            orb.shoot(dx, dy, dz, 1.0f, 2.0f);

            playSound(ModSounds.EG_ATTACK.get(), 2.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(orb);
        } else if (hasLineOfSight(target)) {

            if (!level().isClientSide()) {
                PacketHandler.sendToAllTracking(new PacketFXSonic(this), this);
            }

            float knockX = -Mth.sin(getYRot() * Mth.DEG_TO_RAD) * 1.5f;
            float knockZ = Mth.cos(getYRot() * Mth.DEG_TO_RAD) * 1.5f;
            target.setDeltaMovement(target.getDeltaMovement().add(knockX, 0.1, knockZ));

            try {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
            } catch (Exception ignored) {
            }

            if (target instanceof Player player) {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
            }

            playSound(ModSounds.EG_SCREECH.get(), 4.0f, 1.0f + random.nextFloat() * 0.1f);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 15) {
            armLiftL = 0.5f;
        } else if (id == 16) {
            armLiftR = 0.5f;
        } else if (id == 17) {
            armLiftL = 0.9f;
            armLiftR = 0.9f;
        } else if (id == 18) {
            spawnTimer = 150;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof EntityEldritchGuardian) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.EG_IDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EG_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }
}
