package io.chandler.gap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ArrayRotatorTest {

    @Test
    public void testRotateSubArrays() {
        int[][] array = {
            {1, 2, 3},{4, 5, 6},{7, 8, 9},{10, 11, 12},{10, 2, 4},
        };

        List<String> recursiveResults = new ArrayList<>();
        List<String> iterativeResults = new ArrayList<>();

        Consumer<int[][]> recursiveConsumer = arr -> recursiveResults.add(Arrays.deepToString(arr));
        Consumer<int[][]> iterativeConsumer = arr -> iterativeResults.add(Arrays.deepToString(arr));

        ArrayRotatorRecursive.rotateSubArrays(array, recursiveConsumer);
        ArrayRotator.rotateSubArrays(array, iterativeConsumer);

        assertEquals(recursiveResults.size(), iterativeResults.size(), "Both methods should produce the same number of combinations");

        for (String result : recursiveResults) {
            assertTrue(iterativeResults.contains(result), "Iterative method should contain all results from recursive method");
        }

        for (String result : iterativeResults) {
            assertTrue(recursiveResults.contains(result), "Recursive method should contain all results from iterative method");
        }
    }

    @Test
    public void testRotateSubArraysWithEmptyArray() {
        int[][] emptyArray = {};

        List<String> recursiveResults = new ArrayList<>();
        List<String> iterativeResults = new ArrayList<>();

        Consumer<int[][]> recursiveConsumer = arr -> recursiveResults.add(Arrays.deepToString(arr));
        Consumer<int[][]> iterativeConsumer = arr -> iterativeResults.add(Arrays.deepToString(arr));

        ArrayRotatorRecursive.rotateSubArrays(emptyArray, recursiveConsumer);
        ArrayRotator.rotateSubArrays(emptyArray, iterativeConsumer);

        assertEquals(1, recursiveResults.size(), "Empty array should produce one result");
        assertEquals(1, iterativeResults.size(), "Empty array should produce one result");
        assertEquals(recursiveResults.get(0), iterativeResults.get(0), "Both methods should produce the same result for empty array");
    }

    @Test
    public void testRotateSubArraysWithSingleElementArrays() {
        int[][] singleElementArrays = {{1}, {2}, {3}};

        List<String> recursiveResults = new ArrayList<>();
        List<String> iterativeResults = new ArrayList<>();

        Consumer<int[][]> recursiveConsumer = arr -> recursiveResults.add(Arrays.deepToString(arr));
        Consumer<int[][]> iterativeConsumer = arr -> iterativeResults.add(Arrays.deepToString(arr));

        ArrayRotatorRecursive.rotateSubArrays(singleElementArrays, recursiveConsumer);
        ArrayRotator.rotateSubArrays(singleElementArrays, iterativeConsumer);

        assertEquals(1, recursiveResults.size(), "Single element arrays should produce one result");
        assertEquals(1, iterativeResults.size(), "Single element arrays should produce one result");
        assertEquals(recursiveResults.get(0), iterativeResults.get(0), "Both methods should produce the same result for single element arrays");
    }
}