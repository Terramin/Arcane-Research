package arcaneresearch.proxies;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

// NEOFORGE_PORT: mirrors Forge 1.20.1 proxies/IProxy.java 1:1.
// REQUIRED: net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
//   -> net.neoforged.fml.event.lifecycle.InterModProcessEvent
//   Identifier -> Identifier in 26.x (pending; kept as-is in stub, see TODO_State.md).
public interface IProxy {

    void preInit();

    void init();

    void postInit();

    Level getClientLevel();

    boolean getSingleplayer();

    Level getLevel(ResourceKey<Level> dimension);

    boolean isShiftKeyDown();

    void checkInterModComs(net.neoforged.fml.event.lifecycle.InterModProcessEvent event);

    // Server default keeps the plain data model; ClientProxy returns the renderable part models.
    default arcaneresearch.api.golems.parts.PartModel createGolemPartModel(String name,
            net.minecraft.resources.Identifier objModel, net.minecraft.resources.Identifier objTexture,
            arcaneresearch.api.golems.parts.PartModel.EnumAttachPoint attachPoint) {
        return new arcaneresearch.api.golems.parts.PartModel(objModel, objTexture, attachPoint);
    }
}
