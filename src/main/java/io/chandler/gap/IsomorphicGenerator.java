package io.chandler.gap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IsomorphicGenerator {

	private final int[][][] generatorOrig, generatorCache;

	public IsomorphicGenerator(int[][][] generator) {

		// TODO I'm sure this can be extended to larger generators
		//       but for now just consider pairs
		if (generator.length != 2) {
			throw new IllegalArgumentException("Generator must have 2 cycle groups");
		}

		this.generatorOrig = generator;
		this.generatorCache = GroupExplorer.renumberGenerators(generator);
		int nElementsFirstGroup = 0;
		for (int[] cycle : generatorCache[0]) {
			for (int element : cycle) {
				if (element > 0) {
					nElementsFirstGroup++;
				}
			}
		}
		zeroOutElementsNotInFirstCycleGroup(nElementsFirstGroup, generatorCache[1]);

		// TODO rearrange so that the first cycle group has the smallest
		//        permutation / rotation count
	}

	public int[][][] generator() {
		return generatorOrig;
	}

	public boolean equals(Object o) {
		if (o instanceof IsomorphicGenerator) {
			return checkIsomorphism(((IsomorphicGenerator) o).generatorOrig);
		}
		return false;
	}

	private boolean cycleStructureMatches(int[][][] bPerm) {
		for (int i = 0; i < generatorCache.length; i++) {
			if (generatorCache[i].length != bPerm[i].length) {
				return false;
			}
		}

		for (int j = 0; j < generatorCache[0].length; j++) {
			if (generatorCache[0][j].length != bPerm[0][j].length) {
				return false;
			}
		}

		return true;
	}

	private void zeroOutElementsNotInFirstCycleGroup(int nElementsFirstGroup, int[][] bRenumbered) {
		for (int j = 0; j < bRenumbered.length; j++) {
			for (int k = 0; k < bRenumbered[j].length; k++) {
				if (bRenumbered[j][k] > nElementsFirstGroup) {
					bRenumbered[j][k] = 0;
				}
			}
		}
	}

	private boolean checkIsomorphism(int[][][] b) {
		if (generatorCache.length != b.length) {
			throw new IllegalArgumentException("Generator must have 2 cycle groups");
		}

		// First, get all permutations of b's cycle groups
		List<int[]> generatorPermutations = Permu.generatePermutations(b.length);

		skipGeneratorPermutation:
		for (int[] generatorPermutation : generatorPermutations) {
			int[][][] bPerm = new int[b.length][][];
			for (int i = 0; i < b.length; i++) {
				bPerm[i] = b[generatorPermutation[i] - 1];
			}

			int tmp = 0;
			for (int[] cycle : bPerm[0]) {
				tmp += cycle.length;
			}
			final int nElementsFirstGroup = tmp;

			// Now we have bPerm - make sure the cycle structures are the same
			if (!cycleStructureMatches(bPerm)) continue skipGeneratorPermutation;
			
			

			// The cycle structure matches, now we need to fit the cycles to each
			//   other one at a time

			boolean[] matchFound = new boolean[1];

			int[][] aCycles1 = generatorCache[1];
			int[][] bCycles0 = bPerm[0]; // After renumbering this will match aCycles0
			int[][] bCycles1 = bPerm[1];

			List<int[][]> bCyclesList = Permu.applyStatePermutationsAndRepetitions(bCycles1);
			for (int[][] bCycles : bCyclesList) {
				ArrayRotator.rotateSubArrays(bCycles, (bCycles1Rotated) -> {

					int[][][] bRenumbered = GroupExplorer.renumberGenerators(
						new int[][][] { bCycles0, bCycles1Rotated });

					// Zero out numbers that aren't in the first cycle group
					for (int i = 1; i < bRenumbered.length; i++) {
						for (int j = 0; j < bRenumbered[i].length; j++) {
							for (int k = 0; k < bRenumbered[i][j].length; k++) {
								if (bRenumbered[i][j][k] > nElementsFirstGroup) {
									bRenumbered[i][j][k] = 0;
								}
							}
						}
					}

					// Now we need to check if the second cycle group can be
					// permuted/rotated/repeated to match, ignoring the zeroed out elements

					boolean match = checkSelectivePRR(aCycles1, bRenumbered[1]);
					if (match) {
						matchFound[0] = true;
						return false; // Stop rotations loop
					}
					return true; // Continue rotations loop
				});
				if (matchFound[0]) {
					return true;
				}
			}


		}
		return false;
		
	}

	public static boolean checkSelectivePRR(int[][] aCycles, int[][] bCycles) {
		int cycleLength = aCycles[0].length;

		List<int[][]> bCyclesRepetitions = Permu.applyStateRepetitions(bCycles);
		
		// This covers reversals and repetitions for odd cycles
		nextRep: for (int[][] bCycleRep : bCyclesRepetitions) {

			// These are the cycles that must be matched that we have to choose from
			ArrayList<int[]> bCycleList = new ArrayList<>();
			for (int[] bCycle : bCycleRep) bCycleList.add(bCycle);

			// Eliminate matches one at a time
			for (int[] aCycle : aCycles) {
				boolean foundMatch = false;

				// Check if we're matching an all-zero aCycle
				if (countZeroes(aCycle) == cycleLength) {
					// Then select any all-zero bCycle
					Iterator<int[]> bCycleIterator = bCycleList.iterator();
					while (bCycleIterator.hasNext()) {
						int[] bCycle = bCycleIterator.next();
						if (countZeroes(bCycle) == cycleLength) {
							bCycleIterator.remove();
							foundMatch = true;
							break;
						}
					}
				} else {
					// aCycle guarenteed to have at least one non-zero element

					// Find the first non-zero element in aCycle
					int ai;
					for (ai = 0; ai < cycleLength; ai++) {
						if (aCycle[ai] != 0) {
							break;
						}
					}

					Iterator<int[]> bCycleIterator = bCycleList.iterator();
					while (bCycleIterator.hasNext()) {
						int[] bCycle = bCycleIterator.next();
						int bi;
						boolean foundMatch0 = false;
						for (bi = 0; bi < cycleLength; bi++) {
							if (aCycle[ai] == bCycle[bi]) {
								foundMatch0 = true;
								break;
							}
						}
						boolean match = foundMatch0;
						if (foundMatch0) {
							for (int i = 0; i < cycleLength; i++) {
								if (	aCycle[(i + ai) % cycleLength] !=
										bCycle[(i + bi) % cycleLength]) {
									
									match = false;
									break;
								}
							}
						}
						if (match) {
							bCycleIterator.remove();
							foundMatch = true;
							break;
						}
					}
				}

				// Missed a match so move on
				if (!foundMatch) continue nextRep;
			}

			if (bCycleList.isEmpty()) return true; // Matched all cycles
		}
		return false;
	}

	private static int countZeroes(int[] array) {
		int count = 0;
		for (int i : array) {
			if (i == 0) {
				count++;
			}
		}
		return count;
	}
}
