package io.chandler.trapentrix;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class FilterPentagonalM11 {
    public static void main(String[] args) throws Exception {
        File file = new File("Generators_Pent_3x3_11.txt");
        Scanner scanner = new Scanner(file);
        
		int qualified = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            List<int[][]> operations = GAP.parseOperations(line);
            int[][] op2 = operations.get(2);
			boolean disqualify = false;
			for (int[] cycle : op2) {
				boolean hasTop = false;
				boolean hasBottom = false;
				boolean hasA = false;
				boolean hasB = false;
				for (int i : cycle) {
					if (i == 11) {
						hasA = true;
					} else if (i == 12) {
						hasB = true;
					} else if (i <= 5) {
						hasTop = true;
					} else {
						hasBottom = true;
					}
				}
				if ((hasA || hasB) && hasTop && hasBottom) {
					disqualify = true;
					break;
				}
				if (hasA && hasB) {
					disqualify = true;
					break;
				}
				if (!(hasA || hasB) && (hasTop != hasBottom)) {
					disqualify = true;
					break;
				}
				if (hasTop && hasBottom) {
					// Check for adjacency
					boolean hasAdjacent = false;
					for (int i = 0; i < cycle.length; i++) {
						int current = cycle[i];
						int next = cycle[(i + 1) % cycle.length];
						if (Math.abs(current - next) == 1 || (Math.min(current, next) == 6 && Math.max(current, next) == 10) || (Math.min(current, next) == 1 && Math.max(current, next) == 5)) {
							hasAdjacent = true;
							break;
						}
					}
					if (!hasAdjacent) {
						disqualify = true;
						break;
					}
				}
			}

			if (!disqualify) {
				qualified++;
				System.out.println(line);
			}
        }

		System.out.println(qualified);
        
        scanner.close(); // Don't forget to close the scanner when done
    }
}