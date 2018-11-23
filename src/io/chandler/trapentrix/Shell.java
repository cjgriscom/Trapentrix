package io.chandler.trapentrix;

import java.util.LinkedList;
import java.util.Scanner;

public class Shell {
	static LinkedList<String> moveList = new LinkedList<>();
	static Trapentrix t = new Trapentrix();
	
	public static void main(String[] args) {
		
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		
		while (true) {
			System.out.print("[" + moveList.size() + "]> ");
			String cmd = in.nextLine().trim().toUpperCase();
			if (cmd.startsWith("G") || cmd.startsWith("N")) moveList.add(cmd);
			handle(cmd);
		}
	}
	
	public static void handle(String cmd) {
		if (cmd.contains(" ")) {
			String[] seq = cmd.split(" ");
			if (seq[0].startsWith("N")) {
				try{
					for (int i = 0; i < Integer.parseInt(seq[0].substring(1)); i++) {
						for (int j = 1; j < seq.length; j++) {
							handle(seq[j]);
						}
					}
				} catch (Exception e) {
					System.out.println("Error");
					return;
				}
			} else {
				for (String s : seq) handle(s);
			}
		} else switch(cmd) {
		// Print help
		case ("H"):
			System.out.println("Commands:");
			System.out.println("    h: Print help");
			System.out.println("    u: Undo last move");
			System.out.println("    s: Print puzzle state (named)");
			System.out.println("    p: Print puzzle state (colors)");
			System.out.println("    ?: Print puzzle state (what-changed)");
			System.out.println("    c: Print cycle state");
			System.out.println("    m: Print move list");
			System.out.println("    rrr: Reset");
			System.out.println("Puzzle Moves:");
			System.out.println(" * Accepts space-delimited sequences i.e. 'G1U G2D'");
			System.out.println(" * Accepts repeat directives i.e. 'N12 G1U G2D'");
			System.out.println("    G1U: Move grip 1 up");
			System.out.println("    G1D: Move grip 1 down");
			System.out.println("    G2U: Move grip 2 up");
			System.out.println("    G2D: Move grip 2 down");
			break;
		// Undo last move
		case ("U"):
			if (moveList.size() == 0) break;
			String rem = moveList.pop();
			String n = "N1";
			if (rem.startsWith("N")) {
				n = rem.substring(0, rem.indexOf(" "));
				rem = rem.substring(rem.indexOf(" ") + 1);
			}
			String newList = n;
			String[] oldList = rem.split(" ");
			for (int i = oldList.length - 1; i >= 0; i--) {
				String c = oldList[i];
				if (c.equals("G1U")) newList += " G1D";
				if (c.equals("G1D")) newList += " G1U";
				if (c.equals("G2U")) newList += " G2D";
				if (c.equals("G2D")) newList += " G2U";
			}
			handle(newList);
			
			break;
		// Print named states
		case ("S"): 
			System.out.print(t.toString());
			break;
		// Print color states
		case ("P"): 
			System.out.print(t.colorString());
			break;
		// Print changed pieces state
		case ("?"): 
			System.out.print(t.changedString());
			break;
		// Print cycle state list
		case ("C"): 
			System.out.print(t.stateAsCycleList());
			break;
		// Print move list
		case ("M"): 
			for (String m : moveList) System.out.print(m + " ");
			System.out.println();
			break;
		// Reset
		case ("RRR"): 
			System.out.println("Reset.");
			moveList.clear();
			t = new Trapentrix();
			break;
		// Moves
		case ("G1U"): t.move(Trapentrix.grip1Up); break;
		case ("G1D"): t.move(Trapentrix.grip1Down); break;
		case ("G2U"): t.move(Trapentrix.grip2Up); break;
		case ("G2D"): t.move(Trapentrix.grip2Down); break;
		default:
			System.out.println("Unknown command");
		}
	}
}
