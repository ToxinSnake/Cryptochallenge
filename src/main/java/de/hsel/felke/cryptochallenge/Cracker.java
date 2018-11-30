package de.hsel.felke.cryptochallenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Cracker 
{
    public static void main(String[] args)
    {
        System.out.println( "Cryptochallenge!" );
        
//        if(args.length == 0) {
//        	System.out.println("Missing Filepath!\nUsage: Cracker publickey.txt");
//        	System.exit(1);
//        }
        
        ArrayList<Expression> pubKey = null;
        HashSet<String> allVars = new HashSet<String>();
        int varCount = 0;
        
        System.out.println("Trying to load public key...");
        try {
        	pubKey = getPublicKey("3Bit.txt");
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
        varCount = allVars.size();
        System.out.println("Variables found: "+varCount);
        System.out.println("Allocating memory for "+varCount*varCount+"*"+2*varCount*varCount+" matrix...");
        int fullMatrix[][] = new int[2*varCount*varCount][varCount*varCount];
        
        //2n^2 Klartexte erzeugen, einsetzen und resultierende Zeile erzeugen
        for(int i = 0; i <= (2*varCount*varCount); i++ ) {
        	String binary = Integer.toBinaryString(i+1);
        	int [] clearText = new int[varCount];
        	int [] cipherText = new int[varCount];
        	
        	//Wenn 2n^2 > 2^n
        	if (binary.length() > clearText.length) {
        		System.out.println("Generated all possible cleartexts!");
        		break;
        	}
        	
        	//Binary String zahl für zahl in int array übertragen
        	for(int j = binary.length() - 1, n = 0; j >= 0; j--, n++) {
        		clearText[n] = binary.charAt(j) - '0';
        	}
        	
        	//Debug Anfang
        	System.out.print("Cleartext: ");
        	for(int j = 0; j < clearText.length; j++) {
        		System.out.print(clearText[j]+" ");
        	}
        	System.out.println();
        	//Debug Ende
        	
        	HashMap<String, Double> values = new HashMap<String, Double>();
        	for(String iterator : allVars) {
        		int varNum = iterator.charAt(2) - '0';
        		double varVal = clearText[varNum - 1];
        		values.put(iterator, varVal);
        	}
        	
        	System.out.print("Chitext: ");
        	int expressionCounter = 0;
        	for(Expression iterator : pubKey) {
        		iterator.setVariables(values);
        		cipherText[expressionCounter] = (int)iterator.evaluate()%2;       		
        		System.out.print(cipherText[expressionCounter] + " ");
        		expressionCounter++;
        	}
        	System.out.println();
        	
        	System.out.print("Resulting Row: ");
        	int rowCounter = 0;
        	for(int j = 0; j < clearText.length; j++) {
        		for(int s = 0; s < cipherText.length; s++) {
        			fullMatrix[i][rowCounter] = clearText[j]*cipherText[s];
        			System.out.print(fullMatrix[i][rowCounter]+" ");
        			rowCounter++;
        		}
        	}
        	System.out.println("\n------------");
        }
        
        //Debug Anfang
        System.out.println("Full Matrix: ");
        for(int i = 0; i < fullMatrix.length; i++) {
        	for(int j = 0; j < fullMatrix[i].length; j++) {
        		System.out.print(fullMatrix[i][j]+" ");
        	}
        	System.out.println();
        }
        //Debug Ende
    }
    
    public static ArrayList<Expression> getPublicKey(String path) throws IOException {   	
    	ArrayList<Expression> pubKey = new ArrayList<Expression>();
    	HashSet<String> variables = new HashSet<String>();

    	byte[] allTextRaw = Files.readAllBytes(Paths.get(path));
    	String allText = new String(allTextRaw, "UTF-8");  	
    	String[] splits = allText.split("\\[|,|\\]");
    	
    	for(String outer : splits) {
    		String line = outer.trim();
    		if(line.contains("x_")){ 
//    			System.out.println(line);
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
    
    public static int[][] calcRowEchelon(int[][] matrix){
		
    	//TODO
    	return matrix;
    }
}
