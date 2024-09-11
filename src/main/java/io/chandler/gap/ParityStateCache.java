package io.chandler.gap;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class ParityStateCache extends AbstractSet<State> {
    private final Set<State> map;
    private final Set<State> mapSupplied;

    public ParityStateCache(Set<State> mapSupplied) {
        this.map = new ObjectOpenHashSet<State>();
        this.mapSupplied = mapSupplied;
    }

    @Override
    public boolean add(State state) {
        boolean r = map.add(state);
        boolean r2 = mapSupplied.add(state);
        if (r != r2) throw new StateRejectedException();
        return r;
    }

    @Override
    public boolean contains(Object o) {
        boolean r = map.contains((State)o);
        boolean r2 = mapSupplied.contains((State)o);
        if (r != r2) throw new StateRejectedException();
        return r;
    }

    @Override
    public int size() {
        return map.size();
    }

	@Override
    public Iterator<State> iterator() {
        return map.iterator();
    }


    public static class StateRejectedException extends RuntimeException {
        public StateRejectedException() {
            super();
        }
    }
}
