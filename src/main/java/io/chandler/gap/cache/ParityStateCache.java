package io.chandler.gap.cache;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Used to check if a partial cache matches the full cache
 *   useful for eliminating non N-transitive groups
 * 
 * Note that GroupExplorer checks cache parity internally now,
 * so this probably isn't needed anymore
 */
public class ParityStateCache extends AbstractSet<State> {
    private final Set<State> map;
    private final Set<State> mapSupplied;

    public ParityStateCache(Set<State> mapPrimary, Set<State> mapChecks) {
        this.map = mapPrimary;
        this.mapSupplied = mapChecks;
    }

    public ParityStateCache(Set<State> mapSupplied) {
        this.map = new ObjectOpenHashSet<State>();
        this.mapSupplied = mapSupplied;
    }

    @Override
    public boolean add(State state) {
        boolean r = map.add(state);
        boolean r2 = mapSupplied.add(state);
        if (r != r2) throw new StateRejectedException("Added to primary: " + r + " and secondary: " + r2);
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
        int r = map.size();
        int r2 = mapSupplied.size();
        if (r != r2) throw new StateRejectedException();
        return r;
    }

    @Override
    public void clear() {
        map.clear();
        mapSupplied.clear();
        if (map.size() != mapSupplied.size()) throw new StateRejectedException();
    }

    @Override
    public boolean remove(Object o) {
        boolean r = map.remove(o);
        boolean r2 = mapSupplied.remove(o);
        if (r != r2) throw new StateRejectedException();
        return r;
    }

    @Override
    public Iterator<State> iterator() {
        return map.iterator();
    }

    public static class StateRejectedException extends RuntimeException {
        public StateRejectedException() {
            super();
        }

        public StateRejectedException(String message) {
            super(message);
        }
    }
}
