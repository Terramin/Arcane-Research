package arcaneresearch.common.entities.ai.pech;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.common.entities.monster.EntityPech;

public class AINearestAttackableTargetPech extends NearestAttackableTargetGoal<Player> {
    public AINearestAttackableTargetPech(Mob pech, boolean checkSight) {
        super(pech, Player.class, 10, checkSight, false,
                (target, level) -> pech instanceof EntityPech p && p.getAnger() > 0);
    }
}
