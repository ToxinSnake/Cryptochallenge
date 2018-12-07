package de.hsel.felke.cryptochallenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Cracker 
{	
	
	private static final boolean debug = true;

    public static void main(String[] args)
    {
    	System.out.println( "Cryptochallenge!" );
        
//        if(args.length == 0) {
//        	System.out.println("Missing Filepath!\nUsage: Cracker publickey.txt");
//        	System.exit(1);
//        }
        
        ArrayList<Expression> pubKey = null;
        HashSet<String> allVars = new HashSet<String>();
        
        System.out.println("Trying to load public key...");
        try {
        	pubKey = getPublicKey("5Bit.txt");
        } catch (IOException e) {
        	e.printStackTrace();
        	System.exit(1);
        }
        System.out.println("Loading successfull!");
        
        //Alle Variablen in ein Set schreiben
        for(Expression iterator : pubKey) {
        	allVars.addAll(iterator.getVariableNames());
        }
        
        //Resultierende Matrix initialisieren
        System.out.println("Generating the full matrix...");
        int[][] fullMatrix = generateMatrix(pubKey, allVars);
        System.out.println("Done!");

        //Debug Anfang
		if (debug) {
			System.out.println("Full Matrix: ");
			for (int i = 0; i < fullMatrix.length; i++) {
				for (int j = 0; j < fullMatrix[i].length; j++) {
					System.out.print(fullMatrix[i][j] + " ");
				}
				System.out.println();
			}
		}		      
        //Debug Ende
		
		System.out.println("Computing reduced row echelon form...");
		int[][] reduced = reducedRowEchelon(fullMatrix);
		System.out.println("Done!");
	
		//Debug Anfang
		if (debug) {
			System.out.println("Reduced Matrix: ");
			for (int i = 0; i < reduced.length; i++) {
				for (int j = 0; j < reduced[i].length; j++) {
					System.out.print(reduced[i][j] + " ");
				}
				System.out.println();
			}
		}
        //Debug Ende

    }
    
    //Siehe https://en.wikipedia.org/wiki/Gaussian_elimination#Pseudocode
    //Siehe https://en.wikipedia.org/wiki/Row_echelon_form#Reduced_row_echelon_form
    //Ein Pivot-Element ist die erste 1 die in einer Spalte gefunden wird
    public static int[][] reducedRowEchelon(int[][] matrix){
    	int piRow = 0; //Pivot-Reihe V
    	int piCol = 0; //Pivot-Spalte >
    	int M = getM(matrix); //Zeilenanzahl
    	int N = getN(matrix); //Spaltenanzahl
    	
    	while(piRow < M && piCol < N) {
    		int elementPos = -1;
    		for(int i=piRow; i < M; i++) {
    			//Pivot-Element gefunden
    			if(matrix[i][piCol] == 1) {
    				elementPos = i;
    				break;
    			}
    		}
    		//Wenn kein Pivot-Element existiert mit nächster Spalte weiter
    		if(elementPos == -1) {
    			piCol++;
    		} else {
    			//Wenn Pivot-Element nicht in Reihe ist, für die ein Pivot-Element gesucht wird
    			if(elementPos != piRow) {
    				swapRow(matrix, piRow, elementPos);
    			}
    			
    			//Alle Zeilen XOR die eine 1 in piRow haben und unter dem Pivot-Element sind
    			for(int i=0; i < M; i++) {
    				//Rücksubstitution
    				if(i != piRow && matrix[i][piCol] == 1) {
    					xorRow(matrix, piRow, i);
    				}
    			}
    			piRow++;
    			piCol++;    			
    		}    		
    	}   	    	
    	return matrix;
    }
    
    //Row A ist oben, Row B wird verundet
    //^ = XOR
    private static void xorRow(int[][] matrix, int rowNoA, int rowNoB) {
    	int[] rowA = matrix[rowNoA];
		for (int i = 0; i < matrix[0].length; i++) {
			matrix[rowNoB][i] = matrix[rowNoB][i]^rowA[i];
		}
    }
    
    //Tauscht zwei Reihen miteinander
    private static void swapRow(int[][] matrix, int rowNoA, int rowNoB) {
    	int[] rowA = matrix[rowNoA];
    	int[] rowB = matrix[rowNoB];    	
    	matrix[rowNoA] = rowB;
    	matrix[rowNoB] = rowA;
    }
    
    private static int getN(int [][] matrix) {
    	return matrix[0].length;
    }
    
    private static int getM(int [][] matrix) {
    	return matrix.length;
    }

	private static int[][] generateMatrix(ArrayList<Expression> pubKey, HashSet<String> allVars) {
		int varCount;
		Random rand = new Random();
		varCount = allVars.size();
        System.out.println("Variables found: "+varCount);
        System.out.println("Allocating memory for "+2*varCount*varCount+"*"+varCount*varCount+" matrix...");
        int fullMatrix[][] = new int[2*varCount*varCount][varCount*varCount];
        System.out.println("Generating "+2*varCount*varCount+" cleartext-ciphertext pairs...");
        
        //2n^2 Klartexte erzeugen, einsetzen und resultierende Zeile erzeugen
        for(int i = 0; i < (2*varCount*varCount); i++ ) {        	
        	int [] clearText = new int[varCount];
        	int [] cipherText = new int[varCount];
        	
        	//Für jedes Bit eine 50% chance für 0 oder 1 sichern.
        	String binary = "";
        	for(int j = 0; j < varCount; j++) {
        		binary = Integer.toString(rand.nextInt(2)) + binary;
        	}
        	
        	//Binary String zahl für zahl in int array übertragen
        	for(int j = binary.length() - 1, n = 0; j >= 0; j--, n++) {
        		clearText[n] = binary.charAt(j) - '0';
        	}
        	
        	//Debug Anfang
        	if(debug) {
				System.out.print("Cleartext: ");
				for (int j = 0; j < clearText.length; j++) {
					System.out.print(clearText[j] + " ");
				}
				System.out.println();
        	} 	
        	//Debug Ende
        	
        	HashMap<String, Double> values = new HashMap<String, Double>();
        	for(String iterator : allVars) {
        		int varNum = iterator.charAt(2) - '0';
        		double varVal = clearText[varNum - 1];
        		values.put(iterator, varVal);
        	}
        	
        	if(debug) System.out.print("Chitext: ");
        	int expressionCounter = 0;
        	for(Expression iterator : pubKey) {
        		iterator.setVariables(values);
        		cipherText[expressionCounter] = (int)iterator.evaluate()%2;       		
        		if(debug) System.out.print(cipherText[expressionCounter] + " ");
        		expressionCounter++;
        	}
        	if(debug) System.out.println();

        	
        	if(debug) System.out.print("Resulting Row: ");
        	int rowCounter = 0;
        	for(int j = 0; j < clearText.length; j++) {
        		for(int s = 0; s < cipherText.length; s++) {
        			fullMatrix[i][rowCounter] = clearText[j]*cipherText[s];
        			if(debug) System.out.print(fullMatrix[i][rowCounter]+" ");
        			rowCounter++;
        		}
        	}
        	if(debug) System.out.println("\n------------");
        }
		return fullMatrix;
	}
    
    private static ArrayList<Expression> getPublicKey(String path) throws IOException {   	
    	ArrayList<Expression> pubKey = new ArrayList<Expression>();
    	HashSet<String> variables = new HashSet<String>();

    	byte[] allTextRaw = Files.readAllBytes(Paths.get(path));
    	String allText = new String(allTextRaw, "UTF-8");  	
    	String[] splits = allText.split("\\[|,|\\]");
    	
    	for(String outer : splits) {
    		String line = outer.trim();
    		if(line.contains("x_")){ 
    			if(debug) System.out.println(line);
    			String[] vars = line.split("\\*|\\+");
    			for(String inner : vars) {
    				variables.add(inner.trim());
    			}
    			Expression exp = new ExpressionBuilder(line)
    					.variables(variables)
    					.build();
    			pubKey.add(exp);
    			variables.clear();
    		}
    	}
    	return pubKey;
    }
}
