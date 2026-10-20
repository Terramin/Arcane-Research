package arcaneresearch.common.lib.capabilities;

import net.neoforged.bus.api.IEventBus;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT: mirrors Forge 1.20.1 common/lib/capabilities/CapabilityHandler.java.
// Forge registered IPlayerKnowledge/IPlayerWarp in RegisterCapabilitiesEvent (MOD bus) + log line.
// NeoForge has NO equivalent event: AttachmentTypes self-register via DeferredRegister
// (see ArcaneresearchCapabilities.register), interfaces need no registration.
// File kept 1:1 for future diff; log preserved in register() for parity.
public class CapabilityHandler {

    public static void register(IEventBus modEventBus) {
        // No-op by design (see class javadoc). Kept so Arcaneresearch main wiring has a 1:1 slot.
        Arcaneresearch.LOGGER.info("Arcaneresearch capabilities registered");
    }
}
