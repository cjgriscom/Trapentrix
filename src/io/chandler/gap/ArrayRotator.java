package io.chandler.trapentrix;

import java.util.function.Consumer;

public class ArrayRotator {

    /**
     * Rotates the elements of each subarray in every possible combination and applies a consumer to each combination.
     *
     * @param array the original array of subarrays
     * @param rotatedConsumer the consumer to apply to each rotated combination
     */
    public static void rotateSubArrays(int[][] array, Consumer<int[][]> rotatedConsumer) {
        rotateSubArraysHelper(array, 0, rotatedConsumer);
    }

    /**
     * Helper method to recursively rotate subarrays.
     *
     * @param array the array of subarrays
     * @param index the current subarray index being processed
     * @param rotatedConsumer the consumer to apply to each rotated combination
     */
    private static void rotateSubArraysHelper(int[][] array, int index, Consumer<int[][]> rotatedConsumer) {
        if (index == array.length) {
            // Apply the consumer to the fully rotated combination
            rotatedConsumer.accept(deepCopy(array));
            return;
        }

        int length = array[index].length;
        for (int i = 0; i < length; i++) {
            // Rotate the current subarray
            rotateRight(array[index]);
            // Recurse to the next subarray
            rotateSubArraysHelper(array, index + 1, rotatedConsumer);
        }
        // Restore the original subarray after full rotation to prevent side effects
        rotateRight(array[index]);
    }

    /**
     * Rotates an array to the right by one position.
     *
     * @param array the array to rotate
     */
    private static void rotateRight(int[] array) {
        int last = array[array.length - 1];
        System.arraycopy(array, 0, array, 1, array.length - 1);
        array[0] = last;
    }

    /**
     * Creates a deep copy of a two-dimensional integer array.
     *
     * @param array the array to copy
     * @return a deep copy of the array
     */
    private static int[][] deepCopy(int[][] array) {
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i].clone();
        }
        return copy;
    }

    public static void main(String[] args) {
        int[][] array = {
            {1, 2, 3},
            {1, 2, 3, 4}
        };

        Consumer<int[][]> printConsumer = arr -> System.out.println(java.util.Arrays.deepToString(arr));
        rotateSubArrays(array, printConsumer);
    }
}