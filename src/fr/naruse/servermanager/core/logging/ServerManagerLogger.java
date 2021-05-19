package fr.naruse.servermanager.core.logging;

import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerManagerLogger {

    private static java.util.logging.Logger PLUGIN_LOGGER;
    public static void info(String msg){
        log(Level.INFO, msg);
    }

    public static void warn(String msg){
        log(Level.WARNING, msg);
    }

    public static void error(String msg){
        log(Level.SEVERE, msg);
    }

    public static void log(Level level, String msg){
        log(level, msg, null);
    }

    public static void log(Level level, String msg, Logger logger){
        if(PLUGIN_LOGGER != null){
            PLUGIN_LOGGER.log(level, (logger != null ? "["+ logger.getTag()+"] " : "")+msg);
        }else{
            Date date = Date.from(Instant.now());

            StringBuilder stringBuilder = new StringBuilder("[")
                    .append(date.getHours())
                    .append(":")
                    .append(date.getMinutes())
                    .append(":")
                    .append(date.getSeconds())
                    .append("] [")
                    .append(Thread.currentThread().getName())
                    .append(" | ")
                    .append(level.getName())
                    .append("]"+(logger == null ? ": " : " ["+ logger.getTag()+"]: "))
                    .append(msg);
            if(level == Level.SEVERE){
                System.err.println(stringBuilder);
            }else{
                System.out.println(stringBuilder);
            }
        }
    }

    public static void load(java.util.logging.Logger logger) {
        PLUGIN_LOGGER = logger;
    }

    public static class Logger {

        private final String tag;

        public Logger(String tag) {
            this.tag = tag;
        }

        public void info(String msg){
            this.log(Level.INFO, msg);
        }

        public void warn(String msg){
            this.log(Level.WARNING, msg);
        }

        public void error(String msg){
            this.log(Level.SEVERE, msg);
        }

        public void log(Level level, String msg){
            ServerManagerLogger.log(level, msg, this);
        }

        public String getTag() {
            return tag;
        }
    }
}
