package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

public class ChampionModTainted implements IChampionModifierEffect {

    public static void resetAI(Mob critter) {
        if (critter instanceof net.minecraft.world.entity.monster.Monster) {
            ensureAttackDamage(critter);
            return;
        }
        if (critter.getPersistentData().getBooleanOr("tc_tainted_ai", false)) {
            ensureAttackDamage(critter);
            return;
        }
        try {
            critter.goalSelector.removeAllGoals(goal -> true);
            critter.targetSelector.removeAllGoals(goal -> true);
            critter.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(critter));
            if (critter instanceof net.minecraft.world.entity.PathfinderMob pathfinder) {
                critter.goalSelector.addGoal(2, new arcaneresearch.common.entities.ai.combat.EntityCritterAIAttackMelee(pathfinder, 1.2, false));
                critter.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal(pathfinder, 1.0));
                critter.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(pathfinder, 1.0));
                
                
                critter.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(pathfinder,
                        net.minecraft.world.entity.monster.zombie.ZombifiedPiglin.class));
                critter.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(
                        pathfinder, net.minecraft.world.entity.player.Player.class, true));
            }
            critter.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(critter,
                    net.minecraft.world.entity.player.Player.class, 8.0f));
            critter.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(critter));
            if (critter instanceof net.minecraft.world.entity.PathfinderMob pathfinder2) {
                critter.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal(
                        pathfinder2, 1.0, false, 4, () -> false));
            }
            critter.getPersistentData().putBoolean("tc_tainted_ai", true);
        } catch (Exception ignored) {
        }
        ensureAttackDamage(critter);
    }

    private static void ensureAttackDamage(Mob mob) {

        net.minecraft.world.entity.ai.attributes.AttributeInstance attackDamage =
                mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }
        attackDamage.setBaseValue(Math.max(2.0f, (mob.getBbHeight() + mob.getBbWidth()) * 2.0f));
    }

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        if (champion instanceof Mob mob && !champion.level().isClientSide()) {
            resetAI(mob);
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity champion) {

        float w = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float d = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float h = champion.level().getRandom().nextFloat() * champion.getBbHeight();
        FXDispatcher.INSTANCE.drawGenericParticles(champion.getBoundingBox().minX + w,
                champion.getBoundingBox().minY + h, champion.getBoundingBox().minZ + d,
                0.0, -0.01, 0.0, 0.1f + champion.level().getRandom().nextFloat() * 0.2f, 0.0f,
                0.1f + champion.level().getRandom().nextFloat() * 0.1f,
                0.25f, false, 1, 5, 1, 6 + champion.level().getRandom().nextInt(6), 0,
                2.0f + champion.level().getRandom().nextFloat(), 0.5f, 1);
    }

    @Override
    public void preRender(LivingEntity champion) {

    }
}