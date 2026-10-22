package thaumictinker;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 




 
public final class TtCore {
    public static final String MODID = "thaumictinker";
    public static final Logger LOGGER = LoggerFactory.getLogger(TtCore.MODID);
    private TtCore() {
    }
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
