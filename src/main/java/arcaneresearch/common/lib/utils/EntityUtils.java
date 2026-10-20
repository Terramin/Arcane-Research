package arcaneresearch.common.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.common.entities.monster.boss.EntityArcaneresearchBoss;
import arcaneresearch.common.entities.monster.mods.ChampionModifier;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class EntityUtils {

    // NEOFORGE_PORT: modifier ids are Identifiers in 26.x (record ctor).
    public static final net.minecraft.resources.Identifier CHAMPION_HEALTH_ID =
            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                    arcaneresearch.Arcaneresearch.MODID, "champion/health");
    public static final net.minecraft.resources.Identifier CHAMPION_DAMAGE_ID =
            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                    arcaneresearch.Arcaneresearch.MODID, "champion/damage");
    public static final net.minecraft.resources.Identifier BOLDBUFF_ID =
            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                    arcaneresearch.Arcaneresearch.MODID, "champion/bold");
    public static final net.minecraft.resources.Identifier MIGHTYBUFF_ID =
            net.minecraft.resources.Identifier.fromNamespaceAndPath(
                    arcaneresearch.Arcaneresearch.MODID, "champion/mighty");
    private static java.lang.reflect.Field floatCounterField = null;

    /** Луч взгляда в блок (общий для таумометра проекта и temp-изучения). */
    public static BlockHitResult blockRay(Level level, Player player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = eyePos.add(player.getLookAngle().scale(range));
        return level.clip(new ClipContext(eyePos, targetPos,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }

    // NEOFORGE_PORT: dropCustomDeathLoot lost its looting param in 26.x; derive it from
    // the killer's main-hand item (Enchantments.LOOTING), same value the old caller passed.
    public static int getLootingLevel(net.minecraft.server.level.ServerLevel level,
                                      net.minecraft.world.damagesource.DamageSource source) {
        if (source.getEntity() instanceof LivingEntity killer) {
            var enchantments = level.registryAccess()
                    .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    enchantments.getOrThrow(
                            net.minecraft.world.item.enchantment.Enchantments.LOOTING),
                    killer.getMainHandItem());
        }
        return 0;
    }

    @Nullable
    public static Entity getPointedEntity(Level level, Entity entity, double minrange,
                                          double range, float padding, boolean nonCollide) {
        Vec3 eyePos = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 lookVec = entity.getLookAngle();
        Vec3 endVec = eyePos.add(lookVec.scale(range));
        AABB box = new AABB(eyePos.x, eyePos.y, eyePos.z, eyePos.x, eyePos.y, eyePos.z)
                .expandTowards(lookVec.scale(range)).inflate(padding);
        List<Entity> list = level.getEntities(entity, box);
        Entity pointed = null;
        double best = 0.0;
        for (Entity candidate : list) {
            if (eyePos.distanceTo(candidate.position()) < minrange) {
                continue;
            }
            if (!candidate.isPickable() && !nonCollide) {
                continue;
            }

            Vec3 targetEyes = new Vec3(candidate.getX(), candidate.getY() + candidate.getEyeHeight(), candidate.getZ());
            BlockHitResult blockHit = level.clip(new ClipContext(eyePos, targetEyes,
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
            if (blockHit.getType() != HitResult.Type.MISS) {
                continue;
            }
            float border = Math.max(0.8f, candidate.getPickRadius());
            AABB candidateBox = candidate.getBoundingBox().inflate(border);
            if (candidateBox.contains(eyePos)) {
                if (0.0 < best || best == 0.0) {
                    pointed = candidate;
                    best = 0.0;
                }
            } else {
                var intercept = candidateBox.clip(eyePos, endVec);
                if (intercept.isPresent()) {
                    double dist = eyePos.distanceTo(intercept.get());
                    if (dist < best || best == 0.0) {
                        pointed = candidate;
                        best = dist;
                    }
                }
            }
        }
        return pointed;
    }

    @Nullable
    public static net.minecraft.world.phys.EntityHitResult getPointedEntityRay(Level level, Entity ignoreEntity,
                                                                               Vec3 startVec, Vec3 lookVec, double minrange, double range, float padding, boolean nonCollide) {
        Vec3 endVec = startVec.add(lookVec.scale(range));
        AABB box = new AABB(startVec.x, startVec.y, startVec.z, startVec.x, startVec.y, startVec.z)
                .expandTowards(lookVec.scale(range)).inflate(padding);
        List<Entity> list = level.getEntities(ignoreEntity, box);
        net.minecraft.world.phys.EntityHitResult pointed = null;
        double best = 0.0;
        for (Entity candidate : list) {
            if (startVec.distanceTo(candidate.position()) < minrange) {
                continue;
            }
            if (!candidate.isPickable() && !nonCollide) {
                continue;
            }

            Vec3 targetEyes = new Vec3(candidate.getX(), candidate.getY() + candidate.getEyeHeight(), candidate.getZ());
            BlockHitResult blockHit = level.clip(new ClipContext(startVec, targetEyes,
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, ignoreEntity));
            if (blockHit.getType() != HitResult.Type.MISS) {
                continue;
            }
            float border = Math.max(0.8f, candidate.getPickRadius());
            AABB candidateBox = candidate.getBoundingBox().inflate(border);
            if (candidateBox.contains(startVec)) {
                if (0.0 < best || best == 0.0) {
                    pointed = new net.minecraft.world.phys.EntityHitResult(candidate, candidate.position());
                    best = 0.0;
                }
            } else {
                var intercept = candidateBox.clip(startVec, endVec);
                if (intercept.isPresent()) {
                    double dist = startVec.distanceTo(intercept.get());
                    if (dist < best || best == 0.0) {
                        pointed = new net.minecraft.world.phys.EntityHitResult(candidate, intercept.get());
                        best = dist;
                    }
                }
            }
        }
        return pointed;
    }

    public static boolean isInFlightCone(Entity self, Entity target, float aperture, double range) {
        Vec3 t = new Vec3(self.getX(), self.getBoundingBox().minY + self.getEyeHeight(), self.getZ());
        Vec3 axis = self.getLookAngle().scale(range);
        Vec3 apexToX = new Vec3(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0,
                target.getZ()).subtract(t);
        if (apexToX.lengthSqr() == 0 || axis.lengthSqr() == 0) {
            return true;
        }
        double halfAperture = aperture / 2.0;
        Vec3 axisNorm = axis.normalize();
        if (apexToX.normalize().dot(axisNorm) <= Math.cos(halfAperture)) {
            return false;
        }
        return apexToX.dot(axisNorm) < axis.length();
    }

    public static boolean pushOutOfBlocks(Entity entity) {
        Level level = entity.level();
        if (level.noCollision(entity, entity.getBoundingBox())) {
            return false;
        }
        BlockPos pos = entity.blockPosition();
        double px = entity.getX() - pos.getX();
        double py = entity.getY() - pos.getY();
        double pz = entity.getZ() - pos.getZ();

        Direction bestDir = null;
        double bestDist = Double.MAX_VALUE;

        Direction[] dirs = {
                Direction.WEST, Direction.EAST,
                Direction.DOWN, Direction.UP,
                Direction.NORTH, Direction.SOUTH};
        double[] depths = {px, 1.0 - px, py, 1.0 - py, pz, 1.0 - pz};
        for (int i = 0; i < dirs.length; i++) {
            BlockPos neighbour = pos.relative(dirs[i]);
            if (level.getBlockState(neighbour).getCollisionShape(level, neighbour).isEmpty()
                    && depths[i] < bestDist) {
                bestDist = depths[i];
                bestDir = dirs[i];
            }
        }
        if (bestDir == null) {
            return false;
        }
        BlockPos target = pos.relative(bestDir);
        switch (bestDir) {
            case UP -> entity.setPos(entity.getX(), target.getY(), entity.getZ());
            case DOWN -> entity.setPos(entity.getX(), target.getY() + 1 - entity.getBbHeight(), entity.getZ());
            default -> entity.setPos(target.getX() + 0.5, entity.getY(), target.getZ() + 0.5);
        }
        return true;
    }

    public static <T extends Entity> List<T> getEntitiesInRange(Level level, BlockPos pos, double radius, Class<T> type) {
        AABB box = new AABB(
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + radius + 1, pos.getY() + radius + 1, pos.getZ() + radius + 1
        );
        // NEOFORGE_PORT (26.3): getEntitiesOfClass(Class, AABB) still exists and, as in
        // 1.20.1, filters with EntitySelector.NO_SPECTATORS (the e -> true lookup didn't).
        return level.getEntitiesOfClass(type, box);
    }

    public static <T extends Entity> List<T> getEntitiesInRange(Level level, BlockPos pos, double radius,
                                                                Class<T> type, Predicate<T> filter) {
        AABB box = new AABB(
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + radius + 1, pos.getY() + radius + 1, pos.getZ() + radius + 1
        );
        return level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(type), box, filter);
    }

    public static void resetFloatCounter(ServerPlayer player) {
        try {
            if (floatCounterField == null) {
                floatCounterField = net.minecraft.server.network.ServerGamePacketListenerImpl.class
                        .getDeclaredField("aboveGroundTickCount");
                floatCounterField.setAccessible(true);
            }
            floatCounterField.setInt(player.connection, 0);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public static <T extends Entity> T getNearestEntity(Level level, BlockPos pos, double radius, Class<T> type) {
        List<T> entities = getEntitiesInRange(level, pos, radius, type);
        if (entities.isEmpty()) return null;

        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return entities.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    @Nullable
    public static LivingEntity getNearestLivingEntity(Level level, BlockPos pos, double radius,
                                                      @Nullable Entity exclude) {
        List<LivingEntity> entities = getEntitiesInRange(level, pos, radius, LivingEntity.class);
        if (entities.isEmpty()) return null;

        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return entities.stream()
                .filter(e -> e != exclude && e.isAlive())
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(center)))
                .orElse(null);
    }

    public static boolean isHostile(Entity entity) {
        return entity instanceof Enemy;
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof Player;
    }

    public static boolean isAliveAndValid(Entity entity) {
        return entity != null && entity.isAlive() && !entity.isRemoved();
    }

    public static Vec3 getLookVector(Entity entity) {
        return entity.getLookAngle();
    }

    public static Vec3 getEyePosition(Entity entity) {
        return entity.getEyePosition();
    }

    public static void pushAway(Entity entity, BlockPos pos, double force) {
        Vec3 entityPos = entity.position();
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        Vec3 direction = entityPos.subtract(center).normalize();
        entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(force)));
        // NEOFORGE_PORT (26.3): Entity.hurtMarked was renamed syncVelocity (still what
        // makes ServerEntity send the motion packet; setDeltaMovement alone doesn't).
        entity.syncVelocity = true;
    }

    public static void pullToward(Entity entity, BlockPos pos, double force) {
        Vec3 entityPos = entity.position();
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        Vec3 direction = center.subtract(entityPos).normalize();
        entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(force)));
        // NEOFORGE_PORT (26.3): hurtMarked -> syncVelocity (see pushAway).
        entity.syncVelocity = true;
    }

    public static double getMaxHealth(LivingEntity entity) {
        return entity.getMaxHealth();
    }

    public static double getCurrentHealth(LivingEntity entity) {
        return entity.getHealth();
    }

    public static double getAttackDamage(LivingEntity entity) {
        var attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        return attr != null ? attr.getValue() : 1.0;
    }

    public static boolean isCreative(Player player) {
        return player.getAbilities().instabuild;
    }

    public static boolean isSurvival(Player player) {
        return !player.getAbilities().instabuild;
    }

    public static boolean isServerPlayer(Entity entity) {
        return entity instanceof ServerPlayer;
    }

    public static List<Player> getPlayersInRange(Level level, BlockPos pos, double radius) {
        return getEntitiesInRange(level, pos, radius, Player.class);
    }

    @Nullable
    public static Player getNearestPlayer(Level level, BlockPos pos, double radius) {
        return getNearestEntity(level, pos, radius, Player.class);
    }

    public static boolean canSee(LivingEntity entity, BlockPos pos) {
        Vec3 eyePos = entity.getEyePosition();
        Vec3 targetPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return entity.level().clip(new net.minecraft.world.level.ClipContext(
                eyePos, targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                entity
        )).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    public static void setTarget(Entity attacker, @Nullable LivingEntity target) {
        if (attacker instanceof Mob mob) {
            mob.setTarget(target);
        }
    }

    public static boolean canEntityBeSeen(Entity entity, double x, double y, double z) {
        Level level = entity.level();
        Vec3 entityPos = entity.getEyePosition();
        Vec3 targetPos = new Vec3(x, y, z);

        return level.clip(new net.minecraft.world.level.ClipContext(
                entityPos, targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                entity
        )).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    public static boolean hasGoggles(net.minecraft.world.entity.Entity e) {
        if (!(e instanceof Player viewer)) {
            return false;
        }
        if (viewer.getMainHandItem().getItem() instanceof arcaneresearch.api.items.IGoggles
                && showPopups(viewer.getMainHandItem(), viewer)) {
            return true;
        }
        if (viewer.getOffhandItem().getItem() instanceof arcaneresearch.api.items.IGoggles
                && showPopups(viewer.getOffhandItem(), viewer)) {
            return true;
        }
        // NEOFORGE_PORT: LivingEntity.getArmorSlots gone -> per-slot reads.
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            ItemStack armor = viewer.getItemBySlot(slot);
            if (!armor.isEmpty() && armor.getItem() instanceof arcaneresearch.api.items.IGoggles
                    && showPopups(armor, viewer)) {
                return true;
            }
        }
        List<ItemStack> curios = arcaneresearch.common.lib.compat.CuriosCompat.findCuriosItems(viewer,
                s -> !s.isEmpty() && s.getItem() instanceof arcaneresearch.api.items.IGoggles);
        for (ItemStack stack : curios) {
            if (showPopups(stack, viewer)) {
                return true;
            }
        }
        return false;
    }

    private static boolean showPopups(ItemStack stack, Player player) {
        if (stack.getItem() instanceof arcaneresearch.api.items.IGoggles goggles) {
            try {
                return goggles.showIngamePopups(stack, player);
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    public static void makeChampion(Monster entity, boolean persist) {

        if (ChampionModifier.getChampionId(entity) != -1) {
            return;
        }
        int type = entity.level().getRandom().nextInt(ChampionModifier.MODS.length);
        if (entity instanceof Creeper) {
            type = 0;
        }
        ChampionModifier.setChampionId(entity, type);
        if (!(entity instanceof EntityArcaneresearchBoss)) {
            AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.removeModifier(CHAMPION_HEALTH_ID);
                health.addTransientModifier(new AttributeModifier(CHAMPION_HEALTH_ID,
                        100.0, AttributeModifier.Operation.ADD_VALUE));
            }
            AttributeInstance damage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.removeModifier(CHAMPION_DAMAGE_ID);
                damage.addTransientModifier(new AttributeModifier(CHAMPION_DAMAGE_ID,
                        2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            entity.heal(25.0f);
            ChampionModifier mod = ChampionModifier.getById(type);
            if (mod != null) {
                entity.setCustomName(Component.literal(
                        mod.getModNameLocalized() + " " + entity.getName().getString()));
            }
        } else {
            ((EntityArcaneresearchBoss) entity).generateName();
        }
        if (persist) {
            entity.setPersistenceRequired();
        }
        switch (type) {
            case 0 -> {
                AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) {
                    speed.removeModifier(BOLDBUFF_ID);
                    speed.addTransientModifier(new AttributeModifier(BOLDBUFF_ID,
                            0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
            case 3 -> {
                AttributeInstance damage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
                if (damage != null) {
                    damage.removeModifier(MIGHTYBUFF_ID);
                    damage.addTransientModifier(new AttributeModifier(MIGHTYBUFF_ID,
                            2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
            case 5 -> {
                AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
                if (health != null) {
                    int half = (int) health.getBaseValue() / 2;
                    entity.setAbsorptionAmount(entity.getAbsorptionAmount() + half);
                }
            }
            default -> {
            }
        }
    }

    public static boolean isFriendly(Entity source, Entity target) {
        if (source == null || target == null) return false;
        if (source == target) return true;

        if (source.isAlliedTo(target)) return true;

        if (target instanceof net.minecraft.world.entity.TamableAnimal tamable) {
            if (tamable.isTame() && tamable.getOwner() == source) {
                return true;
            }
        }

        if (source instanceof net.minecraft.world.entity.TamableAnimal tamable) {
            if (tamable.isTame() && tamable.getOwner() == target) {
                return true;
            }
        }

        if (source instanceof net.minecraft.world.entity.TamableAnimal sourceTamable &&
                target instanceof net.minecraft.world.entity.TamableAnimal targetTamable) {
            if (sourceTamable.isTame() && targetTamable.isTame() &&
                    sourceTamable.getOwner() == targetTamable.getOwner()) {
                return true;
            }
        }

        return false;
    }
}
