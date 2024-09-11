package io.chandler.gap;

import io.chandler.gap.GroupExplorer.MemorySettings;

import static io.chandler.gap.CubicGenerators.cubicPISymmetries;

import java.util.HashSet;
import java.util.Set;

public class CheckM24 {
	public static void main(String[] args) {
		// [(2,16,3)(8,22,9)(14,12,13)(18,20,17)(21,6,19)(24,15,23)]
		// [(8,22,9)(19,18,20)(1,2,3)(4,5,6)(10,11,12)(13,14,15)]
		// [(5,19,6)(8,22,9)(1,2,3)(10,11,12)(13,14,15)(16,17,18)]

		String genCandidate = "[(1,4,7,10)(2,5,8,11)(3,6,9,12)(17,20,23,15)(13,18,21,24)(16,19,22,14),(2,16,3)(8,4,7)(14,12,13)(17,20,18)(19,6,21)(24,23,22)]";
		// Sanity check
		genCandidate = "[(1,4)(2,7)(3,17)(5,13)(6,9)(8,15)(10,19)(11,18)(12,21)(14,16)(20,24)(22,23),(1,4,6)(2,21,14)(3,9,15)(5,18,10)(13,17,16)(19,24,23)]";
		GroupExplorer ge_m24 = new GroupExplorer(genCandidate, MemorySettings.DEFAULT, new M24StateCache());
		GroupExplorer ge_ord = new GroupExplorer(genCandidate, MemorySettings.COMPACT, new HashSet<>());
		ge_m24.initIterativeExploration();
		ge_ord.initIterativeExploration();
		int[] stateCount = new int[2];
		int ret = -2;
		while (ret == -2) {
			ret = ge_m24.iterateExploration(true, -1, (states, depth) -> {
				stateCount[0] += states.size();
			});
			if (stateCount[0] <= 36_000_000) {
				ge_ord.iterateExploration(false, -1, (states, depth) -> {
					stateCount[1] += states.size();
				});
				if (stateCount[0] != stateCount[1]) {
					System.out.println("State count mismatch: " + stateCount[0] + " != " + stateCount[1]);
					break;
				}
			} else if (ge_ord != null) {
				System.out.println("Switching to M24 cache");
				ge_ord = null;
			}
		}
		
	}
}
