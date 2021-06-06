package fr.naruse.servermanager.core.logging;

import fr.naruse.servermanager.core.ServerManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;

public class ServerManagerLogger {

    public static final StringBuilder LOGS_BUILDER = new StringBuilder();
    private static boolean debug = false;
    private static CustomLogger customLogger;

    public static void setCustomLogger(CustomLogger customLogger) {
        ServerManagerLogger.customLogger = customLogger;
    }

    public static void setDebug(boolean debug) {
        ServerManagerLogger.debug = debug;
    }

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

    public static void debug(String msg){
        log(Level.OFF, msg);
    }

    public static void log(Level level, String msg){
        log(level, msg, null);
    }

    public static void log(Level level, String msg, Logger logger){
        if(level == Level.OFF){
            if(debug){
                level = Level.INFO;
            }else{
                return;
            }
        }

        if(customLogger != null){
            LOGS_BUILDER.append(customLogger.log(level, msg, logger)).append("\n");
            return;
        }

        if(PLUGIN_LOGGER != null){
            String s = (logger != null ? "["+ logger.getTag()+"] " : "")+msg;
            PLUGIN_LOGGER.log(level, s);
            LOGS_BUILDER.append(s).append("\n");
        }else{
            Date date = Date.from(Instant.now());

            StringBuilder stringBuilder = new StringBuilder("[")
                    .append(String.valueOf(date.getHours()).length() == 1 ? "0"+date.getHours() : date.getHours())
                    .append(":")
                    .append(String.valueOf(date.getMinutes()).length() == 1 ? "0"+date.getMinutes() : date.getMinutes())
                    .append(":")
                    .append(String.valueOf(date.getSeconds()).length() == 1 ? "0"+date.getSeconds() : date.getSeconds())
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
            LOGS_BUILDER.append(stringBuilder).append("\n");
        }
    }

    public static void load(java.util.logging.Logger logger) {
        PLUGIN_LOGGER = logger;
    }

    public static void saveLogs() {
        File file = new File(ServerManager.get().getCoreData().getDataFolder(), "latest.log");
        if(file.exists()){
            file.delete();
        }else{
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            writer.write(LOGS_BUILDER.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Logger {

        private String tag;

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

        public void debug(String msg){
            this.log(Level.OFF, msg);
        }

        public void log(Level level, String msg){
            ServerManagerLogger.log(level, msg, this);
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public static abstract class CustomLogger {

        public abstract String log(Level level, String msg, Logger logger) ;

    }
}
