package simplex;

import utils.IntegerToRoman;

public class Simplex
{
    public static void start(SimplexTableau tableau, SimplexMode mode)
    {
        switch (mode)
        {
            case DEFAULTMAXIMUM:
                defaultMethod(tableau);
        }
    }

    public static void start(double[][] tableau, SimplexMode mode)
    {
        switch (mode)
        {
            case DEFAULTMAXIMUM:
                SimplexTableau simplexTableau = new SimplexTableau();

                simplexTableau.parseDefaultMaximum(tableau);

                defaultMethod(simplexTableau);
        }
    }

    private static void defaultMethod(SimplexTableau tableau)
    {
        if (tableau.isEmpty())
        {
            System.out.println("Can not start the Simplex method because the tableau is empty.");

            return;
        }

        int[] pivotIndex = findPivot(tableau);

        int simplexStep = 0;

        while (pivotIndex[0] != -1)
        {
            int pivotRow = pivotIndex[0];
            int pivotCol = pivotIndex[1];

            double pivotValue = tableau.getValue(pivotRow, pivotCol);

            printPivotInterimTableau(tableau, pivotRow, pivotCol);

            tableau.resetQuotientColumn();

            /* normalize the pivot row */
            for (int col = 0; col < tableau.getColumnCount(); col++)
            {
                tableau.setValue(pivotRow, col, tableau.getValue(pivotRow, col) / pivotValue);
            }

            System.out.println("Normalize step: ");
            printNormalizeSteps(tableau, pivotRow, pivotCol);

            /* make the column a basis vector */
            for (int row = 0; row < tableau.getRowCount(); row++)
            {
                /* ignore pivot row */
                if (row == pivotRow)
                {
                    continue;
                }

                double multiplier = tableau.getValue(row, pivotCol);

                for (int col = 0; col < tableau.getColumnCount(); col++)
                {
                    tableau.setValue(row, col, tableau.getValue(row, col) - multiplier * tableau.getValue(pivotRow, col));
                }
            }

            System.out.println("Result of simplex step " + simplexStep);
            tableau.printTableau();

            pivotIndex = findPivot(tableau);

            simplexStep++;
        }

        tableau.printResults();
    }

    private static int[] findPivot(SimplexTableau tableau)
    {
        int pivotCol = findPivotColumn(tableau);

        /* every Z is equal or greater than 0 -> Z is optimal */
        if (pivotCol == -1)
        {
            return new int[]{-1, -1};
        }

        double qMin = Double.MAX_VALUE;
        int pivotRow = 0;

        /* find pivot row in the pivot column */
        for (int row = 0; row < tableau.getRowCount(); row++)
        {
            double x = tableau.getValue(row, pivotCol);

            /* b should not become negative nor is b / 0 allowed */
            if (x <= 0)
            {
                continue;
            }

            double b = tableau.getBvalue(row);
            double q = b / x;

            if (q < qMin)
            {
                pivotRow = row;
                qMin = q;
            }

            tableau.setQuotientInterim(row, q);
        }

        tableau.markPivotElement(pivotRow, pivotCol);

        return new int[]{pivotRow, pivotCol};
    }

    /**
     * Find the pivot column based on the minimum coefficient of Z (optimum criteria)
     * @param tableau
     * @return index of column
     */
    private static int findPivotColumn(SimplexTableau tableau)
    {
        int minCol = -1;

        for (int col = 0; col < tableau.getColumnCount(); col++)
        {
            /* Ignore Z, b and q columns */
            if (tableau.getTableauColumnType(col).type == SimplexTableau.TableauType.B
                    || tableau.getTableauColumnType(col).type == SimplexTableau.TableauType.Z
                    || tableau.getTableauColumnType(col).type == SimplexTableau.TableauType.QUOTIENT)
            {
                continue;
            }

            double z = tableau.getZvalue(col);
            double zmin = (minCol == -1) ? Double.MAX_VALUE : tableau.getZvalue(minCol);

            /* pivot column only concerns negative Z coefficients*/
            if (z >= 0)
            {
                continue;
            }

            if (z < zmin)
            {
                minCol = col;
            }
        }

        return minCol;
    }

    private static void printPivotInterimTableau(SimplexTableau tableau, int pivotRow, int pivotColumn)
    {
        double pivotValue = tableau.getValue(pivotRow, pivotColumn);

        System.out.println();
        System.out.println("Pivot element:");
        String[] postColumnPivotDivisor = new String[tableau.getRowCount()];

        postColumnPivotDivisor[pivotRow] = " / " + encloseNegativeValue(pivotValue);
        tableau.printTableau(postColumnPivotDivisor);
    }

    private static void printNormalizeSteps(SimplexTableau tableau, int pivotRow, int pivotColumn)
    {
        String[] normalizeStepsColumn = new String[tableau.getRowCount()];

        for (int row = 0; row < tableau.getRowCount(); row++)
        {
            /* ignore pivot row */
            if (row == pivotRow)
            {
                continue;
            }

            double multiplier = tableau.getValue(row, pivotColumn);

            if (multiplier == 0)
            {
                continue;
            }

            normalizeStepsColumn[row] = " " + IntegerToRoman.toRoman(row + 1) + " - " + IntegerToRoman.toRoman(pivotRow + 1) + " * " + encloseNegativeValue(multiplier);
        }

        tableau.printTableau(normalizeStepsColumn);
    }

    private static String encloseNegativeValue(double value)
    {
        boolean valueNegative = value < 0;

        return ((valueNegative) ? "(" : "") + value + ((valueNegative) ? ")" : "");
    }
}
