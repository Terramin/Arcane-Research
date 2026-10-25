package arcaneresearch.api.aspects;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
public class AspectSourceHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AspectSourceHelper.class);
    private static Method drainEssentiaMethod;
    private static Method findEssentiaMethod;
    public static boolean drainEssentia(BlockEntity blockEntity, Aspect aspect, Direction direction, int range) {
        try {
            if (drainEssentiaMethod == null) {
                Class<?> handlerClass = Class.forName("arcaneresearch.common.lib.events.EssentiaHandler");
                drainEssentiaMethod = handlerClass.getMethod("drainEssentia",
                    BlockEntity.class, Aspect.class, Direction.class, int.class);
            }
            return (Boolean) drainEssentiaMethod.invoke(null, blockEntity, aspect, direction, range);
        } catch (Exception ex) {
            LOGGER.warn("[Arcaneresearch API] Could not invoke arcaneresearch.common.lib.events.EssentiaHandler method drainEssentia");
        }
        return false;
    }
    public static boolean findEssentia(BlockEntity blockEntity, Aspect aspect, Direction direction, int range) {
        try {
            if (findEssentiaMethod == null) {
                Class<?> handlerClass = Class.forName("arcaneresearch.common.lib.events.EssentiaHandler");
                findEssentiaMethod = handlerClass.getMethod("findEssentia",
                    BlockEntity.class, Aspect.class, Direction.class, int.class);
            }
            return (Boolean) findEssentiaMethod.invoke(null, blockEntity, aspect, direction, range);
        } catch (Exception ex) {
            LOGGER.warn("[Arcaneresearch API] Could not invoke arcaneresearch.common.lib.events.EssentiaHandler method findEssentia");
        }
        return false;
    }
}
