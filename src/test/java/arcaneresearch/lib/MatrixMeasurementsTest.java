package arcaneresearch.lib;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Измерения: векторы и матрицы")
class MatrixMeasurementsTest {

    private static final double DELTA = 1.0e-9;

    @Test
    void vectorMeasurements() {
        assertArrayEquals(new double[]{0, 0, 0}, matrix.VEC_ZERO(), DELTA);
        assertArrayEquals(new double[]{1, 2, 3}, matrix.VEC_COPY(new double[]{1, 2, 3}), DELTA);
        assertArrayEquals(new double[]{1, 2, 0}, matrix.VEC_COPY_2(new double[]{1, 2, 3}), DELTA);

        assertEquals(5.0, matrix.VEC_LENGTH(new double[]{3, 4, 0}), DELTA);
        assertEquals(25.0, matrix.VEC_LENGTH_2(new double[]{3, 4, 0}), DELTA);
        assertArrayEquals(new double[]{0, 1, 0}, matrix.VEC_NORMALIZE(new double[]{0, 3, 0}), DELTA);
        assertArrayEquals(new double[]{0, 0, 0}, matrix.VEC_NORMALIZE(new double[]{0, 0, 0}), DELTA);

        assertEquals(32.0, matrix.VEC_DOT_PRODUCT(new double[]{1, 2, 3}, new double[]{2, 3, 8}), DELTA);
        assertArrayEquals(new double[]{-4, 8, -4},
                matrix.VEC_CROSS_PRODUCT(new double[]{1, 2, 3}, new double[]{3, 2, 1}), DELTA);
        assertArrayEquals(new double[]{2, 4, 6}, matrix.VEC_SCALE(2.0, new double[]{1, 2, 3}), DELTA);
        assertArrayEquals(new double[]{1, -2, -3}, matrix.VEC_DIFF(new double[]{2, 0, 0}, new double[]{1, 2, 3}), DELTA);
        assertArrayEquals(new double[]{3, 2, 3}, matrix.VEC_SUM(new double[]{1, 2, 3}, new double[]{2, 0, 0}), DELTA);
    }

    @Test
    void reflectionAndPerpendicularComponent() {
        // Reflection across the Y axis flips the X component.
        assertArrayEquals(new double[]{-1, 2, 3},
                matrix.VEC_REFLECT(new double[]{1, 2, 3}, new double[]{1, 0, 0}), DELTA);
        double[] perpendicular = matrix.VEC_PERP(new double[]{1, 2, 3}, new double[]{0, 1, 0});
        assertArrayEquals(new double[]{1, 0, 3}, perpendicular, DELTA);
        assertEquals(0.0, matrix.VEC_DOT_PRODUCT(perpendicular, new double[]{0, 1, 0}), DELTA);
    }

    @Test
    void invalidVectorsAreRejected() {
        assertThrows(GLEException.class,
                () -> matrix.VEC_DOT_PRODUCT(new double[]{1, 2}, new double[]{1, 2, 3}));
        assertThrows(GLEException.class,
                () -> matrix.VEC_DIFF(new double[]{1, 2}, new double[]{1, 2, 3}));
        assertThrows(GLEException.class,
                () -> matrix.VEC_SUM(new double[]{1, 2, 3}, new double[]{1, 2}));
    }

    @Test
    void identitiesAndProducts() {
        double[][] identity3 = matrix.IDENTIFY_MATRIX_3X3();
        double[][] identity4 = matrix.IDENTIFY_MATRIX_4X4();
        assertEquals(3, identity3.length);
        assertEquals(4, identity4.length);
        assertArrayEquals(new double[]{1, 2, 3},
                matrix.MAT_DOT_VEC_3X3(identity3, new double[]{1, 2, 3}), DELTA);

        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5, 6}, {7, 8}};
        assertArrayEquals(new double[]{19, 22}, matrix.MATRIX_PRODUCT_2X2(a, b)[0], DELTA);
        assertArrayEquals(new double[]{1, 0, 0, 0},
                matrix.MATRIX_PRODUCT_4X4(identity4, identity4)[0], DELTA);
    }

    @Test
    void quarterTurnMatricesUseTheirOwnAxis() {
        double c = Math.cos(Math.PI / 2);
        double s = Math.sin(Math.PI / 2);
        double[][] x = matrix.urotx_cs_d(c, s);
        double[][] y = matrix.uroty_cs_d(c, s);
        double[][] z = matrix.urotz_cs_d(c, s);

        assertEquals(1.0, x[1][2], DELTA);
        assertEquals(-1.0, x[2][1], DELTA);
        assertEquals(-1.0, y[0][2], DELTA);
        assertEquals(1.0, y[2][0], DELTA);
        assertEquals(1.0, z[0][1], DELTA);
        assertEquals(-1.0, z[1][0], DELTA);

        assertArrayEquals(x, matrix.urot_cs_d(c, s, 'X'));
        assertArrayEquals(y, matrix.urot_cs_d(c, s, 'y'));
        assertArrayEquals(z, matrix.urot_cs_d(c, s, 'Z'));
        assertNull(matrix.urot_cs_d(c, s, '?'));
    }

    @Test
    void axisRotationIsOrthogonalAndKeepsItsAxis() {
        double[][] m = matrix.urot_about_axis_d(90.0, new double[]{0, 1, 0});

        assertArrayEquals(new double[]{0, 1, 0}, matrix.MAT_DOT_VEC_3X3(m, new double[]{0, 1, 0}), DELTA);
        assertEquals(1.0, determinant3(m), 1.0e-6);
        for (int i = 0; i < 3; i++) {
            double[] column = {m[0][i], m[1][i], m[2][i]};
            assertEquals(1.0, matrix.VEC_LENGTH(column), 1.0e-6);
            for (int j = i + 1; j < 3; j++) {
                double[] other = {m[0][j], m[1][j], m[2][j]};
                assertEquals(0.0, matrix.VEC_DOT_PRODUCT(column, other), 1.0e-6);
            }
        }
    }

    @Test
    void omegaVectorMagnitudeIsTheRotationAngle() {
        double[][] fromOmega = matrix.urot_omega_d(new double[]{0, Math.PI / 2, 0});
        double[][] fromDegrees = matrix.urot_about_axis_d(90.0, new double[]{0, 1, 0});

        for (int row = 0; row < 3; row++) {
            assertArrayEquals(fromDegrees[row], fromOmega[row], 1.0e-6);
        }
        assertThrows(GLEException.class, () -> matrix.urot_omega_d(new double[]{0, 0, 0}));
        assertThrows(GLEException.class, () -> matrix.urot_omega_d(new double[]{1, 2}));
    }

    private static double determinant3(double[][] m) {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
                - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
                + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
    }
}
