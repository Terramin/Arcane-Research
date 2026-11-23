package arcaneresearch.common.blocks.essentia;

/**
 * Jar label facing, kept as plain ints so the mapping can be checked without
 * a Minecraft runtime.
 *
 * <p>Values follow the legacy Thaumcraft convention of {@code Direction.ordinal()}:
 * 2=NORTH, 3=SOUTH, 4=WEST, 5=EAST. This is not the same as
 * {@code Direction.get3DDataValue()} (where 2=SOUTH, 3=WEST, 4=NORTH).</p>
 */
public final class JarFacing {
    public static final int NORTH = 2;
    public static final int SOUTH = 3;
    public static final int WEST = 4;
    public static final int EAST = 5;

    private JarFacing() {
    }

    /** Side of the jar the player stands on, derived from the player's yaw in degrees. */
    public static int fromYaw(float yaw) {
        int l = (int) Math.floor(yaw * 4.0f / 360.0f + 0.5f) & 3;
        return switch (l) {
            case 0 -> NORTH; // looking +Z: player is north of the jar
            case 1 -> EAST;  // looking -X: player is east of the jar
            case 2 -> SOUTH; // looking -Z: player is south of the jar
            default -> WEST; // looking +X: player is west of the jar
        };
    }

    /**
     * Y rotation that moves the label quad (drawn on +Z) onto the given face.
     * The renderer does not apply a global X flip, so NORTH needs the 180 deg turn.
     */
    public static int labelYawDegrees(int facing) {
        return switch (facing) {
            case NORTH -> 180;
            case WEST -> 270;
            case EAST -> 90;
            default -> 0; // SOUTH
        };
    }
}
