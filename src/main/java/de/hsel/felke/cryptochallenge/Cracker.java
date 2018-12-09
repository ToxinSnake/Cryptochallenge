package de.hsel.felke.cryptochallenge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Cracker {

	private static final boolean debug = false;
	public static int numberOfVars;

	public static void main(String[] args) {
//        if(args.length == 0) {
//        	System.out.println("Missing Filepath!\nUsage: Cracker publickey.txt");
//        	System.exit(1);
//        }

		Date startTime = new Date();
		Date endTime = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss.SSS");
		System.out.println("Start: " + ft.format(startTime));

		ArrayList<Expression> pubKey = null;
		HashSet<String> allVars = new HashSet<String>();

		System.out.println("Trying to load public key...");
		try {
			pubKey = getPublicKey("45Bit.txt");
		} catch (IOException e) {
			System.out.println("File not found!\nExiting.");
			System.exit(1);
		}
		System.out.println("Loading successfull!");

		// Alle Variablen in ein Set schreiben
		for (Expression iterator : pubKey) {
			allVars.addAll(iterator.getVariableNames());
		}
		numberOfVars = allVars.size();
		
		//TODO: Ciphertext auslesen

		// Resultierende Matrix initialisieren
		System.out.println("Generating the full matrix...");
		int[][] fullMatrix = generateMatrix(pubKey, allVars);
		System.out.println("Done!");

		// Debug Anfang
		if (debug) {
			System.out.println("Full matrix: ");
			for (int i = 0; i < fullMatrix.length; i++) {
				for (int j = 0; j < fullMatrix[i].length; j++) {
					System.out.print(fullMatrix[i][j] + " ");
				}
				System.out.println();
			}
		}
		// Debug Ende

		System.out.println("Computing reduced row echelon form...");
		int[][] reduced = reducedRowEchelon(fullMatrix);
		System.out.println("Done!");

		// Debug Anfang
		if (debug) {
			System.out.println("Reduced matrix: ");
			for (int i = 0; i < reduced.length; i++) {
				for (int j = 0; j < reduced[i].length; j++) {
					System.out.print(reduced[i][j] + " ");
				}
				System.out.println();
			}
		}
		// Debug Ende

		System.out.println("Calculating special solutions...");
		int[][] specials = specialSolutions(reduced);
		System.out.println("Done!");
		
		//TODO: Ciphertext in Spezielle Lösungen einsetzen und Gleichungssystem reduzieren

		endTime.setTime(System.currentTimeMillis());
		System.out.println("End: " + ft.format(endTime));
		Date timeNeeded = new Date(endTime.getTime() - startTime.getTime() - 3600000);
		System.out.println("Time needed: " + ft.format(timeNeeded));
	}

	/**
	 * Lädt den Public Key aus einer Datei und sucht nach den Gleichungen. Aus den
	 * Gleichungen werden Expressions gebaut.
	 * 
	 * @param path - Dateipfad zum Public-Key
	 * @return Liste mit allen Gleichungen als Expressions
	 * @throws IOException - Wenn die Datei nicht gefunden oder geöffnet werden kann
	 */
	public static ArrayList<Expression> getPublicKey(String path) throws IOException {
		ArrayList<Expression> pubKey = new ArrayList<Expression>();
		HashSet<String> variables = new HashSet<String>();

		byte[] allTextRaw = Files.readAllBytes(Paths.get(path));
		String allText = new String(allTextRaw, "UTF-8");
		// Splitten wenn [ oder , oder ] gefunden wird
		String[] splits = allText.split("\\[|,|\\]");

		for (String outer : splits) {
			String line = outer.trim();
			// Wenn Element mindestens eine Variable enthält
			if (line.contains("x_")) {
				if (debug)
					System.out.println(line);
				// Element nach jeder Operation splitten um alle Variablen zu finden
				String[] vars = line.split("\\*|\\+");
				for (String inner : vars) {
					variables.add(inner.trim());
				}
				// Expression aus der Zeile bilden und alle Variablen mitgeben
				Expression exp = new ExpressionBuilder(line).variables(variables).build();
				pubKey.add(exp);
				variables.clear();
			}
		}
		return pubKey;
	}

	/**
	 * Genertiert Klartexte und verschlüsselt diese mit dem Public-Key. Setzt diese
	 * außerdem in die Relation ein um die vollständige Matrix zu generieren
	 * 
	 * @param pubKey  - Alle Gleichungen des Public-Keys
	 * @param allVars - Alle Variablen
	 * @return Ein Integer-Array mit allen in die Relation eingesetzten Klar- und
	 *         Chitexten
	 */
	public static int[][] generateMatrix(ArrayList<Expression> pubKey, HashSet<String> allVars) {
		int varCount;
		Random rand = new Random();
		varCount = allVars.size();
		System.out.println("Variables found: " + varCount);
		System.out
				.println("Allocating memory for " + 2 * varCount * varCount + "*" + varCount * varCount + " matrix...");
		int fullMatrix[][] = new int[2 * varCount * varCount][varCount * varCount];
		System.out.println("Generating " + 2 * varCount * varCount + " cleartext-ciphertext pairs...");

		// 2n^2 Klartexte erzeugen, einsetzen und resultierende Zeile erzeugen
		for (int i = 0; i < (2 * varCount * varCount); i++) {
			int[] clearText = new int[varCount];
			int[] cipherText = new int[varCount];

			// Für jedes Bit eine 50% chance für 0 oder 1 sichern.
			String binary = "";
			for (int j = 0; j < varCount; j++) {
				binary = Integer.toString(rand.nextInt(2)) + binary;
			}

			// Binary String zahl für zahl in int array übertragen
			for (int j = binary.length() - 1, n = 0; j >= 0; j--, n++) {
				clearText[n] = binary.charAt(j) - '0';
			}

			// Debug Anfang
			if (debug) {
				System.out.print("Cleartext: ");
				for (int j = 0; j < clearText.length; j++) {
					System.out.print(clearText[j] + " ");
				}
				System.out.println();
			}
			// Debug Ende

			HashMap<String, Double> values = new HashMap<String, Double>();
			for (String iterator : allVars) {
				int varNum = iterator.charAt(2) - '0';
				double varVal = clearText[varNum - 1];
				values.put(iterator, varVal);
			}

			if (debug)
				System.out.print("Chitext: ");
			int expressionCounter = 0;
			for (Expression iterator : pubKey) {
				iterator.setVariables(values);
				cipherText[expressionCounter] = (int) iterator.evaluate() % 2;
				if (debug)
					System.out.print(cipherText[expressionCounter] + " ");
				expressionCounter++;
			}
			if (debug)
				System.out.println();

			if (debug)
				System.out.print("Resulting Row: ");
			int rowCounter = 0;
			for (int j = 0; j < clearText.length; j++) {
				for (int s = 0; s < cipherText.length; s++) {
					fullMatrix[i][rowCounter] = clearText[j] * cipherText[s];
					if (debug)
						System.out.print(fullMatrix[i][rowCounter] + " ");
					rowCounter++;
				}
			}
			if (debug)
				System.out.println("\n------------");
		}
		return fullMatrix;
	}

	/**
	 * Bringt eine Matrix mithilfe des Gauß-Verfahrens in die erweiterte
	 * Zeilenstufenform Info: Ein Pivot-Element ist die erste 1 die in einer Spalte
	 * gefunden wird
	 * 
	 * Siehe: https://en.wikipedia.org/wiki/Gaussian_elimination#Pseudocode Siehe:
	 * https://en.wikipedia.org/wiki/Row_echelon_form#Reduced_row_echelon_form
	 * 
	 * @param matrix - Matrix die in die erweiterte Zeilenstufenform gebracht werden
	 *               soll
	 * @return Eine Matrix in erweiterter Zeilenstufenform
	 */

	public static int[][] reducedRowEchelon(int[][] matrix) {
		int piRow = 0; // Pivot-Reihe
		int piCol = 0; // Pivot-Spalte
		int M = getM(matrix); // Zeilenanzahl
		int N = getN(matrix); // Spaltenanzahl

		while (piRow < M && piCol < N) {
			int elementPos = -1;
			for (int i = piRow; i < M; i++) {
				// Pivot-Element gefunden
				if (matrix[i][piCol] == 1) {
					elementPos = i;
					break;
				}
			}
			// Wenn kein Pivot-Element existiert mit nächster Spalte weiter
			if (elementPos == -1) {
				piCol++;
			} else {
				// Wenn Pivot-Element nicht in Reihe ist, für die ein Pivot-Element gesucht wird
				if (elementPos != piRow) {
					swapRow(matrix, piRow, elementPos);
				}
				// Alle Zeilen XOR die eine 1 in piRow haben und unter dem Pivot-Element sind
				for (int i = 0; i < M; i++) {
					// Rücksubstitution
					if (i != piRow && matrix[i][piCol] == 1) {
						xorRow(matrix, piRow, i);
					}
				}
				piRow++;
				piCol++;
			}
		}
		return matrix;
	}

	private static int getN(int[][] matrix) {
		return matrix[0].length;
	}

	private static int getM(int[][] matrix) {
		return matrix.length;
	}

	/**
	 * Tauscht zwei Reihen miteinander
	 * 
	 * @param matrix
	 * @param rowNoA
	 * @param rowNoB
	 */
	private static void swapRow(int[][] matrix, int rowNoA, int rowNoB) {
		int[] rowA = matrix[rowNoA];
		int[] rowB = matrix[rowNoB];
		matrix[rowNoA] = rowB;
		matrix[rowNoB] = rowA;
	}

	/**
	 * Verknüpfts Reihe A mit Reihe B mit einem XOR Row A ist oben, Row B wird
	 * verundet ^ = XOR
	 * 
	 * @param matrix
	 * @param rowNoA
	 * @param rowNoB
	 */
	private static void xorRow(int[][] matrix, int rowNoA, int rowNoB) {
		int[] rowA = matrix[rowNoA];
		for (int i = 0; i < matrix[0].length; i++) {
			matrix[rowNoB][i] = matrix[rowNoB][i] ^ rowA[i];
		}
	}

	/**
	 * Gibt alle speziellen Lösungen einer Matrix in erweiterter Zeilenstufenform
	 * zurück
	 * 
	 * @param matrix - Matrix in erweiterter Zeilenstufenform
	 * @return Alle speziellen Lösungen
	 */
	public static int[][] specialSolutions(int[][] matrix) {
		// Feste Variablen finden
		HashSet<Integer> fixedVars = new HashSet<Integer>();
		HashSet<Integer> openVars = new HashSet<Integer>();
		for (int i = 0; i < getM(matrix); i++) {
			int pivotIndex = getPivotIndex(matrix[i]);
			// Wenn kein Pivot-Element in der Reihe vorkommt, sind bereits alle gefunden
			if (pivotIndex != -1) {
				fixedVars.add(pivotIndex);
			} else {
				break;
			}
		}

		// Freie Variablen ausrechnen (Alle Variablen - Feste Variablen = Freie
		// Variablen)
		for (int i = 0; i < getN(matrix); i++) {
			openVars.add(i);
		}
		openVars.removeAll(fixedVars);

		// Debug Anfang
		if (debug) {
			System.out.println("Fixed variables:");
			System.out.print("[ ");
			for (Integer iterator : fixedVars) {
				System.out.print(iterator + " ");
			}
			System.out.println("]");
			System.out.println("Open variables:");
			System.out.print("[ ");
			for (Integer iterator : openVars) {
				System.out.print(iterator + " ");
			}
			System.out.println("]");
		}
		// Debug Ende

		// Eine freie Variable auf 0, alle anderen auf 1
		// Diesen Schritt für jede freie Variable wiederholen
		// Jeder Schritt ergibt einen speziellen Lösungsvektor

		HashSet<int[]> specialSolutions = new HashSet<int[]>();
		for (Integer openVarIndex : openVars) {
			// Kopie von erweiterter Zeilenstufenform anlegen
			int[][] copy = new int[getM(matrix)][];
			for (int i = 0; i < matrix.length; i++) {
				copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
			}
			setOtherVarsToZero(copy, openVarIndex);
			int[] special = solveMatrix(copy, openVarIndex);
			specialSolutions.add(special);
		}

		// Debug Anfang
		if (debug) {
			System.out.println("Special solutions: ");
			for (int[] iterator : specialSolutions) {
				System.out.print("( ");
				for (int i = 0; i < iterator.length; i++) {
					System.out.print(iterator[i] + " ");
				}
				System.out.println(")");
			}
		}
		// Debug Ende
		return matrix;
	}

	/**
	 * Löst Matrix und gibt eine Spezielle Lösung zurück.
	 * 
	 * @param matrix  - Matrix in erweiterter Zeilenstufenform
	 * @param openVar - Freie Variable welche auf 1 gesetzt wurde
	 * @return Speziellen Lösungsvektor
	 */
	private static int[] solveMatrix(int[][] matrix, int openVar) {
		int[] solution = new int[numberOfVars * numberOfVars];
		int pivotIndex = 0;
		for (int i = 0; i < getM(matrix); i++) {
			int rowSolution = 0;
			pivotIndex = getPivotIndex(matrix[i]);
			if (pivotIndex != -1) {
				for (int j = 0; j < getN(matrix); j++) {
					rowSolution += matrix[i][j];
				}
				solution[pivotIndex] = ((rowSolution + 1) % 2);
			} else {
				break;
			}
		}
		// Die freie Variable die auf 1 gesetzt ist
		solution[openVar] = 1;
		return solution;
	}

	/**
	 * Setzt alle freien Variablen auf 0, außer die per varIndex übergebene
	 * 
	 * @param matrix   - Matrix in erweiterter Zeilenstufenform
	 * @param varIndex - Freie Variable welche nicht genullt werden soll
	 */
	private static void setOtherVarsToZero(int[][] matrix, int varIndex) {
		int pivotIndex = 0;
		for (int i = 0; i < getM(matrix); i++) {
			pivotIndex = getPivotIndex(matrix[i]);
			for (int j = pivotIndex + 1; j < getN(matrix); j++) {
				if (j == varIndex) {
					continue;
				} else {
					matrix[i][j] = 0;
				}
			}
		}
	}

	/**
	 * Sucht Pivot-Element in einer Reihe und gibt den Index (effektiv die Variable)
	 * zurück
	 * 
	 * @param row - Reihe in der das Pivot-Element gesucht werden soll
	 * @return Den Index des Pivot-Elements oder -1 wenn kein Pivot-Element
	 *         existiert
	 */
	// Gibt -1 zurück wenn die Reihe kein Pivot-Element enthält
	private static int getPivotIndex(int[] row) {
		for (int i = 0; i < row.length; i++) {
			if (row[i] == 1) {
				return i;
			}
		}
		return -1;
	}
}
