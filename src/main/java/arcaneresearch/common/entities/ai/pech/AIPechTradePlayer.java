package arcaneresearch.common.entities.ai.pech;

import net.minecraft.world.entity.ai.goal.Goal;
import arcaneresearch.common.entities.monster.EntityPech;

import java.util.EnumSet;

public class AIPechTradePlayer extends Goal {

    private final EntityPech pech;

    public AIPechTradePlayer(EntityPech pech) {
        this.pech = pech;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return pech.isAlive() && !pech.isInWater() && pech.isTamed()
                && pech.onGround() && pech.trading;
    }

    @Override
    public void start() {
        pech.getNavigation().stop();
    }

    @Override
    public void stop() {
        pech.trading = false;
    }
}
