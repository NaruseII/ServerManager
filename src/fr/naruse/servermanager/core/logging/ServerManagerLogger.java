package fr.naruse.servermanager.core.logging;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.utils.Utils;

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
    private static final Color colors = new Color(Attribute.NONE(), Attribute.YELLOW_TEXT(), Attribute.RED_TEXT(), Attribute.BRIGHT_GREEN_TEXT());
    private static java.util.logging.Logger PLUGIN_LOGGER;

    public static void setCustomLogger(CustomLogger customLogger) {
        ServerManagerLogger.customLogger = customLogger;
    }

    public static void info(String msg){
        info(null, msg);
    }

    public static void info(Attribute attribute, String msg){
        log(Level.INFO, msg, attribute);
    }

    public static void warn(String msg){
        warn(null, msg);
    }

    public static void warn(Attribute attribute, String msg){
        log(Level.WARNING, msg, attribute);
    }

    public static void error(String msg){
        error(null, msg);
    }

    public static void error(Attribute attribute, String msg){
        log(Level.SEVERE, msg, attribute);
    }

    public static void debug(String msg){
        debug(null, msg);
    }

    public static void debug(Attribute attribute, String msg){
        log(Level.OFF, msg, attribute);
    }

    public static void log(Level level, String msg){
        log(level, msg, null, null);
    }

    public static void log(Level level, String msg, Attribute attribute){
        log(level, msg, null, attribute);
    }

    public static void log(Level level, String msg, Logger logger){
        log(level, msg, logger, null);
    }

    public static void log(Level level, String msg, Logger logger, Attribute attribute){
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

            boolean hideTimeAndThread = false;
            String tag = "";
            if(logger != null){
                hideTimeAndThread = logger.hideTimeAndThread;
                if(logger.getAttribute() == null){
                    tag += "["+ logger.getTag()+"]";
                }else{
                    tag += Ansi.colorize("["+ logger.getTag()+"]", logger.getAttribute());
                }
                tag += ": ";
            }

            StringBuilder stringBuilder = new StringBuilder();
            if(!hideTimeAndThread){
                stringBuilder.append("[")
                        .append(String.valueOf(date.getHours()).length() == 1 ? "0"+date.getHours() : date.getHours())
                        .append(":")
                        .append(String.valueOf(date.getMinutes()).length() == 1 ? "0"+date.getMinutes() : date.getMinutes())
                        .append(":")
                        .append(String.valueOf(date.getSeconds()).length() == 1 ? "0"+date.getSeconds() : date.getSeconds())
                        .append("] [")
                        .append(Thread.currentThread().getName())
                        .append(" | ")
                        .append(level.getName())
                        .append("]");
            }
            stringBuilder.append(hideTimeAndThread ? tag : ": "+tag)
                    .append(color(level, msg));

            String colored = attribute == null ? color(level, stringBuilder.toString()) : Ansi.colorize(stringBuilder.toString(), attribute);
            if(level == Level.SEVERE){
                System.err.println(colored);
            }else{
                System.out.println(colored);
            }

            LOGS_BUILDER.append(stringBuilder).append("\n");
        }
    }

    public static void loadPluginLogger(java.util.logging.Logger logger) {
        PLUGIN_LOGGER = logger;
    }

    public static void loadConfigData(){
        new ServerManagerLogger.Logger("Logger").info("Changing parameters...");
        Configuration configuration = ServerManager.get().getConfigurationManager().getConfig();

        Configuration.ConfigurationSection section = configuration.getSection("console");
        debug = configuration.get("debug");

        Attribute defaultAttribute = (Attribute) Utils.getStaticField(Attribute.class, section.get("defaultColor"));
        if(defaultAttribute != null){
            colors.setDefaultAttribute(defaultAttribute);
        }

        Attribute warningAttribute = (Attribute) Utils.getStaticField(Attribute.class, section.get("warningColor"));
        if(warningAttribute != null){
            colors.setWarningAttribute(warningAttribute);
        }

        Attribute errorAttribute = (Attribute) Utils.getStaticField(Attribute.class, section.get("errorColor"));
        if(errorAttribute != null){
            colors.setErrorAttribute(errorAttribute);
        }

        Attribute debugAttribute = (Attribute) Utils.getStaticField(Attribute.class, section.get("debugColor"));
        if(debugAttribute != null){
            colors.setDebugAttribute(debugAttribute);
        }

        Ansi.setEnabled(section.get("enabled"));
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

    private static String color(Level level, String s){
        if(level == Level.WARNING){
            return Ansi.colorize(s, colors.warningAttribute);
        }else if(level == Level.SEVERE){
            return Ansi.colorize(s, colors.errorAttribute);
        }else if(level == Level.OFF){
            return Ansi.colorize(s, colors.debugAttribute);
        }
        if(colors.defaultAttribute.toString().equals(Attribute.NONE().toString())){
            return s;
        }else{
            return Ansi.colorize(s, colors.defaultAttribute);
        }
    }

    public static boolean isDebugMode(){
        return debug;
    }

    public static class Logger {

        private String tag;
        private Attribute attribute;
        private boolean hideTimeAndThread = false;

        public Logger(String tag) {
            this(tag, false);
        }

        public Logger(String tag, boolean hideTimeAndThread) {
            this(tag, null, hideTimeAndThread);
        }

        public Logger(String tag, Attribute attribute) {
            this(tag, attribute, false);
        }

        public Logger(String tag, Attribute attribute, boolean hideTimeAndThread) {
            this.tag = tag;
            this.attribute = attribute;
            this.hideTimeAndThread = hideTimeAndThread;
        }

        public void info(String msg){
            this.log(Level.INFO, msg);
        }

        public void info(Attribute attribute, String msg){
            this.log(Level.INFO, msg, attribute);
        }

        public void warn(String msg){
            this.log(Level.WARNING, msg);
        }

        public void warn(Attribute attribute, String msg){
            this.log(Level.WARNING, msg, attribute);
        }

        public void error(String msg){
            this.log(Level.SEVERE, msg);
        }

        public void error(Attribute attribute, String msg){
            this.log(Level.SEVERE, msg, attribute);
        }

        public void debug(String msg){
            this.log(Level.OFF, msg);
        }

        public void debug(Attribute attribute, String msg){
            this.log(Level.OFF, msg, attribute);
        }

        public void log(Level level, String msg){
            log(level, msg, null);
        }

        public void log(Level level, String msg, Attribute attribute){
            ServerManagerLogger.log(level, msg, this, attribute);
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public void setAttribute(Attribute attribute) {
            this.attribute = attribute;
        }

        public void setHideTimeAndThread(boolean hideTimeAndThread) {
            this.hideTimeAndThread = hideTimeAndThread;
        }
    }

    public static abstract class CustomLogger {

        public abstract String log(Level level, String msg, Logger logger) ;

    }

    private static class Color {

        private Attribute defaultAttribute;
        private Attribute warningAttribute;
        private Attribute errorAttribute;
        private Attribute debugAttribute;

        public Color(Attribute defaultAttribute, Attribute warningAttribute, Attribute errorAttribute, Attribute debugAttribute) {
            this.defaultAttribute = defaultAttribute;
            this.warningAttribute = warningAttribute;
            this.errorAttribute = errorAttribute;
            this.debugAttribute = debugAttribute;
        }

        public void setDebugAttribute(Attribute debugAttribute) {
            this.debugAttribute = debugAttribute;
        }

        public void setDefaultAttribute(Attribute defaultAttribute) {
            this.defaultAttribute = defaultAttribute;
        }

        public void setErrorAttribute(Attribute errorAttribute) {
            this.errorAttribute = errorAttribute;
        }

        public void setWarningAttribute(Attribute warningAttribute) {
            this.warningAttribute = warningAttribute;
        }
    }
}
