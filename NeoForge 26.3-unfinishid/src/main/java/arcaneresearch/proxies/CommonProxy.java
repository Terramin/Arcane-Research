package arcaneresearch.proxies;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;

// NEOFORGE_PORT (foundation stub):
// Mirrors Forge CommonProxy structure; full logic (parseCountedStack, championModWhitelist,
// BiomeHandler/Config*/InternalMethodHandler wiring) ported 1:1 in the proxies phase.
// Forge ServerLifecycleHooks -> NeoForge net.neoforged.neoforge.server.ServerLifecycleHooks (TODO).
public class CommonProxy implements IProxy {

    // NEOFORGE_PORT: same key type as Forge (ResourceLocation -> Identifier).
    public static final java.util.Map<net.minecraft.resources.Identifier, Integer> championModWhitelist = new java.util.HashMap<>();

    @Override
    public void preInit() {
        // TODO_PORT: Forge CommonProxy.preInit body.
    }

    @Override
    public void init() {
        // TODO_PORT: Forge CommonProxy.init body.
    }

    @Override
    public void postInit() {
        // TODO_PORT: Forge CommonProxy.postInit body.
    }

    @Override
    public Level getClientLevel() {
        return null;
    }

    @Override
    public boolean getSingleplayer() {
        return false;
    }

    @Override
    public Level getLevel(ResourceKey<Level> dimension) {
        return null;
    }

    @Override
    public boolean isShiftKeyDown() {
        return false;
    }

    @Override
    public void checkInterModComs(InterModProcessEvent event) {
        // TODO_PORT: Forge Proxies.get().checkInterModComs(event) body.
    }
}
