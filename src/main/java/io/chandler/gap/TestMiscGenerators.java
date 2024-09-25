package io.chandler.gap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.chandler.gap.GroupExplorer.MemorySettings;

public class TestMiscGenerators {
	public static void main(String[] args) {
		/*
Deltoidal icositrahedron alternating 8-cycles around equator
7,5,20,19,2,4,24,23
6,8,21,22,3,1,17,18

5,6,9,10,1,2,13,14
8,7,16,15,4,3,12,11

15,13,19,17,10,12,22,24
14,16,23,21,11,9,18,20

Alternating 4-cycles around equator
(1,18,8,22)(2,24,7,20)(3,17,6,21)(4,23,5,19)
(1,13,5,9)(2,14,6,10)(3,11,7,15)(4,12,8,16)
(9,20,16,21)(10,22,15,19)(11,18,14,23)(12,24,13,17)

3-fold symm
4,15,24
2,16,22 13,23,3
20,8,10 5,11,17
6,9,18

1,10,17
2,12,18 19,3,9
8,14,24 21,5,15
7,16,23

// This was generated on PI 
         "[(4,8,6)(7,11,9)(19,20,21)(22,23,24),(5,19,6)(8,22,9)(20,23,21)]"
		 it has 13 elements and 13 cycles, triple 3 cycles
		 It's PSL(3,3)
		 */
		String deltIcoSymm = 
			"[(4,8,6)(7,11,9)(19,20,21)(22,23,24),(5,19,6)(8,22,9)(20,23,21)]";
		// [(1,2,3)(4,5,6)(7,8,9)(10,11,12),(13,7,3)(2,10,6)(8,11,9)]
		System.out.println(GroupExplorer.renumberGeneratorNotation(deltIcoSymm));

		// Lucky M22 on PI
		// [(3,5,1)(7,4,8)(12,14,11)(16,13,17)(24,9,22)(21,20,19),(2,16,3)(9,11,7)(14,12,13)(17,20,18)(22,21,23)(6,5,4)]
		// Lucky M23 on PI
		// [(6,8,4)(7,11,9)(10,2,12)(23,21,22)(13,14,15)(16,17,18),(2,16,3)(9,22,8)(11,14,12)(23,15,24)(4,5,6)(19,20,21)]
		
		GroupExplorer ge = new GroupExplorer(deltIcoSymm, MemorySettings.FASTEST);
		ge.initIterativeExploration();
		HashMap<String, Integer> counts = new HashMap<>();
		int iter = -1;
		for (int i = 0; i < 38 && iter < 0; i++) {
			iter = ge.iterateExploration(true, -1, (states, depth) -> {
				for (int[] state : states) {
					String desc = GroupExplorer.describeState(24, state);
					counts.put(desc, counts.getOrDefault(desc, 0) + 1);
					if (desc.equals("triple 3-cycles")) {
						int[][] cycles = GroupExplorer.stateToCycles(state);
						try {
							PentagonalIcositrahedron.printVertexGeneratorNotations(new int[][][]{cycles});
							
						} catch (Exception e) {}
					}
				}
			});
		}
		int sumCounts = counts.values().stream().mapToInt(Integer::intValue).sum();
		System.out.println(" iter " + iter + "   order " + ge.order() + "   sumCounts " + (sumCounts+1));
		// Sort by count
		List<Map.Entry<String, Integer>> sortedCounts = new ArrayList<>(counts.entrySet());
		sortedCounts.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
		for (Map.Entry<String, Integer> entry : sortedCounts) {
			System.out.println(entry.getValue() + " " + entry.getKey());
		}
	}
}
