package arcaneresearch.client.renderers.item;

import net.minecraft.world.item.ItemDisplayContext;

/**
 * Pure placement rule for the jar item's dropped pose. The jar model starts at
 * y=0 of the block frame, which leaves its base slightly below the item entity
 * origin; lifted only for ground items so hand/GUI poses stay unchanged.
 */
public final class JarItemGroundOffset {
    /** Applied after the display transform (which scales the ground pose by 0.625). */
    public static final float GROUND_LIFT = 0.12F;

    private JarItemGroundOffset() {
    }

    public static float lift(ItemDisplayContext transform) {
        return transform == ItemDisplayContext.GROUND ? GROUND_LIFT : 0.0F;
    }
}
