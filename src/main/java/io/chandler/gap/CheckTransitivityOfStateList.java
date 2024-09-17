package io.chandler.gap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class CheckTransitivityOfStateList {
	public static void main(String[] args) throws IOException {
		

		/*
		 * 869271: single 4-cycle, single 20-cycle
691031: single 5-cycle, single 19-cycle
682202: single 2-cycle, single 3-cycle, single 18-cycle
641381: single 6-cycle, single 18-cycle
		 */

		 Scanner in = new Scanner(new File("../GroupTxt/PI-Unity-Partial.txt"));

		 HashMap<Integer, Integer> elementsAffectedToClassOrderSum = new HashMap<>();
		 while (in.hasNextLine()) {
			String line = in.nextLine();
			String[] parts = line.split(":");
			int classOrder = Integer.parseInt(parts[0]);
			if (parts.length <= 1) continue;
			String[] cycles = parts[1].split(",");
			int elementsAffacted = 0;
			for (String cycle : cycles) {
				// Split on space or dash
				//System.out.println(cycle);
				String[] txt = cycle.trim().split(" |-");
				int cycleLength = Integer.parseInt(txt[1]);
				String multiplicityS = txt[0];
				int multiplicity;
				switch (multiplicityS) {
					case "single":
						multiplicity = 1;
						break;
					case "dual":
						multiplicity = 2;
						break;
					case "triple":
						multiplicity = 3;
						break;
					case "quadruple":
						multiplicity = 4;
						break;
					case "quintuple":
						multiplicity = 5;
						break;
					default:
						multiplicity = Integer.parseInt(multiplicityS.substring(0, multiplicityS.length() - 1));
				}

				elementsAffacted += cycleLength * multiplicity;
				
			}
			// Add the sum of elements affected to the class order
			elementsAffectedToClassOrderSum.put(elementsAffacted, elementsAffectedToClassOrderSum.getOrDefault(elementsAffacted, 0) + classOrder);
		 }

		 System.out.println(elementsAffectedToClassOrderSum);
	}
}
