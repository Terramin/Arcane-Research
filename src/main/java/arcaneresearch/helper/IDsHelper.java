package arcaneresearch.helper;

import net.minecraft.resources.Identifier;

 

 
public final class IDsHelper {

    private IDsHelper() {
    }

      
    public static Identifier mod(String path) {
        return Identifier.fromNamespaceAndPath(arcaneresearch.Arcaneresearch.MODID, path);
    }

      
    public static Identifier of(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

      
    public static Identifier parse(String id) {
        return Identifier.parse(id);
    }

      
    public static Identifier vanilla(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }
}
