package arcaneresearch.proxies;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;





public interface IProxy {

    void preInit();

    void init();

    void postInit();

    Level getClientLevel();

    boolean getSingleplayer();

    Level getLevel(ResourceKey<Level> dimension);

    boolean isShiftKeyDown();

    void checkInterModComs(net.neoforged.fml.event.lifecycle.InterModProcessEvent event);

    
    default arcaneresearch.api.golems.parts.PartModel createGolemPartModel(String name,
            net.minecraft.resources.Identifier objModel, net.minecraft.resources.Identifier objTexture,
            arcaneresearch.api.golems.parts.PartModel.EnumAttachPoint attachPoint) {
        return new arcaneresearch.api.golems.parts.PartModel(objModel, objTexture, attachPoint);
    }
}
