package arcaneresearch.client.renderers.item;

import arcaneresearch.common.blocks.essentia.JarFacing;
import net.minecraft.world.item.ItemDisplayContext;

/** Which side of the item jar the paper label is glued to. */
public final class JarItemLabelSide {
    private JarItemLabelSide() {
    }

    /**
     * Yaw of the +Z label quad. First-person hands are rotated 90 degrees in
     * opposite directions; GUI, item frames and third-person hands show the
     * back face; other views keep the label on +Z.
     */
    public static int yawDegrees(ItemDisplayContext transform) {
        if (transform == ItemDisplayContext.GUI || transform == ItemDisplayContext.FIXED
                || transform == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || transform == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            return JarFacing.labelYawDegrees(JarFacing.NORTH);
        }
        if (transform == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return JarFacing.labelYawDegrees(JarFacing.EAST);
        }
        if (transform == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            return JarFacing.labelYawDegrees(JarFacing.WEST);
        }
        return JarFacing.labelYawDegrees(JarFacing.SOUTH);
    }

    /** Human readable side for the debug log. */
    public static String name(int yawDegrees) {
        return switch (yawDegrees) {
            case 90 -> "EAST";
            case 180 -> "NORTH";
            case 270 -> "WEST";
            default -> "SOUTH";
        };
    }
}
