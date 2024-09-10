package io.chandler.gap;

import java.util.function.Consumer;

public class PermuCallback {


    /**
     * Generates all combinations of choosing K elements from a set of N elements,
     * calling the provided callback for each combination.
     *
     * @param N the total number of elements
     * @param K the number of elements to choose
     * @param callback the Consumer to be called for each combination
     */
    public static void generateCombinations(int N, int K, Consumer<int[]> callback) {
        int[] combination = new int[K]; // This array will hold the current combination
        // Start the recursive process
        generateCombinationsHelper(combination, 0, N, 0, callback);
    }

    /**
     * Helper method to generate combinations recursively.
     *
     * @param combination the current combination being built
     * @param start the next index to consider for inclusion in the combination
     * @param N the total number of elements
     * @param index the current index in the combination array
     * @param callback the Consumer to be called for each combination
     */
    private static void generateCombinationsHelper(int[] combination, int start, int N, int index, Consumer<int[]> callback) {
        if (index == combination.length) {
            callback.accept(combination.clone()); // Call the callback with a copy of the combination
            return;
        }

        for (int i = start; i < N; i++) {
            combination[index] = i;
            generateCombinationsHelper(combination, i + 1, N, index + 1, callback);
        }
    }
}
