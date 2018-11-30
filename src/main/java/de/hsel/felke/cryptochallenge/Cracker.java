package de.hsel.felke.cryptochallenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Hello world!
 *
 */
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
        
        System.out.println("Trying to load public key...");
        try {
        	pubKey = getPublicKey("3Bit.txt");
        } catch (IOException e) {
        	e.printStackTrace();
        	System.exit(1);
        }
        System.out.println("Loading successfull!");
        //TESTCODE
        HashMap<String, Double> variablen = new HashMap<String, Double>();
        variablen.put("x_1", (double) 0);
        variablen.put("x_2", (double) 1);
        variablen.put("x_3", (double) 0);       
        pubKey.get(0).setVariables(variablen);
        pubKey.get(1).setVariables(variablen);
        pubKey.get(2).setVariables(variablen);
        System.out.println("Cleartext: 1 0 1");
        System.out.println("Chitext: "+ (int) pubKey.get(0).evaluate()%2 +" "+ (int) pubKey.get(1).evaluate()%2 +" "+ (int) pubKey.get(2).evaluate()%2);   
        //ENDE TESTCODE
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
    			System.out.println(line);
    			String[] vars = line.split("\\*|\\+");
    			for(String inner : vars) {
    				variables.add(inner.trim());
    			}
    			Expression exp = new ExpressionBuilder(line)
    					.variables(variables)
    					.build();
    			pubKey.add(exp);
    		}
    	}
    	return pubKey;
    }
    
    public static int[][] calcRowEchelon(int[][] matrix){
		
    	//TODO
    	return matrix;
    }
}
