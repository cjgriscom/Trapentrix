package io.chandler.gap;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.AbstractSet;
import java.util.Iterator;

public class M24StateCache extends AbstractSet<State> {
    private final LongOpenHashSet map;

    public M24StateCache() {
        this.map = new LongOpenHashSet();
    }

    /**
     * This is non-optimal but it allows the M24 cache to fit in my 32gb of RAM
     * 
     * Use the assumption that we don't need to store the entire state
     *   for comparisons, just the first few positions (i.e. what fits into 8 bytes)
     * @param state
     * @return
     */
    long cvt(int[] state) {
        long value = 0;
        int x = 25;
        for (int i = 0; i < 13; i++) {
            value = value * x + (state[i]);
            //x--;
        }
        return value;
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
    public int size() {
        return map.size();
    }

	@Override
    public Iterator<State> iterator() {
        // Since the full state is not stored we can't retrieve the original states
        throw new UnsupportedOperationException("Not implemented");
    }

}
