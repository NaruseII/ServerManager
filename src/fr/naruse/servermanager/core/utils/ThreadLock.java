package fr.naruse.servermanager.core.utils;

import fr.naruse.servermanager.core.connection.packet.AbstractPacketResponsive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadLock {

    private static final Map<Long, Thread> lockedMap = new ConcurrentHashMap<>();
    private static final Map<Thread, CustomRunnable> runnableMap = new ConcurrentHashMap<>();

    public static void lock(Thread thread, CustomRunnable runnable){
        if (lockedMap.containsKey(thread.getId())) {
            return;
        }
        lockedMap.put(thread.getId(), thread);
        runnableMap.put(thread, runnable);
        thread.suspend();
    }

    public static void unlock(AbstractPacketResponsive packet){
        Thread thread = lockedMap.remove(packet.getThreadId());
        if(thread != null){
            runnableMap.get(thread).run(packet);
            thread.resume();
        }
    }

}
