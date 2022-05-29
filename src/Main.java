import simplex.Simplex;
import simplex.SimplexMode;

public class Main
{
    public static void main(String[] args)
    {
        double[][] tableau2 = { {3,6,1,  1,0,0, 0, 6},
                                {4,2,1,  0,1,0, 0, 4},
                                {1,-1,1, 0,0,1, 0, 3},
                                {-2,3,-1,0,0,0, 1, 0}};

        //SimplexTableau tableauTest = new SimplexTableau();

        //tableauTest.parseSystem(tableau2, 3);

        Simplex.start(tableau2, SimplexMode.DEFAULTMAXIMUM);
    }
}
