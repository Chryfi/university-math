package utils;

public class FormattedTablePrint
{
    public static void printRow(int[] row)
    {
        for (int i : row) {
            System.out.print(i);
            System.out.print("\t");
        }
        System.out.println();
    }

    public static void print(String[] table)
    {
        int twoDm[][]= new int[7][5];
        int i,j,k=1;

        for(i=0;i<7;i++)
        {
            for(j=0;j<5;j++)
            {
                twoDm[i][j]=k;
                k++;
            }
        }

        for(int[] row : twoDm)
        {
            printRow(row);
        }
    }

    public static void printMatrix(Object[][] matrix)
    {
        int maxLength = 0;

        for (Object[] row : matrix)
        {
            for (Object col : row)
            {
                if (col.toString().length() > maxLength)
                {
                    maxLength = col.toString().length();
                }
            }
        }

        for (int row = 0; row < matrix.length; row++)
        {
            for (int col = 0; col < matrix[row].length; col++)
            {
                System.out.printf("%-" + (maxLength + 2) + "s", matrix[row][col].toString());
            }

            System.out.println();
        }
    }
}
