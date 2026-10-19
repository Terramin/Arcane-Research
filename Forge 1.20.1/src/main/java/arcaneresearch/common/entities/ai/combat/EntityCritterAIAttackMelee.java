package arcaneresearch.common.entities.ai.combat;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
public class EntityCritterAIAttackMelee extends MeleeAttackGoal {
    public EntityCritterAIAttackMelee(PathfinderMob creature, double speed, boolean useLongMemory) {
        super(creature, speed, useLongMemory);
    }
}
