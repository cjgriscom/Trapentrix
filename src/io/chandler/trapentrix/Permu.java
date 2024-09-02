package io.chandler.trapentrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Permu {
    public static void main(String[] args) {
        int[][] operation = new int[][] {
            new int[] {1, 2, 3},
            new int[] {4, 5, 6},
            new int[] {7, 8, 9}
        };
        int[][] operation2 = new int[][] {
            new int[] {1, 2, 3, 4},
            new int[] {4, 2, 3, 1},
        };
        int[][][] generator = new int[][][] {
            operation,
            operation2
        };
        List<int[][][]> isomorphs = applyGeneratorPermutationsAndRotations(generator);
        for (int[][][] isomorph : isomorphs) {
            System.out.println(GAP.generatorsToString(GAP.renumberGenerators(isomorph)));
        }
    }

    public static List<int[][][]> applyGeneratorPermutationsAndRotations(int[][][] a) {
        List<int[][][]> result = new ArrayList<>();
        List<List<int[][]>> statePermutations = new ArrayList<>();
    
        // Apply permutations and rotations to each state
        for (int[][] state : a) {
            statePermutations.add(applyStatePermutationsAndRotations(state));
        }
    
        // Generate all combinations of permuted states
        generateCombinations(statePermutations, 0, new int[a.length][][], result);
    
        return result;
    }
    
    private static void generateCombinations(List<List<int[][]>> statePermutations, int index, int[][][] current, List<int[][][]> result) {
        if (index == statePermutations.size()) {
            result.add(current.clone());
            return;
        }
    
        for (int[][] permutedState : statePermutations.get(index)) {
            current[index] = permutedState;
            generateCombinations(statePermutations, index + 1, current, result);
        }
    }

    public static List<int[][]> applyStatePermutationsAndRotations(int[][] a) {
        
        ArrayList<int[][]> result = new ArrayList<>();
        List<int[]> permutationsA = Permu.generatePermutations(a.length);
        
        int cycleOrderA = a[0].length;

        int aReps = cycleOrderA % 2 == 0 ? 2 : cycleOrderA;

        int permsAndRepetitionsA = permutationsA.size() * aReps;
        for (int i = 0; i < permsAndRepetitionsA; i++) {
            int permutationIndex = i % permutationsA.size();
            int[] permutationA = permutationsA.get(permutationIndex);
            int repA = i / permutationsA.size() + 1;
            if (cycleOrderA % 2 == 0) repA = cycleOrderA;
            
            int[][] aRep = GAP.repeatOperation(a, repA);
            int[][] aModified = new int[a.length][cycleOrderA];
            for (int j = 0; j < a.length; j++) {
                int[] cycleSrc = aRep[permutationA[j] - 1];
                aModified[j] = cycleSrc;
            }
            result.add(aModified);
        }
        return result;
    }

    
	public static List<int[]> generatePermutations(int n) {
        List<int[]> permutations = new ArrayList<>();
        int[] initial = new int[n];
        for (int i = 0; i < n; i++) {
            initial[i] = i + 1;
        }
        generatePermutationsHelper(initial, 0, permutations);
        return permutations;
    }

    private static void generatePermutationsHelper(int[] arr, int start, List<int[]> permutations) {
        if (start == arr.length - 1) {
            permutations.add(arr.clone());
            return;
        }

        for (int i = start; i < arr.length; i++) {
            swap(arr, start, i);
            generatePermutationsHelper(arr, start + 1, permutations);
            swap(arr, start, i); // backtrack
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
