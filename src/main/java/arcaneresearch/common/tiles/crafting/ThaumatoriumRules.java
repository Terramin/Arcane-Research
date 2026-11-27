package arcaneresearch.common.tiles.crafting;

/**
 * Pure rules for the alchemy furnace. No Minecraft types are referenced, so the
 * timing, capacity and transport decisions can be unit-tested directly.
 * {@link TileThaumatorium} delegates these calculations here.
 */
public final class ThaumatoriumRules {

    /** Suction strength advertised to neighbouring essentia transports. */
    public static final int SUCTION = 128;
    /** Ticks between heat and upgrade checks. */
    public static final int REFRESH_INTERVAL = 40;
    /** Ticks between crafting attempts. */
    public static final int CRAFT_INTERVAL = 5;
    /** Ticks of vent particles after a client-side trigger. */
    public static final int VENT_DURATION = 7;
    /** Recipe slots of a furnace without brain boxes. */
    public static final int BASE_RECIPE_SLOTS = 1;
    /** Additional recipe slots granted by one brain box. */
    public static final int SLOTS_PER_BRAIN_BOX = 2;

    private ThaumatoriumRules() {
    }

    /** @return whether heat and upgrades should be refreshed on this counter value */
    public static boolean isRefreshTick(int counter) {
        return counter % REFRESH_INTERVAL == 0;
    }

    /** @return whether a crafting attempt should be made on this counter value */
    public static boolean isCraftTick(int counter) {
        return counter % CRAFT_INTERVAL == 0;
    }

    /** @return number of recipe slots available for the given number of brain boxes */
    public static int recipeCapacity(int brainBoxes) {
        int boxes = Math.max(brainBoxes, 0);
        if (boxes > (Integer.MAX_VALUE - BASE_RECIPE_SLOTS) / SLOTS_PER_BRAIN_BOX) {
            return Integer.MAX_VALUE;
        }
        return BASE_RECIPE_SLOTS + boxes * SLOTS_PER_BRAIN_BOX;
    }

    /**
     * Transport-side pull check used while filling the furnace.
     *
     * @param y layer being scanned (0 = furnace, 1 = top block)
     * @param towardFacing whether the neighbour is on the furnace's front side
     * @param down whether the neighbour is below
     * @param up whether the neighbour is above
     * @return true when this neighbour may supply essentia
     */
    public static boolean canPullFrom(int y, boolean towardFacing, boolean down, boolean up) {
        return !towardFacing && !down && !(y == 0 && up);
    }

    /** @return whether inventory contents must be dropped when the block changes */
    public static boolean shouldDropContents(boolean moving, boolean sameBlock) {
        return !moving && !sameBlock;
    }

    /**
     * The brass item itself is only dropped for a real break. Converting the partner
     * block to brass is not a break and must not produce a second ingot.
     */
    public static boolean shouldDropBlockItem(boolean brassConversion) {
        return !brassConversion;
    }

    /** @return whether an index addresses an existing recipe slot */
    public static boolean isRecipeSlotValid(int slot, int recipeCount) {
        return slot >= 0 && slot < recipeCount;
    }

    /** @return whether another unit of essentia can still be accepted for a recipe */
    public static boolean canAcceptEssence(int stored, int required) {
        return stored < required;
    }

    /** Independent random offset for vent particles, matching the six-roll original. */
    public static float ventSpeed(float randomValue) {
        if (!Float.isFinite(randomValue)) {
            return 0.0f;
        }
        return 0.1f - randomValue * 0.2f;
    }

    /** @return a legal recipe slot count even for a corrupted negative tag value */
    public static int normalizeMaxRecipes(int raw) {
        return Math.max(raw, BASE_RECIPE_SLOTS);
    }

    /** @return a non-null owner name, safe to write into an NBT string tag */
    public static String normalizeOwner(String owner) {
        return owner == null ? "" : owner;
    }

    /** @return how many tail entries must be removed to fit the capacity */
    public static int trimCount(int size, int capacity) {
        return Math.max(0, size - Math.max(capacity, 0));
    }

    /** @return whether another recipe fits into the buffer */
    public static boolean canAddRecipe(int size, int capacity) {
        return size >= 0 && size < Math.max(capacity, 0);
    }

    /** Amount a neighbouring tube may pull, zero when the transport is closed or empty. */
    public static int takeEssentia(int stored, int amount, boolean canOutput) {
        return canOutput && amount > 0 && stored >= amount ? amount : 0;
    }

    /** Amount accepted from a neighbouring tube, zero when input is closed. */
    public static int acceptEssentia(int amount, boolean canInput) {
        return canInput && amount > 0 ? amount : 0;
    }

    /** @return essentia still required for a recipe aspect, never negative */
    public static int remainingEssentia(int required, int stored) {
        return Math.max(0, required - stored);
    }

    /** @return whether one aspect requirement is fully met */
    public static boolean isSatisfied(int required, int stored) {
        return remainingEssentia(required, stored) == 0;
    }

    /** @return whether the furnace must search for a new recipe for its catalyst */
    public static boolean shouldReselectRecipe(int slot, int size,
                                               boolean recipeNull, boolean catalystMatches) {
        return !isRecipeSlotValid(slot, size) || recipeNull || !catalystMatches;
    }

    /**
     * Slot ranges tried by {@code quickMoveStack}, in order. Each pair is [from, to).
     * Slot 0 is the catalyst; 1..27 the main inventory; 28..36 the hotbar.
     */
    public static int[][] moveAttempts(int slot, int total) {
        if (slot == 0) {
            return new int[][]{{1, total}};
        }
        if (slot < 28) {
            return new int[][]{{0, 1}, {28, total}};
        }
        return new int[][]{{0, 1}, {1, 28}, {1, total}};
    }

    /** Fingerprint of a recipe catalogue, used to skip rebuilding the bridge. */
    public static int recipeFingerprint(java.util.Collection<Integer> keyHashes) {
        int fingerprint = keyHashes.size() + 1;
        for (int hash : keyHashes) {
            fingerprint = 31 * fingerprint + hash;
        }
        return fingerprint;
    }
}
