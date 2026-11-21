package arcaneresearch.lib.render.uv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UV-преобразования: чистая геометрия текстурных координат")
class UVTransformationsTest {

    private static final double DELTA = 1.0e-3;

    @Test
    void uvValueSemanticsAndCopying() {
        UV original = new UV(1.0, -2.0, 3);
        UV copy = original.copy();

        assertNotSame(original, copy);
        assertEquals(original, copy);
        // equals intentionally ignores the texture index.
        assertEquals(new UV(1.0, -2.0, 99), original);

        original.add(new UV(2.0, 1.0)).multiply(2.0);
        assertEquals(6.0, original.u, DELTA);
        assertEquals(-2.0, original.v, DELTA);
        assertEquals(3, original.tex);
        assertEquals(new UV(1.0, -2.0), copy);
    }

    @Test
    void translationAppliesAndInverts() {
        UVTranslation translation = new UVTranslation(0.25, -0.5);
        UV point = new UV(1.0, 1.0);

        point.apply(translation);
        assertEquals(1.25, point.u, DELTA);
        assertEquals(0.5, point.v, DELTA);

        point.apply(translation.inverse());
        assertEquals(new UV(1.0, 1.0), point);
    }

    @Test
    void adjacentTranslationsMergeIntoOne() {
        UVTransformation merged = new UVTranslation(1.0, 2.0).merge(new UVTranslation(3.0, 4.0));
        UV point = new UV(0.0, 0.0);

        merged.apply(point);
        assertEquals(4.0, point.u, DELTA);
        assertEquals(6.0, point.v, DELTA);
        assertTrue(new UVTranslation(1.0e-7, 0.0).isRedundant());
        assertFalse(new UVTranslation(0.01, 0.0).isRedundant());
    }

    @Test
    void scaleAppliesAndInverts() {
        UV point = new UV(2.0, 4.0);

        new UVScale(2.0).apply(point);
        assertEquals(new UV(4.0, 8.0), point);

        point.apply(new UVScale(2.0).inverse());
        assertEquals(new UV(2.0, 4.0), point);

        UV nonUniform = new UV(1.0, 1.0);
        new UVScale(2.0, 3.0).apply(nonUniform);
        assertEquals(new UV(2.0, 3.0), nonUniform);
    }

    @Test
    void rotationUsesTheSharedSinTableAndInverts() {
        UV point = new UV(1.0, 0.0);
        UVRotation quarterTurn = new UVRotation(Math.PI / 2);

        quarterTurn.apply(point);
        assertEquals(0.0, point.u, DELTA);
        assertEquals(-1.0, point.v, DELTA);

        point.apply(quarterTurn.inverse());
        assertEquals(1.0, point.u, DELTA);
        assertEquals(0.0, point.v, DELTA);
        assertTrue(new UVRotation(1.0e-7).isRedundant());
    }

    @Test
    void mergedRotationsCompose() {
        UV point = new UV(1.0, 0.0);
        UVTransformation merged = new UVRotation(Math.PI / 2).merge(new UVRotation(Math.PI / 2));

        merged.apply(point);
        assertEquals(-1.0, point.u, DELTA);
        assertEquals(0.0, point.v, DELTA);
    }

    @Test
    void listCompactsMergesAndSkipsRedundantTransforms() {
        UV point = new UV(0.0, 0.0);
        new UVTransformationList(
                new UVTranslation(0.0, 0.0),
                new UVTranslation(1.0, 0.0),
                new UVTranslation(2.0, 0.0)).apply(point);

        assertEquals(new UV(3.0, 0.0), point);
        assertTrue(new UVTransformationList(new UVTranslation(0.0, 0.0)).isRedundant());
    }

    @Test
    void withAppendsAndPrependPrepends() {
        UV appended = new UV(1.0, 1.0);
        new UVTransformationList(new UVScale(2.0)).with(new UVTranslation(1.0, 0.0)).apply(appended);
        assertEquals(new UV(3.0, 2.0), appended);

        UV prepended = new UV(1.0, 1.0);
        new UVTransformationList(new UVScale(2.0)).prepend(new UVTranslation(1.0, 0.0)).apply(prepended);
        assertEquals(new UV(4.0, 2.0), prepended);
    }

    @Test
    void listInverseReversesTheWholeChain() {
        UVTransformationList list = new UVTransformationList(
                new UVTranslation(1.0, -2.0), new UVScale(2.0), new UVRotation(Math.PI / 4));
        UV point = new UV(3.0, 5.0);
        UV original = point.copy();

        point.apply(list).apply(list.inverse());

        assertEquals(original.u, point.u, DELTA);
        assertEquals(original.v, point.v, DELTA);
    }

    @Test
    void atRotatesAroundTheGivenPoint() {
        UV point = new UV(2.0, 1.0);

        new UVRotation(Math.PI / 2).at(new UV(1.0, 1.0)).apply(point);

        assertEquals(1.0, point.u, DELTA);
        assertEquals(0.0, point.v, DELTA);
    }
}
