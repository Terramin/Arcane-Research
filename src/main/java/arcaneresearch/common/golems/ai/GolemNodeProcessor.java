package arcaneresearch.common.golems.ai;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
public class GolemNodeProcessor extends WalkNodeEvaluator {
    public GolemNodeProcessor() {
        super();
        setCanPassDoors(true);
        setCanOpenDoors(false);
    }
}
