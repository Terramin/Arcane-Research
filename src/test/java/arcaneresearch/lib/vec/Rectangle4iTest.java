package arcaneresearch.lib.vec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Rectangle4i: целочисленные прямоугольники")
class Rectangle4iTest {

    @Test
    void boundsAreInclusiveAndAreaUsesSize() {
        Rectangle4i rect = new Rectangle4i(2, 3, 4, 5);

        assertEquals(2, rect.x1());
        assertEquals(3, rect.y1());
        assertEquals(5, rect.x2());
        assertEquals(7, rect.y2());
        assertEquals(20, rect.area());
    }

    @Test
    void containsHasExclusiveUpperBound() {
        Rectangle4i rect = new Rectangle4i(0, 0, 2, 2);

        assertTrue(rect.contains(0, 0));
        assertTrue(rect.contains(1, 1));
        assertFalse(rect.contains(2, 0));
        assertFalse(rect.contains(0, 2));
        assertFalse(rect.contains(-1, 0));
    }

    @Test
    void includeExpandsToCoverPointsAndOtherRectangles() {
        Rectangle4i rect = new Rectangle4i(0, 0, 2, 2);

        assertSame(rect, rect.include(-1, 5));
        assertRectEquals(new Rectangle4i(-1, 0, 3, 6), rect);

        rect.include(new Rectangle4i(4, 8, 2, 2));
        assertRectEquals(new Rectangle4i(-1, 0, 7, 10), rect);
    }

    @Test
    void expandWithNegativeValuesMovesTheOrigin() {
        Rectangle4i rect = new Rectangle4i(0, 0, 2, 2);

        rect.expand(-1, -2);

        assertRectEquals(new Rectangle4i(-1, -2, 3, 4), rect);
        assertEquals(12, rect.area());
    }

    @Test
    void offsetMovesWithoutResizing() {
        Rectangle4i rect = new Rectangle4i(1, 2, 3, 4);

        assertSame(rect, rect.offset(5, -3));

        assertRectEquals(new Rectangle4i(6, -1, 3, 4), rect);
    }

    @Test
    void touchingRectanglesDoNotIntersect() {
        Rectangle4i rect = new Rectangle4i(0, 0, 2, 2);

        assertTrue(rect.intersects(new Rectangle4i(1, 1, 2, 2)));
        assertFalse(rect.intersects(new Rectangle4i(2, 0, 2, 2)));
        assertFalse(rect.intersects(new Rectangle4i(0, 2, 2, 2)));
        assertFalse(rect.intersects(new Rectangle4i(-2, 0, 2, 2)));
    }

    // Rectangle4i intentionally has no equals(): compare its state field by field.
    private static void assertRectEquals(Rectangle4i expected, Rectangle4i actual) {
        assertEquals(expected.x, actual.x, "x");
        assertEquals(expected.y, actual.y, "y");
        assertEquals(expected.w, actual.w, "w");
        assertEquals(expected.h, actual.h, "h");
    }
}
