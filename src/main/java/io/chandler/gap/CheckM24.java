package io.chandler.gap;

import io.chandler.gap.GroupExplorer.MemorySettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckM24 {
	public static void mainX(String[] args) {
		// [(2,16,3)(8,22,9)(14,12,13)(18,20,17)(21,6,19)(24,15,23)]
		// [(8,22,9)(19,18,20)(1,2,3)(4,5,6)(10,11,12)(13,14,15)]
		// [(5,19,6)(8,22,9)(1,2,3)(10,11,12)(13,14,15)(16,17,18)]

		String genCandidate = "[(2,16,3)(4,1,5)(13,17,15)(20,18,19)(23,21,22)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]";
		// Sanity check
		// genCandidate = "[(1,4)(2,7)(3,17)(5,13)(6,9)(8,15)(10,19)(11,18)(12,21)(14,16)(20,24)(22,23),(1,4,6)(2,21,14)(3,9,15)(5,18,10)(13,17,16)(19,24,23)]";
		GroupExplorer ge_m24 = new GroupExplorer(genCandidate, MemorySettings.FASTEST, new M24StateCache());
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


	public static void main(String[] args) {

		int[][] genCycles = GroupExplorer.parseOperations("(2,16,3)(4,1,5)(13,17,15)(20,18,19)(23,21,22)(9,8,7)").get(0);
		checkSymmetricalCopies(genCycles);
	}

	public static void checkSymmetricalCopies(int[][] genCycles) {
		int faceIndex = 2;
		String face1Symm = GroupExplorer.generatorsToString(new int[][][] {
			GroupExplorer.parseOperations(CubicGenerators.cubicPISymmetries3).get(faceIndex)});

		GroupExplorer ge_face1 = new GroupExplorer(face1Symm, MemorySettings.DEFAULT, new HashSet<>());
		ge_face1.resetElements(true);
		ge_face1.applyOperation(0);
		ge_face1.applyOperation(0); // 180 deg
		System.out.println(ge_face1.order());
		int[] turn180 = ge_face1.copyCurrentState();

		int[][] symm = GroupExplorer.stateToCycles(turn180);

		boolean[][] fixed = new boolean[][] {
			{true, true, true, true, true, true, true, true, true, true, true, true},
			{true, false, false, false, false, false}
		};

		List<String> results = Collections.synchronizedList(new ArrayList<>());

		AtomicInteger didNotConverge = new AtomicInteger(0);
		AtomicInteger small = new AtomicInteger(0);

		CycleInverter.generateInvertedCycles(fixed, new int[][][]{symm, genCycles}).parallelStream().forEach(genSrc -> {

			String genStr = GroupExplorer.generatorsToString(genSrc);
			GroupExplorer ge_m24 = new GroupExplorer(
				genStr,
				MemorySettings.DEFAULT,
				new ParityStateCache(new M24StateCache()));

			int iters;
			try {
				iters = ge_m24.exploreStates(false, 700_000*2, null);
			} catch (ParityStateCache.StateRejectedException e) {
				//System.out.println("State rejected");
				iters = -2;
				didNotConverge.incrementAndGet();
			}
			if (iters == -1) {
				results.add(genStr);
			} else if (iters > 0) {
				small.incrementAndGet();
			}
		});

		for (String s : results) {
			//System.out.println(s);

			PentagonalIcositrahedron.printVertexGeneratorNotations(new int[][][]{GroupExplorer.parseOperationsArr(s)[1]});
		}


		System.out.println("Results: " + results.size());
		System.out.println("Did not converge: " + didNotConverge.get());
		System.out.println("Small: " + small.get());
		System.exit(0);

		String genCandidate = "[(2,16,3)(4,1,5)(13,17,15)(20,18,19)(23,21,22)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]";
		// Sanity check
		// genCandidate = "[(1,4)(2,7)(3,17)(5,13)(6,9)(8,15)(10,19)(11,18)(12,21)(14,16)(20,24)(22,23),(1,4,6)(2,21,14)(3,9,15)(5,18,10)(13,17,16)(19,24,23)]";
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
