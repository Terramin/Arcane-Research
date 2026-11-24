package arcaneresearch.client.renderers.item;

import net.minecraft.world.item.ItemDisplayContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Jar: label side per item display context")
class JarItemLabelSideTest {

    @Test
    void guiAndFramesUseTheBackFace() {
        assertEquals(180, JarItemLabelSide.yawDegrees(ItemDisplayContext.GUI));
        assertEquals(180, JarItemLabelSide.yawDegrees(ItemDisplayContext.FIXED));
        assertEquals("NORTH", JarItemLabelSide.name(180));
    }

    @Test
    void thirdPersonHandsUseTheBackFace() {
        assertEquals(180, JarItemLabelSide.yawDegrees(ItemDisplayContext.THIRD_PERSON_LEFT_HAND));
        assertEquals(180, JarItemLabelSide.yawDegrees(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND));
    }

    @Test
    void firstPersonHandsAreMirrored() {
        // Left hand: 90 deg counter-clockwise from +Z.
        assertEquals(90, JarItemLabelSide.yawDegrees(ItemDisplayContext.FIRST_PERSON_LEFT_HAND));
        assertEquals("EAST", JarItemLabelSide.name(90));
        // Right hand: 90 deg clockwise from +Z.
        assertEquals(270, JarItemLabelSide.yawDegrees(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND));
        assertEquals("WEST", JarItemLabelSide.name(270));
    }

    @Test
    void droppedItemUsesTheFrontFace() {
        assertEquals(0, JarItemLabelSide.yawDegrees(ItemDisplayContext.GROUND));
        assertEquals("SOUTH", JarItemLabelSide.name(0));
    }
}
