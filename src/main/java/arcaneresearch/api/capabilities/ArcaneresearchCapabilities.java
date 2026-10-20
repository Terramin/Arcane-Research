package arcaneresearch.api.capabilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
public class ArcaneresearchCapabilities {
    public static final Capability<IPlayerKnowledge> KNOWLEDGE = CapabilityManager.get(new CapabilityToken<>() {});
    @Nullable
    public static IPlayerKnowledge getKnowledge(@Nonnull Player player) {
        return player.getCapability(KNOWLEDGE).orElse(null);
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
    public static final Capability<IPlayerWarp> WARP = CapabilityManager.get(new CapabilityToken<>() {});
    @Nullable
    public static IPlayerWarp getWarp(@Nonnull Player player) {
        return player.getCapability(WARP).orElse(null);
    }
}
