package io.chandler.gap;

import java.math.BigInteger;
import java.util.Arrays;

public abstract class State {
	public static State of(int[] state, int maxElement) {
		if (maxElement <= 31) {
			return new StateFactorial(state, maxElement);
		} else if (maxElement <= 255) {
			return new StateByte(state, maxElement);
		} else {
			throw new IllegalArgumentException("Max element is greater than 255");
		}
	}
	
	public abstract int[] state();


	static class StateFactorial extends State {
        private final byte[] factorialRepresentation;
        private final short length;
        private StateFactorial(int[] state, int maxElement) {
            this.length = (short)state.length;
            this.factorialRepresentation = encodeFactorial(state).toByteArray();
        }

		private BigInteger encodeFactorial(int[] state) {
			BigInteger result = BigInteger.ZERO;
			boolean[] used = new boolean[state.length + 1];
			
			for (int i = 0; i < state.length; i++) {
				int smaller = 0;
				for (int j = 1; j < state[i]; j++) {
					if (!used[j]) {
						smaller++;
					}
				}
				result = result.add(BigInteger.valueOf(smaller).multiply(factorial(state.length - 1 - i)));
				used[state[i]] = true;
			}
			return result;
		}
		private static final ThreadLocal<BigInteger[]> FACTORIAL_CACHE = ThreadLocal.withInitial(() -> new BigInteger[1000]);

		private BigInteger factorial(int n) {
			if (n < 0) {
				throw new IllegalArgumentException("Factorial is not defined for negative numbers");
			}
			if (n < 1000) {
				BigInteger[] cache = FACTORIAL_CACHE.get();
				if (cache[n] == null) {
					cache[n] = calculateFactorial(n);
				}
				return cache[n];
			}
			return calculateFactorial(n);
		}
	
		private BigInteger calculateFactorial(int n) {
			BigInteger result = BigInteger.ONE;
			for (int i = 2; i <= n; i++) {
				result = result.multiply(BigInteger.valueOf(i));
			}
			return result;
		}
	
		@Override
		public int[] state() {
			return decodeFactorial(new BigInteger(factorialRepresentation), length);
		}
	
		private int[] decodeFactorial(BigInteger encoded, int length) {
			int[] result = new int[length];
			boolean[] used = new boolean[length + 1];
			
			for (int i = 0; i < length; i++) {
				BigInteger f = factorial(length - 1 - i);
				int count = encoded.divide(f).intValue();
				encoded = encoded.mod(f);
				
				for (int j = 1; j <= length; j++) {
					if (!used[j]) {
						if (count == 0) {
							result[i] = j;
							used[j] = true;
							break;
						}
						count--;
					}
				}
			}
			return result;
		}

        @Override
        public int hashCode() {
            return Arrays.hashCode(factorialRepresentation);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || !(obj instanceof StateFactorial)) return false;
            StateFactorial other = (StateFactorial) obj;
            return Arrays.equals(factorialRepresentation, other.factorialRepresentation);
        }
    }

	static class StateByte extends State {
		byte[] stateB;

		private StateByte(byte[] state, int maxElement) {
			this.stateB = state;
		}
		private StateByte(int[] state, int maxElement) {
			this.stateB = new byte[state.length];
			for (int i = 0; i < state.length; i++) {
				this.stateB[i] = (byte) state[i];
			}
		}
		@Override
		public int[] state() {
			int[] state = new int[stateB.length];
			for (int i = 0; i < stateB.length; i++) {
				state[i] = stateB[i] & 0xff;
			}
			return state;
		}
		public int hashCode() {
			return Arrays.hashCode(stateB);
		}
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || !(obj instanceof State)) return false;
			StateByte state1 = (StateByte) obj;
			return Arrays.equals(stateB, state1.stateB);
		}
	}

	static class StateCompact extends State {
        private static final int BITS_PER_ELEMENT = 5;
        private static final int ELEMENTS_PER_INT = 32 / BITS_PER_ELEMENT;
        private static final int ELEMENT_MASK = (1 << BITS_PER_ELEMENT) - 1;

        private int[] compactState;
		private final int length;

        private StateCompact(int[] state, int maxElement) {
            if (maxElement > 31) {
                throw new IllegalArgumentException("Max element is greater than 31");
            }
			this.length = state.length;
            this.compactState = new int[(state.length + ELEMENTS_PER_INT - 1) / ELEMENTS_PER_INT];
            for (int i = 0; i < state.length; i++) {
                setElement(i, state[i]);
            }
        }

        @Override
        public int[] state() {
            int[] result = new int[length];
            for (int i = 0; i < length; i++) {
                result[i] = getElement(i);
            }
            return result;
        }

        private void setElement(int index, int value) {
            int arrayIndex = index / ELEMENTS_PER_INT;
            int bitOffset = (index % ELEMENTS_PER_INT) * BITS_PER_ELEMENT;
            compactState[arrayIndex] &= ~(ELEMENT_MASK << bitOffset);
            compactState[arrayIndex] |= (value & ELEMENT_MASK) << bitOffset;
        }

        private int getElement(int index) {
            int arrayIndex = index / ELEMENTS_PER_INT;
            int bitOffset = (index % ELEMENTS_PER_INT) * BITS_PER_ELEMENT;
            return (compactState[arrayIndex] >> bitOffset) & ELEMENT_MASK;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(compactState);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || !(obj instanceof StateCompact)) return false;
            StateCompact other = (StateCompact) obj;
            return Arrays.equals(compactState, other.compactState);
        }

		public static void main(String[] args) {
			testStateFactorial();
		}
	
		private static void testStateFactorial() {
			int[][] testCases = {
				{1, 2, 3},
				{3, 2, 1},
				{2, 1, 3},
				{1, 2, 3, 4, 5},
				{5, 4, 3, 2, 1},
				{2, 4, 1, 5, 3}
			};
	
			for (int[] testCase : testCases) {
				System.out.println("Testing: " + Arrays.toString(testCase));
				StateFactorial state = new StateFactorial(testCase, testCase.length);
				BigInteger encoded = new BigInteger(state.factorialRepresentation);
				int[] decoded = state.state();
				System.out.println("Encoded: " + encoded);
				System.out.println("Decoded: " + Arrays.toString(decoded));
				System.out.println("Match: " + Arrays.equals(testCase, decoded));
				System.out.println();
			}
		}
    }

}
