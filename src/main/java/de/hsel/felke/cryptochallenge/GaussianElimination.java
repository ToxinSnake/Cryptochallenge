package de.hsel.felke.cryptochallenge;

/******************************************************************************
 *  Compilation:  javac GaussianElimination.java
 *  Execution:    java GaussianElimination
 * 
 *  Gaussian elimination with partial pivoting.
 *
 *  % java GaussianElimination
 *  -1.0
 *  2.0
 *  2.0
 *
 ******************************************************************************/

public class GaussianElimination {
    // Gaussian elimination with partial pivoting
    public static int[][] lsolve(int[][] A, int[] b) {
        int n = b.length;

        for (int p = 0; p < n; p++) {

            // find pivot row and swap
            int max = p;
            for (int i = p + 1; i < n; i++) {
                if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                    max = i;
                }
            }
            int[] temp = A[p]; A[p] = A[max]; A[max] = temp;
            int   t    = b[p]; b[p] = b[max]; b[max] = t;

            // singular or nearly singular

            // pivot within A and b
            for (int i = p + 1; i < n; i++) {
                int alpha = A[i][p] / A[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < n; j++) {
                    A[i][j] -= alpha * A[p][j];
                }
            }
        }
        return A;
        
        // back substitution
//        int[] x = new int[n];
//        for (int i = n - 1; i >= 0; i--) {
//            int sum = 0;
//            for (int j = i + 1; j < n; j++) {
//                sum += A[i][j] * x[j];
//            }
//            x[i] = (b[i] - sum) / A[i][i];
//        }
//        return x;
    }
}