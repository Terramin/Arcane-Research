package arcaneresearch.api.capabilities;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// NEOFORGE_PORT:
// Forge 1.20.1 used Capability<IPlayerKnowledge> / Capability<IPlayerWarp>
//   via CapabilityManager.get(new CapabilityToken<>() {}) + AttachCapabilitiesEvent + LazyOptional.
// NeoForge 26.3 uses AttachmentType holders owned by the common implementation:
//   arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities (single source of truth;
//   Forge had DUPLICATE Capability objects in api/ and common/ — NeoForge port unifies them here).
// This api façade keeps the historical helper names/signatures (knowsResearch etc.) so api
// consumers (ScanningManager, InfusionRecipe, future addons) compile unchanged; only the
// two getters delegate to the common holders. Behavior intentionally preserved.
public class ArcaneresearchCapabilities {

    @Nullable
    public static IPlayerKnowledge getKnowledge(@Nonnull Player player) {
        // NEOFORGE_PORT: Forge player.getCapability(KNOWLEDGE).orElse(null)
        //   -> NeoForge attachment (auto-creates default, like AttachCapabilitiesEvent did).
        if (player == null) return null;
        return arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities.getKnowledge(player);
    }

    public static boolean knowsResearch(@Nonnull Player player, @Nonnull String... research) {
        IPlayerKnowledge knowledge = getKnowledge(player);
        if (knowledge == null) return false;

        for (String r : research) {
            if (r.contains("&&")) {
                String[] rr = r.split("&&");
                if (!knowsResearch(player, rr)) return false;
            } else if (r.contains("||")) {
                String[] rr = r.split("\\|\\|");
                boolean anyTrue = false;
                for (String str : rr) {
                    if (knowsResearch(player, str)) {
                        anyTrue = true;
                        break;
                    }
                }
                if (!anyTrue) return false;
            } else {
                if (!knowledge.isResearchKnown(r)) return false;
            }
        }
        return true;
    }

    public static boolean knowsResearchStrict(@Nonnull Player player, @Nonnull String... research) {
        IPlayerKnowledge knowledge = getKnowledge(player);
        if (knowledge == null) return false;

        for (String r : research) {
            if (r.contains("&&")) {
                String[] rr = r.split("&&");
                if (!knowsResearchStrict(player, rr)) return false;
            } else if (r.contains("||")) {
                String[] rr = r.split("\\|\\|");
                boolean anyTrue = false;
                for (String str : rr) {
                    if (knowsResearchStrict(player, str)) {
                        anyTrue = true;
                        break;
                    }
                }
                if (!anyTrue) return false;
            } else if (r.contains("@")) {
                if (!knowledge.isResearchKnown(r)) return false;
            } else {
                if (!knowledge.isResearchComplete(r)) return false;
            }
        }
        return true;
    }

    public static boolean isResearchKnown(@Nonnull Player player, @Nonnull String researchKey) {
        IPlayerKnowledge knowledge = getKnowledge(player);
        return knowledge != null && knowledge.isResearchKnown(researchKey);
    }

    @Nullable
    public static IPlayerWarp getWarp(@Nonnull Player player) {
        // NEOFORGE_PORT: same delegation as getKnowledge (see above).
        if (player == null) return null;
        return arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities.getWarp(player);
    }
}
