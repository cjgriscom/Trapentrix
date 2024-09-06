package io.chandler.gap;

import java.util.function.Consumer;

public class ArrayRotatorTest {

    public static void main(String[] args) {
        int[][] array = {
            {1, 2, 3},
            {1, 2, 3, 4}
        };

        Consumer<int[][]> printConsumer = arr -> System.out.println(java.util.Arrays.deepToString(arr));
        ArrayRotator.rotateSubArrays(array, printConsumer);
    }
}