package arcaneresearch.common.golems.parts;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.GolemArm;
import arcaneresearch.common.entities.projectile.EntityGolemDart;
import arcaneresearch.common.golems.ai.AIArrowAttack;

public class GolemArmDart implements GolemArm.IArmFunction {

    @Override
    public void onMeleeAttack(IGolemAPI golem, Entity target) {
    }

    @Override
    public void onRangedAttack(IGolemAPI golem, LivingEntity target, float range) {
        
        EntityGolemDart dart = new EntityGolemDart(golem.getGolemWorld(), golem.getGolemEntity());
        float dmg = (float) golem.getGolemEntity().getAttributeValue(Attributes.ATTACK_DAMAGE) / 3.0F;
        dart.setBaseDamage(dmg + range + golem.getGolemWorld().getRandom().nextGaussian() * 0.25D);
        double d0 = target.getX() - golem.getGolemEntity().getX();
        double d1 = target.getBoundingBox().minY + target.getEyeHeight() + range * range - dart.getY();
        double d2 = target.getZ() - golem.getGolemEntity().getZ();
        dart.shoot(d0, d1, d2, 1.6F, 3.0F);
        golem.getGolemWorld().addFreshEntity(dart);
        golem.getGolemEntity().playSound(SoundEvents.ARROW_SHOOT, 1.0F,
                1.0F / (golem.getGolemWorld().getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    public Goal getRangedAttackAI(RangedAttackMob mob) {
        return new AIArrowAttack(mob, 1.0D, 20, 25, 16.0F);
    }

    @Override
    public void onUpdateTick(IGolemAPI golem) {
    }
}
