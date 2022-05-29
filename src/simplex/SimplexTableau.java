package simplex;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SimplexTableau
{
    private TableauIndex[] columnTypes;
    private TableauIndex[] rowTypes;
    private double[][] values;

    public SimplexTableau()
    {

    }

    public boolean isEmpty()
    {
        return this.values == null;
    }

    public int getColumnCount()
    {
        return this.columnTypes.length;
    }

    public int getRowCount()
    {
        return this.rowTypes.length;
    }

    public double getValue(int row, int column) throws IndexOutOfBoundsException
    {
        return this.values[row][column];
    }

    public void setValue(int row, int column, double value) throws IndexOutOfBoundsException
    {
        this.values[row][column] = value;
    }

    public double getZvalue(int column) throws IndexOutOfBoundsException
    {
        return this.values[this.values.length - 1][column];
    }

    public double getBvalue(int row) throws IndexOutOfBoundsException
    {
        return this.values[row][this.getColumnCount() - 2];
    }

    public void setQuotientInterim(int row, double value) throws IndexOutOfBoundsException
    {
        this.values[row][this.getColumnCount() - 1] = value;
    }

    public void resetQuotientColumn()
    {
        for (int row = 0; row < this.values.length; row++)
        {
            this.values[row][this.getColumnCount() - 1] = 0;
        }
    }

    public void markPivotElement(int row, int column) throws IndexOutOfBoundsException
    {
        this.rowTypes[row] = this.columnTypes[column];
    }

    public TableauIndex getTableauRowType(int row) throws IndexOutOfBoundsException
    {
        return new TableauIndex(this.rowTypes[row].type, this.rowTypes[row].index);
    }

    public TableauIndex getTableauColumnType(int column) throws IndexOutOfBoundsException
    {
        return new TableauIndex(this.columnTypes[column].type, this.columnTypes[column].index);
    }



    /*public void parseSystem(String[] variables, String z)
    {
        List<TableauIndex> columnTypesList = new ArrayList<>();
        List<TableauIndex> rowTypesList = new ArrayList<>();
        List<List<Double>> valuesList = new ArrayList<>();

        for (int i = 0; i < variables.length; i++)
        {
            String row = variables[i];
            String[] values = row.split(",");

            for (int j = 0; j < values.length; j++)
            {
                String value = values[j].replace(' ', '\0');

                if (value.contains("<") || value.contains("=") || value.contains(">"))
                {

                }
                else
                {

                }
            }
        }
    }*/

    public void parseSystem(double[][] tableau, SimplexMode mode)
    {
        switch (mode)
        {
            case DEFAULTMAXIMUM:
                this.parseDefaultMaximum(tableau);
        }
    }

    public void parseDefaultMaximum(double[][] tableau)
    {
        int slackVarCount = tableau.length - 1;
        boolean zColumnTest = true;

        /* test if Z column is there */
        for (int row = 0; row < tableau.length; row++)
        {
            if (row < tableau.length - 1 && tableau[row][tableau[row].length - 2] != 0)
            {
                zColumnTest = false;

                break;
            }
            else if(row == tableau.length - 1 && tableau[row][tableau[row].length - 2] != 1)
            {
                zColumnTest = false;

                break;
            }
        }

        if (!zColumnTest)
        {
            System.out.println("Parsing error: it seems like the Z column is missing, or the amount of columns differs per row.");

            return;
        }

        int varCount = tableau[0].length - 2 - slackVarCount;

        this.parseSystem(tableau, varCount, slackVarCount, 0);
    }

    public void parseSystem(double[][] tableau, int varCount)
    {
        this.parseSystem(tableau, varCount, tableau.length - 1, 0);
    }

    public void parseSystem(double[][] tableau, int varCount, int slackVarCount, int helperVarCount)
    {
        this.columnTypes = new TableauIndex[varCount + slackVarCount + helperVarCount + 3];
        this.rowTypes = new TableauIndex[tableau.length];

        /* if helper variables exist, there should also be Z' row */
        int rowNumExpectation = slackVarCount + helperVarCount + ((helperVarCount != 0) ? 2 : 1);
        int columnNumExpectation = varCount + slackVarCount + helperVarCount + ((helperVarCount != 0) ? 2 : 1) + 1;

        if (rowNumExpectation != (tableau.length))
        {
            System.out.println("Parsing error: the tableau has more or less rows than there should be.");
            System.out.println("You entered " + slackVarCount + " amount of slack variables and " + helperVarCount + " amount of helper variables.");
            System.out.println("There should be " + rowNumExpectation + " rows. The input tableau has " + tableau.length + " rows.");

            if (helperVarCount != 0)
            {
                System.out.println("Since helper variables exist there should be also a Z' row for the two phase method.");
            }

            this.values = null;

            return;
        }

        for (int i = 0; i < this.columnTypes.length; i++)
        {
            if (i < varCount)
            {
                this.columnTypes[i] = new TableauIndex(TableauType.VARIABLE, i + 1);
            }
            else if ((i - varCount) < slackVarCount)
            {
                this.columnTypes[i] = new TableauIndex(TableauType.SLACKVARIABLE, i - varCount + 1);
                this.rowTypes[(i - varCount)] = new TableauIndex(TableauType.SLACKVARIABLE, i - varCount + 1);
            }
            else if ((i - varCount - slackVarCount) < helperVarCount)
            {
                this.columnTypes[i] = new TableauIndex(TableauType.HELPERVARIABLE, i - varCount - slackVarCount + 1);
                this.rowTypes[(i - varCount)] = new TableauIndex(TableauType.HELPERVARIABLE, i - varCount - slackVarCount + 1);
            }
            else
            {
                if (this.columnTypes.length - i != 3)
                {
                    System.out.println("Parsing error: mistake in assigning the tableau index. Something went wrong during calculation of column amount, WTF");

                    this.values = null;

                    return;
                }

                this.columnTypes[i] = new TableauIndex(TableauType.Z);
                this.columnTypes[i + 1] = new TableauIndex(TableauType.B);
                this.columnTypes[i + 2] = new TableauIndex(TableauType.QUOTIENT);

                this.rowTypes[(i - varCount)] = new TableauIndex(TableauType.Z);

                break;
            }
        }

        this.values = new double[this.rowTypes.length][this.columnTypes.length];

        int prevRowLength = tableau[0].length;

        for (int row = 0; row < tableau.length; row++)
        {
            if (prevRowLength != tableau[row].length)
            {
                System.out.println("Parsing error: row " + (row + 1) + " has a different length than the previous rows - all rows must have equal lengths!");

                this.values = null;

                return;
            }

            for (int col = 0; col < tableau[row].length; col++)
            {
                this.values[row][col] = tableau[row][col];
            }

            prevRowLength = tableau[row].length;
        }

        if (columnNumExpectation != prevRowLength)
        {
            System.out.println("Parsing error: the amount of columns is not correct. You specified " + varCount + " variables, " + slackVarCount + " slack variables and " + helperVarCount + " helper variables.");
            System.out.println("Together with the Z" + (helperVarCount != 0 ? ", Z'" : "") + " and b column it should result in " + columnNumExpectation + ". The input tableau has " + prevRowLength + " columns.");

            this.values = null;

            return;
        }

        System.out.println("Parsing complete:\n");

        this.printTableau();
    }

    public void printTableau()
    {
        this.printTableau(new String[0]);
    }

    public void printTableau(String[] postColumn)
    {
        int offset = 4;
        int maxLength = 0;
        int decimalPlacesThreshold = 5;

        /* find the max length of string */
        for (double[] row : this.values)
        {
            for (double value : row)
            {
                int length = Double.valueOf(value).toString().length();

                String valueStr = String.valueOf(value);

                if (valueStr.substring(valueStr.indexOf(".") + 1).length() > decimalPlacesThreshold)
                {
                    length = 5;
                }

                if (length > maxLength)
                {
                    maxLength = length;
                }
            }
        }

        System.out.printf("%-" + (maxLength + offset) + "s", "");

        /* print the column tyes */
        for (int col = 0; col < this.columnTypes.length; col++)
        {
            System.out.printf("%" + ((col == 0) ? 5 : (maxLength + offset)) + "s", this.columnTypes[col]);
        }

        System.out.println();

        /* print the rows*/
        for (int row = 0; row < this.values.length; row++)
        {
            System.out.printf("%" + (maxLength + offset) + "s", this.rowTypes[row] + " |");

            for (int col = 0; col < this.values[row].length; col++)
            {
                double value = this.values[row][col];


                /* format doubles that exceed the decimal places threshold */
                String valueStr = String.valueOf(value);

                if (valueStr.substring(valueStr.indexOf(".") + 1).length() > decimalPlacesThreshold)
                {
                    String formatDecimals = (new String(new char[decimalPlacesThreshold])).replace('\0', '#');

                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

                    DecimalFormat dcFormat = new DecimalFormat("#." + formatDecimals, symbols);

                    value = Double.valueOf(dcFormat.format(value));
                }

                /* print the value */
                System.out.printf("%" + ((col == 0) ? 5 : (maxLength + offset)) + "s", value);

                /* print the post column */
                if (col == this.values[row].length - 1 && row < postColumn.length && postColumn[row] != null)
                {
                    System.out.printf("%" + (maxLength + offset) + "s", postColumn[row]);
                }
            }

            System.out.println();
        }

        System.out.println();
    }

    public void printResults()
    {
        for (int row = 0; row < this.getRowCount(); row++)
        {
            System.out.println(this.rowTypes[row] + " = " + this.getBvalue(row));
        }
    }

    public class TableauIndex
    {
        public final TableauType type;
        public final int index;

        public TableauIndex(TableauType type, int index)
        {
            this.type = type;
            this.index = index;
        }

        public TableauIndex(TableauType type)
        {
            this.type = type;
            this.index = -1;
        }

        @Override
        public String toString()
        {
            return this.type.name + ((this.index == -1) ? "" : this.index);
        }
    }

    public enum TableauType
    {
        VARIABLE("x"),
        SLACKVARIABLE("s"),
        HELPERVARIABLE("sh"),
        Z("Z"),
        B("b"),
        QUOTIENT("q");

        private final String name;

        TableauType(String name)
        {
            this.name = name;
        }
    }
}
