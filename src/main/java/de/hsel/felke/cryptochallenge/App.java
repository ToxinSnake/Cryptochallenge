package de.hsel.felke.cryptochallenge;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Cryptochallenge!" );
        System.out.println("Gaussian Elimination Algorithm Test\n");

        /** Make an object of GaussianElimination class **/
        
        double[][] A = {
                { 1, 0, 0 },
                { 1, 1, 0 },
                { 0, 0, 1 }
            };
        double[] b = { 0, 0, 0 };

        double x [] = GaussianElimination.lsolve(A, b);
        
        System.out.println(x[0]);
        System.out.println(x[1]);
        System.out.println(x[2]);
    }
}
