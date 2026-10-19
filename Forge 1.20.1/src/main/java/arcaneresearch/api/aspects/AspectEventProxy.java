package arcaneresearch.api.aspects;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchApi;
public class AspectEventProxy {
    public void registerObjectTag(ItemStack item, AspectList aspects) {
        if (aspects == null) aspects = new AspectList();
        ArcaneresearchApi.registerObjectTag(item, aspects);
    }
    public void registerObjectTag(String tagName, AspectList aspects) {
        if (aspects == null) aspects = new AspectList();
        ArcaneresearchApi.registerObjectTag(tagName, aspects);
    }
    public void registerComplexObjectTag(ItemStack item, AspectList aspects) {
        if (aspects == null) aspects = new AspectList();
        ArcaneresearchApi.registerComplexObjectTag(item, aspects);
    }
    public void registerComplexObjectTag(String tagName, AspectList aspects) {
        if (aspects == null) aspects = new AspectList();
        ArcaneresearchApi.registerComplexObjectTag(tagName, aspects);
    }
}
