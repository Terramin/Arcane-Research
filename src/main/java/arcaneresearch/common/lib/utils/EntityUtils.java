package arcaneresearch.common.lib.utils;

import arcaneresearch.api.items.IGoggles;
import arcaneresearch.common.entities.monster.boss.EntityArcaneresearchBoss;
import arcaneresearch.common.entities.monster.mods.ChampionModifier;
import arcaneresearch.common.lib.compat.CuriosCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class EntityUtils {

    public static final UUID CHAMPION_HEALTH_ID = UUID.fromString("a62bef38-48cc-42a6-ac5e-ef913841c4fd");
    public static final UUID CHAMPION_DAMAGE_ID = UUID.fromString("a340d2db-d881-4c25-ac62-f0ad14cd63b0");
    public static final UUID BOLDBUFF_ID       = UUID.fromString("4b1edd33-caa9-47ae-a702-d86c05701037");
    public static final UUID MIGHTYBUFF_ID     = UUID.fromString("7163897f-07f5-49b3-9ce4-b74beb83d2d3");

    private static final Direction[] PUSH_DIRS = {
            Direction.WEST, Direction.EAST,
            Direction.DOWN, Direction.UP,
            Direction.NORTH, Direction.SOUTH
    };

    private static java.lang.reflect.Field floatCounterField = null;

    public static BlockHitResult blockRay(Level level, Player player, double range) {
        if (level == null || player == null) return null;
        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = eyePos.add(player.getLookAngle().scale(range));
        return level.clip(new ClipContext(eyePos, targetPos,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    }

    @Nullable
    public static Entity getPointedEntity(Level level, Entity entity, double minrange,
                                          double range, float padding, boolean nonCollide) {
        if (level == null || entity == null) return null;

        Vec3 eyePos = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 lookVec = entity.getLookAngle();
        Vec3 endVec = eyePos.add(lookVec.scale(range));

        AABB box = new AABB(eyePos.x, eyePos.y, eyePos.z, eyePos.x, eyePos.y, eyePos.z)
                .expandTowards(lookVec.scale(range)).inflate(padding);

        List<Entity> list = level.getEntities(entity, box);
        if (list.isEmpty()) return null;

        Entity pointed = null;
        double best = Double.MAX_VALUE;
        boolean bestContained = false;
        final double minrangeSq = minrange * minrange;

        for (Entity candidate : list) {
            if (candidate == null) continue;
            if (!candidate.isPickable() && !nonCollide) continue;

            if (minrange > 0 && candidate.distanceToSqr(eyePos) < minrangeSq) continue;

            Vec3 targetEyes = new Vec3(candidate.getX(),
                    candidate.getY() + candidate.getEyeHeight(), candidate.getZ());
            BlockHitResult blockHit = level.clip(new ClipContext(eyePos, targetEyes,
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
            if (blockHit.getType() != HitResult.Type.MISS) continue;

            float border = Math.max(0.8f, candidate.getPickRadius());
            AABB candidateBox = candidate.getBoundingBox().inflate(border);
            boolean contained = candidateBox.contains(eyePos)
                    && candidate.position().subtract(eyePos).dot(lookVec) > 0.0D;

            double dist;
            if (contained) {
                dist = eyePos.distanceTo(candidate.position());
            } else {
                var intercept = candidateBox.clip(eyePos, endVec);
                if (intercept.isEmpty()) continue;
                dist = eyePos.distanceTo(intercept.get());
            }

            if (pointed == null
                    || (contained && !bestContained)
                    || (contained == bestContained && dist < best)) {
                pointed = candidate;
                best = dist;
                bestContained = contained;
            }
        }
        return pointed;
    }

    @Nullable
    public static EntityHitResult getPointedEntityRay(Level level, Entity ignoreEntity,
                                                      Vec3 startVec, Vec3 lookVec, double minrange,
                                                      double range, float padding, boolean nonCollide) {
        if (level == null || startVec == null || lookVec == null) return null;

        Vec3 endVec = startVec.add(lookVec.scale(range));
        AABB box = new AABB(startVec.x, startVec.y, startVec.z, startVec.x, startVec.y, startVec.z)
                .expandTowards(lookVec.scale(range)).inflate(padding);

        List<Entity> list = level.getEntities(ignoreEntity, box);
        if (list.isEmpty()) return null;

        EntityHitResult pointed = null;
        double best = Double.MAX_VALUE;
        boolean bestContained = false;
        final double minrangeSq = minrange * minrange;

        for (Entity candidate : list) {
            if (candidate == null) continue;
            if (!candidate.isPickable() && !nonCollide) continue;

            if (minrange > 0 && candidate.distanceToSqr(startVec) < minrangeSq) continue;

            Vec3 targetEyes = new Vec3(candidate.getX(),
                    candidate.getY() + candidate.getEyeHeight(), candidate.getZ());
            BlockHitResult blockHit = level.clip(new ClipContext(startVec, targetEyes,
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, ignoreEntity));
            if (blockHit.getType() != HitResult.Type.MISS) continue;

            float border = Math.max(0.8f, candidate.getPickRadius());
            AABB candidateBox = candidate.getBoundingBox().inflate(border);
            boolean contained = candidateBox.contains(startVec)
                    && candidate.position().subtract(startVec).dot(lookVec) > 0.0D;

            double dist;
            Vec3 hitPos;
            if (contained) {
                dist = startVec.distanceTo(candidate.position());
                hitPos = candidate.position();
            } else {
                var intercept = candidateBox.clip(startVec, endVec);
                if (intercept.isEmpty()) continue;
                dist = startVec.distanceTo(intercept.get());
                hitPos = intercept.get();
            }

            if (pointed == null
                    || (contained && !bestContained)
                    || (contained == bestContained && dist < best)) {
                pointed = new EntityHitResult(candidate, hitPos);
                best = dist;
                bestContained = contained;
            }
        }
        return pointed;
    }

    public static boolean isInFlightCone(Entity self, Entity target, float aperture, double range) {
        if (self == null || target == null) return false;

        Vec3 t = new Vec3(self.getX(), self.getBoundingBox().minY + self.getEyeHeight(), self.getZ());
        Vec3 axis = self.getLookAngle().scale(range);
        Vec3 apexToX = new Vec3(target.getX(),
                target.getBoundingBox().minY + target.getBbHeight() / 2.0, target.getZ()).subtract(t);

        if (apexToX.lengthSqr() == 0 || axis.lengthSqr() == 0) return true;

        double halfAperture = aperture / 2.0;
        Vec3 axisNorm = axis.normalize();
        if (apexToX.normalize().dot(axisNorm) <= Math.cos(halfAperture)) return false;
        return apexToX.dot(axisNorm) < axis.length();
    }

    public static boolean pushOutOfBlocks(Entity entity) {
        if (entity == null) return false;

        Level level = entity.level();
        if (level.noCollision(entity, entity.getBoundingBox())) return false;

        BlockPos pos = entity.blockPosition();
        double px = entity.getX() - pos.getX();
        double py = entity.getY() - pos.getY();
        double pz = entity.getZ() - pos.getZ();

        double[] depths = {px, 1.0 - px, py, 1.0 - py, pz, 1.0 - pz};

        Direction bestDir = null;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < PUSH_DIRS.length; i++) {
            Direction dir = PUSH_DIRS[i];
            BlockPos neighbour = pos.relative(dir);
            if (level.getBlockState(neighbour).getCollisionShape(level, neighbour).isEmpty()
                    && depths[i] < bestDist) {
                bestDist = depths[i];
                bestDir = dir;
            }
        }
        if (bestDir == null) return false;

        BlockPos target = pos.relative(bestDir);
        switch (bestDir) {
            case UP -> entity.setPos(entity.getX(), target.getY(), entity.getZ());
            case DOWN -> entity.setPos(entity.getX(),
                    target.getY() + 1 - entity.getBbHeight(), entity.getZ());
            default -> entity.setPos(target.getX() + 0.5, entity.getY(), target.getZ() + 0.5);
        }
        return true;
    }

    public static <T extends Entity> List<T> getEntitiesInRange(Level level, BlockPos pos,
                                                                double radius, Class<T> type) {
        if (level == null || pos == null || type == null) return Collections.emptyList();
        double r = Math.abs(radius);

        AABB box = new AABB(
                pos.getX() - r, pos.getY() - r, pos.getZ() - r,
                pos.getX() + r + 1, pos.getY() + r + 1, pos.getZ() + r + 1);
        return level.getEntitiesOfClass(type, box);
    }

    public static <T extends Entity> List<T> getEntitiesInRange(Level level, BlockPos pos,
                                                                double radius, Class<T> type,
                                                                Predicate<T> filter) {
        if (level == null || pos == null || type == null || filter == null) {
            return Collections.emptyList();
        }
        double r = Math.abs(radius);

        AABB box = new AABB(
                pos.getX() - r, pos.getY() - r, pos.getZ() - r,
                pos.getX() + r + 1, pos.getY() + r + 1, pos.getZ() + r + 1);
        return level.getEntitiesOfClass(type, box, filter);
    }

    public static void resetFloatCounter(ServerPlayer player) {
        if (player == null || player.connection == null) return;
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
    public static <T extends Entity> T getNearestEntity(Level level, BlockPos pos,
                                                        double radius, Class<T> type) {
        List<T> entities = getEntitiesInRange(level, pos, radius, type);
        if (entities.isEmpty()) return null;

        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        T best = null;
        double bestDist = Double.MAX_VALUE;
        for (T e : entities) {
            if (e == null) continue;
            double d = e.distanceToSqr(cx, cy, cz);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    @Nullable
    public static LivingEntity getNearestLivingEntity(Level level, BlockPos pos, double radius,
                                                      @Nullable Entity exclude) {
        List<LivingEntity> entities = getEntitiesInRange(level, pos, radius, LivingEntity.class);
        if (entities.isEmpty()) return null;

        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : entities) {
            if (e == null || e == exclude || !e.isAlive()) continue;
            double d = e.distanceToSqr(cx, cy, cz);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
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
        if (entity == null || pos == null) return;
        Vec3 entityPos = entity.position();
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3 direction = entityPos.subtract(center).normalize();
        entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(force)));
        entity.hurtMarked = true;
    }

    public static void pullToward(Entity entity, BlockPos pos, double force) {
        if (entity == null || pos == null) return;
        Vec3 entityPos = entity.position();
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3 direction = center.subtract(entityPos).normalize();
        entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(force)));
        entity.hurtMarked = true;
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
        if (entity == null || pos == null) return false;
        Vec3 eyePos = entity.getEyePosition();
        Vec3 targetPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return entity.level().clip(new ClipContext(
                eyePos, targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        )).getType() == HitResult.Type.MISS;
    }

    public static void setTarget(Entity attacker, @Nullable LivingEntity target) {
        if (attacker instanceof Mob mob) {
            mob.setTarget(target);
        }
    }

    public static boolean canEntityBeSeen(Entity entity, double x, double y, double z) {
        if (entity == null) return false;
        Level level = entity.level();
        Vec3 entityPos = entity.getEyePosition();
        Vec3 targetPos = new Vec3(x, y, z);
        return level.clip(new ClipContext(
                entityPos, targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        )).getType() == HitResult.Type.MISS;
    }

    public static boolean hasGoggles(Entity e) {
        if (!(e instanceof Player viewer)) return false;

        if (viewer.getMainHandItem().getItem() instanceof IGoggles
                && showPopups(viewer.getMainHandItem(), viewer)) {
            return true;
        }
        if (viewer.getOffhandItem().getItem() instanceof IGoggles
                && showPopups(viewer.getOffhandItem(), viewer)) {
            return true;
        }
        for (ItemStack armor : viewer.getArmorSlots()) {
            if (armor != null && !armor.isEmpty()
                    && armor.getItem() instanceof IGoggles
                    && showPopups(armor, viewer)) {
                return true;
            }
        }
        List<ItemStack> curios = CuriosCompat.findCuriosItems(viewer,
                s -> s != null && !s.isEmpty() && s.getItem() instanceof IGoggles);
        if (curios != null) {
            for (ItemStack stack : curios) {
                if (stack != null && showPopups(stack, viewer)) return true;
            }
        }
        return false;
    }

    private static boolean showPopups(ItemStack stack, Player player) {
        if (stack == null) return false;
        if (stack.getItem() instanceof IGoggles goggles) {
            try {
                return goggles.showIngamePopups(stack, player);
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        return false;
    }

    public static void makeChampion(Monster entity, boolean persist) {
        if (entity == null) return;
        if (ChampionModifier.getChampionId(entity) != -1) return;

        int type = entity.level().random.nextInt(ChampionModifier.MODS.length);
        if (entity instanceof Creeper) type = 0;
        ChampionModifier.setChampionId(entity, type);

        if (!(entity instanceof EntityArcaneresearchBoss)) {
            AttributeInstance health = entity.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.removeModifier(CHAMPION_HEALTH_ID);
                health.addTransientModifier(new AttributeModifier(CHAMPION_HEALTH_ID,
                        "Champion health buff", 100.0, AttributeModifier.Operation.ADDITION));
            }
            AttributeInstance damage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.removeModifier(CHAMPION_DAMAGE_ID);
                damage.addTransientModifier(new AttributeModifier(CHAMPION_DAMAGE_ID,
                        "Champion damage buff", 2.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
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

        if (persist) entity.setPersistenceRequired();

        switch (type) {
            case 0 -> {
                AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) {
                    speed.removeModifier(BOLDBUFF_ID);
                    speed.addTransientModifier(new AttributeModifier(BOLDBUFF_ID,
                            "Bold speed boost", 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
                }
            }
            case 3 -> {
                AttributeInstance damage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
                if (damage != null) {
                    damage.removeModifier(MIGHTYBUFF_ID);
                    damage.addTransientModifier(new AttributeModifier(MIGHTYBUFF_ID,
                            "Mighty damage boost", 2.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
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

        if (target instanceof TamableAnimal tamable
                && tamable.isTame() && tamable.getOwner() == source) {
            return true;
        }
        if (source instanceof TamableAnimal tamable
                && tamable.isTame() && tamable.getOwner() == target) {
            return true;
        }
        if (source instanceof TamableAnimal st && target instanceof TamableAnimal tt
                && st.isTame() && tt.isTame() && st.getOwner() == tt.getOwner()) {
            return true;
        }
        return false;
    }
}