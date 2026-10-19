package arcaneresearch.common.golems.ai;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
public class AINearestValidTarget<T extends LivingEntity> extends TargetGoal {
    protected final Class<T> targetClass;
    @Nullable
    protected final Predicate<LivingEntity> extraSelector;
    private final int targetChance;
    @Nullable
    protected T targetEntity;
    protected TargetingConditions targetingConditions;
    private long lastCanUseCheck;
    private int unseenTicks;
    public AINearestValidTarget(Mob mob, Class<T> targetClass, boolean checkSight) {
        this(mob, targetClass, 10, checkSight, false, null);
    }
    public AINearestValidTarget(Mob mob, Class<T> targetClass, boolean checkSight, boolean nearbyOnly) {
        this(mob, targetClass, 10, checkSight, nearbyOnly, null);
    }
    public AINearestValidTarget(Mob mob, Class<T> targetClass, int targetChance,
                                boolean checkSight, boolean nearbyOnly,
                                @Nullable Predicate<LivingEntity> extraSelector) {
        super(mob, checkSight, nearbyOnly);
        this.targetClass = targetClass;
        this.targetChance = targetChance;
        this.extraSelector = extraSelector;
        setFlags(EnumSet.of(Flag.TARGET));
        targetingConditions = TargetingConditions.forCombat()
                .range(getFollowDistance())
                .selector(combinedSelector());
    }
    public static boolean friendlyTargetingValid(Mob attacker, LivingEntity target) {
        Team team = attacker.getTeam();
        Team targetTeam = target.getTeam();
        boolean friendly = attacker instanceof ITargets targets && targets.getTargetFriendly();
        if (team != null && targetTeam == team && !friendly) {
            return false;
        }
        if (team != null && targetTeam != team && friendly) {
            return false;
        }
        return true;
    }
    public static boolean isGoodTarget(Mob attacker, LivingEntity target, boolean excludeCreative, boolean checkSight) {
        if (target == null || target == attacker || !target.isAlive()) {
            return false;
        }
        if (!attacker.canAttack(target)) {
            return false;
        }
        if (!friendlyTargetingValid(attacker, target)) {
            return false;
        }
        boolean friendly = attacker instanceof ITargets targets && targets.getTargetFriendly();
        if (attacker instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            if (target instanceof OwnableEntity targetOwned
                    && ownable.getOwnerUUID().equals(targetOwned.getOwnerUUID()) && !friendly) {
                return false;
            }
            if (!(target instanceof OwnableEntity) && !(target instanceof Player) && friendly) {
                return false;
            }
            if (target instanceof OwnableEntity targetOwned
                    && !ownable.getOwnerUUID().equals(targetOwned.getOwnerUUID()) && friendly) {
                return false;
            }
            if (target instanceof Player playerTarget
                    && ownable.getOwnerUUID().equals(playerTarget.getUUID()) && !friendly) {
                return false;
            }
        } else if (target instanceof Player playerTarget && !excludeCreative
                && playerTarget.getAbilities().invulnerable && !friendly) {
            return false;
        }
        if (target instanceof ServerPlayer serverPlayer && serverPlayer.isSpectator()) {
            return false;
        }
        return !checkSight || attacker.getSensing().hasLineOfSight(target);
    }
    protected Predicate<LivingEntity> combinedSelector() {
        return entity -> {
            if (extraSelector != null && !extraSelector.test(entity)) {
                return false;
            }
            if (entity instanceof Player player) {
                double range = getFollowDistance();
                if (player.isCrouching()) {
                    range *= 0.800000011920929;
                }
                if (player.isInvisible()) {
                    float visibility = player.getArmorCoverPercentage();
                    if (visibility < 0.1f) {
                        visibility = 0.1f;
                    }
                    range *= 0.7f * visibility;
                }
                if (entity.distanceTo(mob) > range) {
                    return false;
                }
            }
            return isSuitableTarget(entity, false);
        };
    }
    @Override
    public boolean canUse() {
        if (targetChance > 0 && mob.getRandom().nextInt(targetChance) != 0) {
            return false;
        }
        targetingConditions = TargetingConditions.forCombat()
                .range(getFollowDistance())
                .selector(combinedSelector());
        List<T> list = mob.level().getNearbyEntities(targetClass, targetingConditions, mob,
                mob.getBoundingBox().inflate(getFollowDistance(), 4.0, getFollowDistance()));
        if (list.isEmpty()) {
            return false;
        }
        list.sort((a, b) -> Double.compare(mob.distanceToSqr(a), mob.distanceToSqr(b)));
        targetEntity = list.get(0);
        lastCanUseCheck = mob.level().getGameTime();
        return true;
    }
    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!friendlyTargetingValid(mob, target)) {
            return false;
        }
        double range = getFollowDistance();
        if (mob.distanceToSqr(target) > range * range) {
            return false;
        }
        if (mustSee) {
            if (mob.getSensing().hasLineOfSight(target)) {
                unseenTicks = 0;
            } else if (++unseenTicks > 60) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void start() {
        mob.setTarget(targetEntity);
        unseenTicks = 0;
        super.start();
    }
    protected boolean isSuitableTarget(LivingEntity target, boolean checkSightOnly) {
        if (!isGoodTarget(mob, target, checkSightOnly, mustSee)) {
            return false;
        }
        return !mob.hasRestriction() || mob.isWithinRestriction(target.blockPosition());
    }
}
