package thaumictinker.kami.trinkets;

import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

 


 
public class WearerAvoidGoal extends AvoidEntityGoal<Player> {

    public WearerAvoidGoal(Creeper creeper) {
        super(creeper, Player.class,
                wearer -> wearer instanceof Player player && CatAmuletItem.isWearing(player),
                6.0F, 1.0D, 1.2D,
                entity -> !entity.isSpectator() && !(entity instanceof Player player && player.isCreative()));
    }
}
