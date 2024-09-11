package io.chandler.gap;

import io.chandler.gap.GroupExplorer.MemorySettings;

import static io.chandler.gap.CubicGenerators.cubicPISymmetries;

public class CheckM24 {
	public static void main(String[] args) {
		// [(2,16,3)(8,22,9)(14,12,13)(18,20,17)(21,6,19)(24,15,23)]
		// [(8,22,9)(19,18,20)(1,2,3)(4,5,6)(10,11,12)(13,14,15)]
		// [(5,19,6)(8,22,9)(1,2,3)(10,11,12)(13,14,15)(16,17,18)]


		String genCandidate = "[(2,16,3)(8,22,9)(14,12,13)(18,20,17)(21,6,19)(23,15,24)]";

		genCandidate = genCandidate.substring(0, genCandidate.length() - 1) + "," + cubicPISymmetries.substring(1);

		GroupExplorer ge = new GroupExplorer(genCandidate, MemorySettings.DEFAULT, new M24StateCache());
		IcosahedralGenerators.exploreGroup(ge, null);
	
	}
}
