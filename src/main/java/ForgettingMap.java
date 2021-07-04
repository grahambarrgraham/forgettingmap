import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

/**
 * Notes :
 * 1. Keeping to semantics of Java Map/AbstractMap with intent to extend AbstractMap, but extra effort
 * 2. Worst-case O(1) get and O(n) put, with O(1) put if under capacity or lowest is known.  Preferred to maintaining
 * TreeSet with O(log n) for get and put.
 * 3. Has edge-case on a tie-breaker, if most recently added key has not been fetched, it will be preferred for eviction.
 * 4. NOT thread-safe as-is, intent to extend AbstractMap and wrap with Collections.synchronizedMap, or make get and put synchronized
 */
public class ForgettingMap<K, V> {

    private final HashMap<K, AccessCountingWrapper<V>> wrappedMap;
    private final Comparator<Map.Entry<K, V>> tieBreakingComparator;
    private final int maxCapacity;
    private K currentLowest;

    /**
     * @see AbstractMap
     * @param capacity - maximum size of the map before least fetched are dropped.  Should be multiple of 2 to as this
     *                 value also configures the underlying hashmap capacity
     * @param tieBreakingComparator - applied if there's more than one key with the least number of fetches, and those keys have been fetched at least once
     */
    public ForgettingMap(int capacity, Comparator<Map.Entry<K, V>> tieBreakingComparator) {
        this.maxCapacity = capacity;
        this.wrappedMap = new HashMap<>(capacity, 1);
        this.tieBreakingComparator = tieBreakingComparator;
    }


    /**
     * @see AbstractMap#get(Object)
     */
    public V get(K key) {
        var wrapper = wrappedMap.get(key);
        if (key.equals(currentLowest)) {
            invalidateLowest();
        }
        return wrapper == null ? null : wrapper.incrementAndGet();
    }

    /**
     * @see AbstractMap#put(Object, Object)
     */
    public V put(K key, V value) {

        if (wrappedMap.size() >= maxCapacity && !wrappedMap.containsKey(key)) {
            removeLeastAccessedKey();
            currentLowest = key;
        }

        var wrapper =
                wrappedMap.putIfAbsent(key, new AccessCountingWrapper<V>(value, 0));

        if (wrapper != null) {
            V previousValue = wrapper.getValue();
            wrapper.setValue(value);
            return previousValue;
        } else {
            return null;
        }
    }

    public int size() {
        return wrappedMap.size();
    }

    private void invalidateLowest() {
        currentLowest = null;
    }

    private void removeLeastAccessedKey() {
        if (currentLowest == null) {
            wrappedMap.entrySet()
                    .stream()
                    .min(comparator()) //assuming here that min is O(n)
                    .map(Map.Entry::getKey)
                    .map(k -> wrappedMap.remove(k));
        } else {
            wrappedMap.remove(currentLowest);
            invalidateLowest();
        }
    }

    private Comparator<Map.Entry<K, AccessCountingWrapper<V>>> comparator() {
        return Comparator.<Map.Entry<K, AccessCountingWrapper<V>>>comparingInt(o -> o.getValue().getCount())
                .thenComparing((o1, o2) -> tieBreakingComparator.compare(entry(o1), entry(o2)));
    }

    private AbstractMap.SimpleEntry<K, V> entry(Map.Entry<K, AccessCountingWrapper<V>> o1) {
        //note, it is not efficient to construct temporary objects in the context of a sort, needs rework
        return new AbstractMap.SimpleEntry<K, V>(o1.getKey(), o1.getValue().getValue());
    }

    @AllArgsConstructor
    @Data
    private static class AccessCountingWrapper<V> {
        private V value;
        private int count = 0;

        V incrementAndGet() {
            count++;
            return value;
        }
    }
}

