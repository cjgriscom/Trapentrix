package io.chandler.gap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IsomorphicGeneratorTest {

	@Test
	public void testCheckIsomorphism() {
		int[][][] a = new int[][][] {
			{{1, 2, 4}, {3, 5, 6}, {7, 8, 9}},
			{{1, 5, 9}, {3, 11, 13}, {12, 4, 6}}
		};

		int[][][] b = new int[][][] {
			{{5, 9, 1}, {4, 11, 12}, {13, 3, 6}},
			{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}
		};

		IsomorphicGenerator genA = new IsomorphicGenerator(a);
		IsomorphicGenerator genB = new IsomorphicGenerator(b);
		assertTrue(genA.equals(genB));
	}

	@Test
	public void testCheckSelectivePRR() {
		int[][] aCycles, bCycles;

		aCycles = new int[][] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
		bCycles = new int[][] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
		assertTrue(IsomorphicGenerator.checkSelectivePRR(aCycles, bCycles));

		aCycles = new int[][] {{0, 0, 0}, {4, 5, 6}, {7, 8, 9}};
		bCycles = new int[][] {{0, 0, 0}, {4, 5, 6}, {7, 8, 9}};
		assertTrue(IsomorphicGenerator.checkSelectivePRR(aCycles, bCycles));

		aCycles = new int[][] {{0, 0, 0}, {4, 5, 6}, {7, 8, 9}};
		bCycles = new int[][] {{0, 0, 0}, {6, 5, 4}, {7, 8, 9}};
		assertFalse(IsomorphicGenerator.checkSelectivePRR(aCycles, bCycles));

		bCycles = new int[][] {{0, 0, 0}, {6, 5, 4}, {7, 9, 8}};
		aCycles = new int[][] {{0, 0, 0}, {4, 5, 6}, {7, 8, 9}};
		assertTrue(IsomorphicGenerator.checkSelectivePRR(aCycles, bCycles));

		bCycles = new int[][] {{7, 9, 8}, {0, 0, 0}, {6, 5, 4}};
		aCycles = new int[][] {{0, 0, 0}, {4, 5, 6}, {7, 8, 9}};
		assertTrue(IsomorphicGenerator.checkSelectivePRR(aCycles, bCycles));

		bCycles = new int[][] {{0, 0, 1}, {0, 2, 0}, {3, 0, 0}};
		aCycles = new int[][] {{0, 1, 0}, {0, 3, 0}, {0, 2, 0}};
		assertTrue(IsomorphicGenerator.checkSelectivePRR(aCycles, bCycles));

		bCycles = new int[][] {{2, 0, 1, 0}, {4, 0, 5, 0}, {0, 0, 0, 0}};
		aCycles = new int[][] {{0, 1, 0, 2}, {0, 5, 0, 4}, {0, 0, 0, 0}};
		assertTrue(IsomorphicGenerator.checkSelectivePRR(aCycles, bCycles));

		bCycles = new int[][] {{2, 1, 0, 0}, {4, 5, 0, 0}, {0, 0, 0, 0}};
		aCycles = new int[][] {{0, 1, 0, 2}, {0, 5, 0, 4}, {0, 0, 0, 0}};
		assertFalse(IsomorphicGenerator.checkSelectivePRR(aCycles, bCycles));
	}
}
