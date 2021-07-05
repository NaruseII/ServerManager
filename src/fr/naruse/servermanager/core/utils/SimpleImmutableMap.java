package fr.naruse.servermanager.core.utils;

import java.util.HashMap;

public class SimpleImmutableMap<K, V> extends HashMap<K, V> {

    @Override
    public V put(K key, V value) {
        if(containsKey(key)){
            return get(key);
        }
        return super.put(key, value);
    }
}
