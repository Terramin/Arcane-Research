package arcaneresearch.common.golems.ai;

/** Pure target-selection rules extracted from {@link AINearestValidTarget}. */
public final class TargetingRules {

    /** Facts about a candidate extracted from Minecraft objects by the AI goal. */
    public record Facts(boolean alive, boolean self, boolean canAttack,
                        boolean hasTeam, boolean sameTeam, boolean friendly,
                        boolean hasOwner, boolean targetOwned, boolean sameOwner,
                        boolean targetIsPlayer, boolean targetIsOwner,
                        boolean invulnerablePlayer, boolean excludeCreative,
                        boolean spectator, boolean lineOfSight, boolean checkSight) {
    }

    private TargetingRules() {
    }

    public static boolean friendlyTargetingValid(boolean hasTeam, boolean sameTeam, boolean friendly) {
        if (hasTeam && sameTeam && !friendly) {
            return false;
        }
        return !hasTeam || sameTeam || !friendly;
    }

    public static boolean goodTarget(Facts f) {
        if (!f.alive() || f.self() || !f.canAttack()) {
            return false;
        }
        if (!friendlyTargetingValid(f.hasTeam(), f.sameTeam(), f.friendly())) {
            return false;
        }
        if (f.hasOwner()) {
            if (f.targetOwned() && f.sameOwner() && !f.friendly()) {
                return false;
            }
            if (!f.targetOwned() && !f.targetIsPlayer() && f.friendly()) {
                return false;
            }
            if (f.targetOwned() && !f.sameOwner() && f.friendly()) {
                return false;
            }
            return !(f.targetIsPlayer() && f.targetIsOwner() && !f.friendly());
        }
        if (f.targetIsPlayer() && !f.excludeCreative() && f.invulnerablePlayer() && !f.friendly()) {
            return false;
        }
        if (f.spectator()) {
            return false;
        }
        return !f.checkSight() || f.lineOfSight();
    }
}
