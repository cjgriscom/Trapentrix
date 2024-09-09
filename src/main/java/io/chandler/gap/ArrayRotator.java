package io.chandler.gap;

import java.util.function.Consumer;
import java.util.function.Function;

public class ArrayRotator {
    public static void rotateSubArrays(int[][] array, Consumer<int[][]> rotatedConsumer) {
        rotateSubArrays(array, (x) -> {
            rotatedConsumer.accept(x);
            return true;
        });
    }

    /**
     * Rotates the subarrays of the given array and calls the rotatedConsumer with the rotated array.
     * @param array
     * @param rotatedConsumer return true to continue, false to stop
     */
    public static void rotateSubArrays(int[][] array, Function<int[][], Boolean> rotatedConsumer) {
        int[] rotationCounts = new int[array.length];
        int[] maxRotations = new int[array.length];
        
        for (int i = 0; i < array.length; i++) {
            maxRotations[i] = array[i].length;
        }

        // Apply the consumer to the current combination, break if the consumer returns false
        while (rotatedConsumer.apply(deepCopy(array))) {

            // Find the next subarray to rotate
            int indexToRotate = findNextIndexToRotate(rotationCounts, maxRotations);
            if (indexToRotate == -1) {
                break; // All combinations have been processed
            }

            // Rotate the subarray and update rotation count
            rotateRight(array[indexToRotate]);
            rotationCounts[indexToRotate]++;

            // Reset rotation counts for all subsequent subarrays
            for (int i = indexToRotate + 1; i < rotationCounts.length; i++) {
                while (rotationCounts[i] > 0) {
                    rotateRight(array[i]);
                    rotationCounts[i]--;
                }
            }
        }
    }

    private static int findNextIndexToRotate(int[] rotationCounts, int[] maxRotations) {
        for (int i = rotationCounts.length - 1; i >= 0; i--) {
            if (rotationCounts[i] < maxRotations[i] - 1) {
                return i;
            }
        }
        return -1;
    }

    public static void rotateRight(int[] array) {
        int last = array[array.length - 1];
        System.arraycopy(array, 0, array, 1, array.length - 1);
        array[0] = last;
    }

    public static void rotateLeft(int[] array) {
        int first = array[0];
        System.arraycopy(array, 1, array, 0, array.length - 1);
        array[array.length - 1] = first;
    }

    private static int[][] deepCopy(int[][] array) {
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i].clone();
        }
        return copy;
    }
}