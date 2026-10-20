package arcaneresearch.proxies;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
public interface IProxy {
    void preInit();
    void init();
    void postInit();
    Level getClientLevel();
    boolean getSingleplayer();
    Level getLevel(ResourceKey<Level> dimension);
    boolean isShiftKeyDown();
    void checkInterModComs(InterModProcessEvent event);
    default arcaneresearch.api.golems.parts.PartModel createGolemPartModel(String kind,
                                                                       net.minecraft.resources.ResourceLocation objModel,
                                                                       net.minecraft.resources.ResourceLocation objTexture,
                                                                       arcaneresearch.api.golems.parts.PartModel.EnumAttachPoint attachPoint) {
        return new arcaneresearch.api.golems.parts.PartModel(objModel, objTexture, attachPoint);
    }
}
