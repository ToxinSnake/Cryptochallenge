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
	
	//Reduced Row Echelon Form
    public static int[][] rref(int[][] matrix)
	{
		int lead = 0, rowCount = matrix.length, columnCount = matrix[0].length;
		for (int r = 0; r < rowCount; r++) {
			if (columnCount <= lead) {
				break;
			}				
			int i = r;
			while (matrix[i][lead] == 0) {
				i++;
				if (i == rowCount) {
					i = r;
					lead++;
					if (columnCount == lead) {
						lead--;
						break;
					}
				}
			}
			for (int j = 0; j < columnCount; j++) {
				int temp = matrix[r][j];
				matrix[r][j] = matrix[i][j];
				matrix[i][j] = temp;
			}
			int div = matrix[r][lead];

			if (div != 0) {
				for (int j = 0; j < columnCount; j++) {
					matrix[r][j] = (matrix[r][j]/div);
				}	
			}	
			
			for (int j = 0; j < rowCount; j++) {
				if (j != r) {
					int sub = matrix[j][lead];
					for (int k = 0; k < columnCount; k++) {
						matrix[j][k] -= (sub * matrix[r][k]);
					}		
				}
			}
			lead++;
		}
//		for(int i = 0; i < matrix.length; i++) {
//			for(int j = 0; j < matrix[i].length; j++) {
//				matrix[i][j] = Math.abs(matrix[i][j])%2;
//			}
//		}		
		return matrix;
	}
}