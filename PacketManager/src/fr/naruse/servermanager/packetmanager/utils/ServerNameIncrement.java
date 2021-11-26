package fr.naruse.servermanager.packetmanager.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerNameIncrement {

    private static final Map<String, Integer> baseNameCountMap = new ConcurrentHashMap<>();

    public static String findNewName(String baseName) {
        int newCount;
        if(!baseNameCountMap.containsKey(baseName)){
            baseNameCountMap.put(baseName, newCount = 1);
        }else{
            newCount = baseNameCountMap.get(baseName)+1;
            baseNameCountMap.put(baseName, newCount);
        }
        return baseName+"-"+newCount;
    }
}
