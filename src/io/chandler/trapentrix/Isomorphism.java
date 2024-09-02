package io.chandler.trapentrix;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Isomorphism {
	public static boolean isIsomorphic(int[][][] a, int[][][] b) {
		if (a.length != b.length) return false;
		
		// Sort generators by number of cycles
		Arrays.sort(a, Comparator.comparingInt(gen -> gen.length));
		Arrays.sort(b, Comparator.comparingInt(gen -> gen.length));
		
		// Check if generator structures match
		for (int i = 0; i < a.length; i++) {
			if (a[i].length != b[i].length) return false;
		}
		
		// Try all possible mappings and cycle arrangements
		return tryMapping(a, b, new HashMap<>(), new HashSet<>(), 0);
	}

	private static boolean tryMapping(int[][][] a, int[][][] b, Map<Integer, Integer> mapping, Set<Integer> used, int genIndex) {
		if (genIndex == a.length) return true;
		
		int[][] genA = a[genIndex];
		int[][] genB = b[genIndex];
		
		// Try all permutations of cycles in genB
		return tryPermutations(genA, genB, new int[genB.length], new boolean[genB.length], 0, mapping, used, genIndex, a, b);
	}

	private static boolean tryPermutations(int[][] genA, int[][] genB, int[] perm, boolean[] used, int depth,
										Map<Integer, Integer> mapping, Set<Integer> usedElements, int genIndex, int[][][] a, int[][][] b) {
		if (depth == genB.length) {
			// Try mapping with this permutation
			return tryMappingWithPermutation(genA, genB, perm, mapping, usedElements, genIndex, a, b);
		}
		
		for (int i = 0; i < genB.length; i++) {
			if (!used[i] && genA[depth].length == genB[i].length) {
				used[i] = true;
				perm[depth] = i;
				if (tryPermutations(genA, genB, perm, used, depth + 1, mapping, usedElements, genIndex, a, b)) {
					return true;
				}
				used[i] = false;
			}
		}
		
		return false;
	}

	private static boolean tryMappingWithPermutation(int[][] genA, int[][] genB, int[] perm,
													Map<Integer, Integer> mapping, Set<Integer> used, int genIndex, int[][][] a, int[][][] b) {
		Map<Integer, Integer> newMapping = new HashMap<>(mapping);
		Set<Integer> newUsed = new HashSet<>(used);
		
		for (int i = 0; i < genA.length; i++) {
			int[] cycleA = genA[i];
			int[] cycleB = genB[perm[i]];
			
			if (!tryCycleMapping(cycleA, cycleB, newMapping, newUsed)) {
				return false;
			}
		}
		
		return tryMapping(a, b, newMapping, newUsed, genIndex + 1);
	}

	private static boolean tryCycleMapping(int[] cycleA, int[] cycleB, Map<Integer, Integer> mapping, Set<Integer> used) {
		// Try forward mapping
		if (tryOneCycleMapping(cycleA, cycleB, mapping, used, false)) {
			return true;
		}
		
		// Try reverse mapping
		return tryOneCycleMapping(cycleA, cycleB, mapping, used, true);
	}

	private static boolean tryOneCycleMapping(int[] cycleA, int[] cycleB, Map<Integer, Integer> mapping, Set<Integer> used, boolean reverse) {
		Map<Integer, Integer> tempMapping = new HashMap<>(mapping);
		Set<Integer> tempUsed = new HashSet<>(used);
		
		for (int i = 0; i < cycleA.length; i++) {
			int elementA = cycleA[i];
			int elementB = reverse ? cycleB[(cycleB.length - i) % cycleB.length] : cycleB[i];
			
			if (tempMapping.containsKey(elementA)) {
				if (tempMapping.get(elementA) != elementB) {
					return false;
				}
			} else if (tempUsed.contains(elementB)) {
				return false;
			} else {
				tempMapping.put(elementA, elementB);
				tempUsed.add(elementB);
			}
		}
		
		mapping.putAll(tempMapping);
		used.addAll(tempUsed);
		return true;
	}
}
