package io.chandler.gap;

import java.util.function.Consumer;

public class ArrayRotator {

    public static void rotateSubArrays(int[][] array, Consumer<int[][]> rotatedConsumer) {
        int[] rotationCounts = new int[array.length];
        int[] maxRotations = new int[array.length];
        
        for (int i = 0; i < array.length; i++) {
            maxRotations[i] = array[i].length;
        }

        while (true) {
            // Apply the consumer to the current combination
            rotatedConsumer.accept(deepCopy(array));

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

    private static void rotateRight(int[] array) {
        int last = array[array.length - 1];
        System.arraycopy(array, 0, array, 1, array.length - 1);
        array[0] = last;
    }
    
    private static int[][] deepCopy(int[][] array) {
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i].clone();
        }
        return copy;
    }
}