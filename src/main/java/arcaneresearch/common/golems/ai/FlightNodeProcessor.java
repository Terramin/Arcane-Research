package arcaneresearch.common.golems.ai;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
public class FlightNodeProcessor extends FlyNodeEvaluator {
    public FlightNodeProcessor() {
        super();
        setCanPassDoors(true);
    }
}
