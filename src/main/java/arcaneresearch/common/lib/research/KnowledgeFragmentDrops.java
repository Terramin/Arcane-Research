package arcaneresearch.common.lib.research;

/**
 * Pure rules for the Thaumcraft 4.2.3.5 knowledge-fragment spawn tables.
 * Kept free of Minecraft objects so the thresholds can be unit tested.
 */
public final class KnowledgeFragmentDrops {

    /** EntityCultist.dropFewItems: one fragment when r == 0 of 10. */
    public static final float CULTIST_CHANCE = 0.10F;

    /** ComponentWizardTower / strongholdLibrary chest weight: 20. */
    public static final float STRONGHOLD_LIBRARY_CHANCE = 0.20F;

    /** Generic vanilla chests: uncommonLoot weight 4. */
    public static final float OTHER_CHEST_CHANCE = 0.04F;

    public static final int LIBRARY_MIN = 3;
    public static final int LIBRARY_MAX = 6;
    public static final int OTHER_MIN = 1;
    public static final int OTHER_MAX = 2;

    private KnowledgeFragmentDrops() {
    }

    /** A roll in [0,1) succeeds when it falls below {@code chance}. */
    public static boolean roll(float value, float chance) {
        return value >= 0.0F && value < chance;
    }

    public static boolean cultistDrop(float value) {
        return roll(value, CULTIST_CHANCE);
    }
}
