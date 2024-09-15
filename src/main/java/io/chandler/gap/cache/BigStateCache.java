package io.chandler.gap.cache;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.math.BigInteger;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;

public class BigStateCache extends AbstractSet<State> {
    private final ObjectOpenHashSet<BA> map;



    final long elementsToStore;
    final int nElements;

    public BigStateCache(long elementsToStore, int nElements) {
        this.map = new ObjectOpenHashSet<>();
        this.elementsToStore = elementsToStore;
        this.nElements = nElements;
    }

    BA cvt(int[] state) {
        BigInteger value = BigInteger.ZERO;
        BigInteger x = BigInteger.valueOf(nElements + 1);
        for (int i = 0; i < elementsToStore; i++) {
            value = value.multiply(x);
            value = value.add(BigInteger.valueOf(state[i]));
        }
        return new BA(value.toByteArray());
    }

    @Override
    public boolean add(State state) {
        int[] s = state.state();

        return map.add(cvt(s));
    }

    @Override
    public boolean contains(Object o) {
        return map.contains(cvt(((State)o).state()));
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(cvt(((State)o).state()));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

	@Override
    public Iterator<State> iterator() {
        // Since the full state is not stored we can't retrieve the original states
        throw new UnsupportedOperationException("Not implemented");
    }

    public static class BA {
        public final byte[] value;

        public BA(byte[] value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof BA) {
                return Arrays.equals(value, ((BA)o).value);
            }
            return false;
        }
    }
}
