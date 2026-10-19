package arcaneresearch.common.entities.monster.boss;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.projectile.EntityGolemOrb;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.List;
public class EntityCultistLeader extends EntityArcaneresearchBoss implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> DATA_TITLE =
            SynchedEntityData.defineId(EntityCultistLeader.class, EntityDataSerializers.BYTE);
    private static final String[] TITLES = {
            "Alberic", "Anselm", "Bastian", "Beturian", "Chabier",
            "Chorache", "Chuse", "Dodorol", "Ebardo", "Ferrando",
            "Fertus", "Guillen", "Larpe", "Obano", "Zelipe"
    };
    public EntityCultistLeader(EntityType<? extends EntityCultistLeader> type, Level level) {
        super(type, level);
        this.xpReward = 40;
    }
    public EntityCultistLeader(Level level) {
        this(ModEntities.CULTIST_LEADER.get(), level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return EntityArcaneresearchBoss.createBossAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TITLE, (byte) 0);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new arcaneresearch.common.entities.ai.combat.AILongRangeAttack(this, 16.0, 1.0, 30, 40, 24.0f));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new arcaneresearch.common.entities.ai.combat.AICultistHurtByTarget(this, true, EntityCultist.class, EntityCultistLeader.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override
    public void generateName() {
        int t = arcaneresearch.common.entities.monster.mods.ChampionModifier.getChampionId(this);
        if (t >= 0 && t < arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS.length) {
            setCustomName(net.minecraft.network.chat.Component.literal(String.format(
                    net.minecraft.network.chat.Component.translatable("entity.arcaneresearch.cultist_leader.name.custom").getString(),
                    getTitle(), arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t].getModNameLocalized())));
        } else {
            setCustomName(net.minecraft.network.chat.Component.literal(getTitle() + " the Crimson Praetor"));
        }
    }
    public String getTitle() {
        return TITLES[Math.floorMod(this.entityData.get(DATA_TITLE), TITLES.length)];
    }
    public void setTitle(int title) {
        this.entityData.set(DATA_TITLE, (byte) Math.floorMod(title, TITLES.length));
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("title", this.entityData.get(DATA_TITLE));
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTitle(tag.getByte("title"));
    }
    protected void setLoot(DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON_PRAETOR_HELM.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.CRIMSON_PRAETOR_CHEST.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.CRIMSON_PRAETOR_LEGS.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.CRIMSON_BOOTS.get()));
        if (level().getDifficulty() == Difficulty.EASY) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.VOID_SWORD.get()));
        } else {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CRIMSON_BLADE.get()));
        }
    }
    protected void setEnchantmentBasedOnDifficulty(DifficultyInstance difficulty) {
        float diff = difficulty.getSpecialMultiplier();
        ItemStack weapon = getMainHandItem();
        if (!weapon.isEmpty() && random.nextFloat() < 0.5f * diff) {
            EnchantmentHelper.enchantItem(random, weapon, (int) (7.0f + diff * random.nextInt(22)), false);
        }
    }
    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof EntityCultist || entity instanceof EntityCultistLeader) {
            return true;
        }
        return super.isAlliedTo(entity);
    }
    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof EntityCultist || target instanceof EntityCultistLeader) {
            return false;
        }
        return super.canAttack(target);
    }
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        setLoot(difficulty);
        setEnchantmentBasedOnDifficulty(difficulty);
        setTitle(random.nextInt(TITLES.length));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
    }
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 20 == 0) {
            AABB searchBox = getBoundingBox().inflate(8.0);
            List<EntityCultist> nearbyCultists = level().getEntitiesOfClass(EntityCultist.class, searchBox);
            for (EntityCultist cultist : nearbyCultists) {
                try {
                    if (!cultist.hasEffect(MobEffects.REGENERATION)) {
                        cultist.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (hasLineOfSight(target)) {
            swing(InteractionHand.MAIN_HAND);
            getLookControl().setLookAt(target, 30.0f, 30.0f);
            EntityGolemOrb orb = new EntityGolemOrb(level(), this, target, true);
            double dx = target.getX() - getX();
            double dy = target.getY(0.5) - getY(0.5);
            double dz = target.getZ() - getZ();
            orb.shoot(dx, dy + 2.0, dz, 0.66f, 3.0f);
            playSound(ModSounds.EG_ATTACK.get(), 1.0f, 1.0f + random.nextFloat() * 0.1f);
            level().addFreshEntity(orb);
        }
    }
    public void spawnExplosionParticle() {
        if (level().isClientSide) {
            spawnSpawnFX();
        } else {
            level().broadcastEntityEvent(this, (byte) 20);
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnSpawnFX() {
        for (int i = 0; i < 20; ++i) {
            double d0 = random.nextGaussian() * 0.05;
            double d2 = random.nextGaussian() * 0.05;
            double d3 = random.nextGaussian() * 0.05;
            double d4 = 2.0;
            FXDispatcher.INSTANCE.cultistSpawn(
                    getX() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth() + d0 * d4,
                    getY() + random.nextFloat() * getBbHeight() + d2 * d4,
                    getZ() + random.nextFloat() * getBbWidth() * 2.0f - getBbWidth() + d3 * d4,
                    d0, d2, d3);
        }
    }
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 20) {
            spawnExplosionParticle();
        } else {
            super.handleEntityEvent(id);
        }
    }
    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int lootingLevel, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(source, lootingLevel, wasRecentlyHit);
        spawnAtLocation(new ItemStack(ModItems.LOOT_BAG_UNCOMMON.get()));
    }
}
