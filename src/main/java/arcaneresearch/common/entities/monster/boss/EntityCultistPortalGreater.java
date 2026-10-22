package arcaneresearch.common.entities.monster.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.monster.cult.EntityCultistCleric;
import arcaneresearch.common.entities.monster.cult.EntityCultistKnight;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;

import java.util.List;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class EntityCultistPortalGreater extends Monster {

    protected final ServerBossEvent bossEvent;
    public int pulse = 0;
    private int stage = 0;
    private int stageCounter = 200;

    public EntityCultistPortalGreater(EntityType<? extends EntityCultistPortalGreater> type, Level level) {
        super(type, level);
        this.xpReward = 30;
        this.setNoGravity(true);
        this.bossEvent = new ServerBossEvent(this.getUUID(), getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6);
        this.bossEvent.setDarkenScreen(true);
    }

    public EntityCultistPortalGreater(Level level) {
        this(ModEntities.CULTIST_PORTAL_GREATER.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 5.0);
    }

    @Override
    protected void registerGoals() {

    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("stage", stage);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        stage = tag.getIntOr("stage", 0);
        if (hasCustomName()) {
            this.bossEvent.setName(getDisplayName());
        }
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {

    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel serverLevel) {

    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (stageCounter > 0) {
                --stageCounter;

                if (stageCounter == 160 && stage == 0) {
                    level().broadcastEntityEvent(this, (byte) 16);
                    for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                        BlockPos bp = new BlockPos((int) getX() - dir.getStepX() * 6,
                                (int) getY(), (int) getZ() + dir.getStepZ() * 6);
                        level().setBlock(bp,
                                arcaneresearch.init.ModBlocks.BANNER_CRIMSON_CULT.get().defaultBlockState(), 3);
                        net.minecraft.world.level.block.entity.BlockEntity te =
                                level().getBlockEntity(bp);
                        if (te instanceof arcaneresearch.common.tiles.misc.TileBanner tileBanner) {
                            byte face = switch (dir) {
                                case NORTH -> (byte) 8;
                                case SOUTH -> (byte) 0;
                                case WEST -> (byte) 12;
                                default -> (byte) 4;
                            };
                            tileBanner.setBannerFacing(face);
                            if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                                arcaneresearch.common.lib.network.PacketHandler.sendToAllTrackingChunk(
                                        new arcaneresearch.common.lib.network.fx.PacketFXBlockArc(bp, this,
                                                0.5f + random.nextFloat() * 0.2f, 0.0f, 0.0f),
                                        serverLevel, blockPosition());
                            }
                            playSound(arcaneresearch.init.ModSounds.WAND_FAIL.get(), 1.0f, 1.0f);
                        }
                    }
                }

                if (stageCounter > 20 && stageCounter < 150 && stage == 0 && stageCounter % 13 == 0) {
                    int a = (int) getX() + random.nextInt(5) - random.nextInt(5);
                    int b = (int) getZ() + random.nextInt(5) - random.nextInt(5);
                    BlockPos bp = new BlockPos(a, (int) getY(), b);

                    if (a != (int) getX() && b != (int) getZ() && level().isEmptyBlock(bp)) {
                        level().broadcastEntityEvent(this, (byte) 16);
                        float rr = random.nextFloat();
                        net.minecraft.world.level.block.Block crate =
                                arcaneresearch.init.ModBlocks.LOOT_CRATE_COMMON.get();
                        if (rr < 0.05f) {
                            crate = arcaneresearch.init.ModBlocks.LOOT_CRATE_RARE.get();
                        } else if (rr < 0.2f) {
                            crate = arcaneresearch.init.ModBlocks.LOOT_CRATE_UNCOMMON.get();
                        }
                        level().setBlock(bp, crate.defaultBlockState(), 3);
                        if (level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            arcaneresearch.common.lib.network.PacketHandler.sendToAllTrackingChunk(
                                    new arcaneresearch.common.lib.network.fx.PacketFXBlockArc(bp, this,
                                            0.5f + random.nextFloat() * 0.2f, 0.0f, 0.0f),
                                    serverLevel, blockPosition());
                        }
                        playSound(arcaneresearch.init.ModSounds.WAND_FAIL.get(), 1.0f, 1.0f);
                    }
                }
            } else if (level().getNearestPlayer(this, 48.0) != null) {

                level().broadcastEntityEvent(this, (byte) 16);

                switch (stage) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:

                        stageCounter = 15 + random.nextInt(10 - stage) - stage;
                        spawnMinion();
                        break;
                    case 12:

                        stageCounter = 50 + getTiming() * 2 + random.nextInt(50);
                        spawnBoss();
                        break;
                    default:

                        int t = getTiming();
                        stageCounter = t + random.nextInt(5 + t / 3);
                        spawnMinion();
                        break;
                }
                ++stage;
            } else {

                stageCounter = 30 + random.nextInt(30);
            }

            if (stage < 12) {
                heal(1.0f);
            }

            this.bossEvent.setProgress(getHealth() / getMaxHealth());
        }

        if (pulse > 0) {
            --pulse;
        }

        if (level().isClientSide()) {

            if (random.nextInt(2) == 0) {
                double dx = (random.nextDouble() - 0.5) * getBbWidth();
                double dz = (random.nextDouble() - 0.5) * getBbWidth();
                level().addParticle(ParticleTypes.PORTAL,
                        getX() + dx, getY() + random.nextDouble() * getBbHeight(), getZ() + dz,
                        dx * 0.5, random.nextDouble() * 0.5, dz * 0.5);
            }

            if (random.nextInt(4) == 0) {
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        getX() + (random.nextDouble() - 0.5) * 1.5,
                        getY() + random.nextDouble() * getBbHeight(),
                        getZ() + (random.nextDouble() - 0.5) * 1.5,
                        0, 0.02, 0);
            }
        }
    }

    private int getTiming() {
        AABB searchBox = getBoundingBox().inflate(32.0);
        List<EntityCultist> cultists = level().getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityCultist.class), searchBox, e -> true);
        return cultists.size() * 20;
    }

    private void spawnMinion() {
        EntityCultist cultist;
        if (random.nextFloat() > 0.33f) {
            cultist = new EntityCultistKnight(level());
        } else {
            cultist = new EntityCultistCleric(level());
        }

        
        cultist.snapTo(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25, getZ() + random.nextFloat() - random.nextFloat(), random.nextFloat() * 360.0f, 0.0f);

        cultist.finalizeSpawn((net.minecraft.world.level.ServerLevelAccessor) level(), ((net.minecraft.world.level.ServerLevelAccessor) level()).getCurrentDifficultyAt(new BlockPos((int) cultist.getX(), (int) cultist.getY(), (int) cultist.getZ())), net.minecraft.world.entity.EntitySpawnReason.SPAWNER, null);

        cultist.setHomePos(blockPosition(), 32);
        level().addFreshEntity(cultist);
        cultist.spawnExplosionParticle();
        cultist.playSound(arcaneresearch.init.ModSounds.WAND_FAIL.get(), 1.0f, 1.0f);

        if (stage > 12) {
            hurt(damageSources().fellOutOfWorld(), 5 + random.nextInt(5));
        }
    }

    private void spawnBoss() {
        EntityCultistLeader leader = new EntityCultistLeader(level());

        
        leader.snapTo(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25, getZ() + random.nextFloat() - random.nextFloat(), random.nextFloat() * 360.0f, 0.0f);

        leader.finalizeSpawn((net.minecraft.world.level.ServerLevelAccessor) level(), ((net.minecraft.world.level.ServerLevelAccessor) level()).getCurrentDifficultyAt(new BlockPos((int) leader.getX(), (int) leader.getY(), (int) leader.getZ())), net.minecraft.world.entity.EntitySpawnReason.SPAWNER, null);

        leader.setHomePos(blockPosition(), 32);
        level().addFreshEntity(leader);
        leader.spawnExplosionParticle();
        leader.playSound(arcaneresearch.init.ModSounds.WAND_FAIL.get(), 1.0f, 1.0f);
    }

    @Override
    public void playerTouch(Player player) {
        if (distanceToSqr(player) < 3.0) {
            if (player.level() instanceof net.minecraft.server.level.ServerLevel _sl ? player.hurtServer(_sl, damageSources().indirectMagic(this, this), 8.0f) : false) {
                playSound(arcaneresearch.init.ModSounds.ZAP.get(), 1.0f, (random.nextFloat() - random.nextFloat()) * 0.1f + 1.0f);
            }
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 540;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return arcaneresearch.init.ModSounds.MONOLITH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return arcaneresearch.init.ModSounds.ZAP.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return arcaneresearch.init.ModSounds.SHOCK.get();
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        
        
        int looting = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);

        spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.PRIMORDIAL_PEARL.get()));
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            pulse = 10;

            for (int i = 0; i < 15; i++) {
                double dx = (random.nextDouble() - 0.5) * 2.0;
                double dy = random.nextDouble() * getBbHeight();
                double dz = (random.nextDouble() - 0.5) * 2.0;
                level().addParticle(ParticleTypes.WITCH,
                        getX() + dx, getY() + dy, getZ() + dz,
                        dx * 0.2, 0.2, dz * 0.2);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, Entity source) {
        return false;
    }

    
    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide()) {
            level().explode(this, getX(), getY(), getZ(), 2.0f, Level.ExplosionInteraction.NONE);
        }
        super.die(source);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
