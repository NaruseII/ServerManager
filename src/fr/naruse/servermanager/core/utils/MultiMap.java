package fr.naruse.servermanager.core.utils;

import java.util.HashMap;
import java.util.Map;

public class MultiMap<K, V> extends HashMap<K, V> {

    private final Map<V, K> reversedMap = new HashMap<>();

    @Override
    public V put(K key, V value) {
        this.reversedMap.put(value, key);
        super.put(key, value);
        return value;
    }

    @Override
    public void clear() {
        this.reversedMap.clear();
        super.clear();
    }

    public Map<V, K> reverse() {
        return this.reversedMap;
    }
}
