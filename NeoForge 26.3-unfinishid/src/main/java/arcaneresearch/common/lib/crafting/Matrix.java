package arcaneresearch.common.lib.crafting;

import arcaneresearch.api.crafting.Part;

public class Matrix {

    int rows;
    int cols;
    Part[][] matrix;

    public Matrix(Part[][] matrix) {
        this.rows = matrix.length;
        this.cols = matrix[0].length;
        this.matrix = new Part[rows][cols];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                this.matrix[i][j] = matrix[i][j];
            }
        }
    }

    public void Rotate90DegRight(int times) {
        for (int a = 0; a < times; ++a) {
            Part[][] newMatrix = new Part[cols][rows];

            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < cols; ++j) {
                    newMatrix[j][rows - i - 1] = matrix[i][j];
                }
            }

            matrix = newMatrix;

            int tmp = rows;
            rows = cols;
            cols = tmp;
        }
    }

    public Part[][] getMatrix() {
        return matrix;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
