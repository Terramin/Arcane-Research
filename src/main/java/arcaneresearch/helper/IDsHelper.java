package arcaneresearch.helper;
import net.minecraft.resources.ResourceLocation;
public final class IDsHelper {
    private IDsHelper() {
    }
    public static ResourceLocation mod(String path) {
        return new ResourceLocation(arcaneresearch.Arcaneresearch.MODID, path);
    }
    public static ResourceLocation of(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
    public static ResourceLocation parse(String id) {
        return new ResourceLocation(id);
    }
    public static ResourceLocation vanilla(String path) {
        return new ResourceLocation("minecraft", path);
    }
}
