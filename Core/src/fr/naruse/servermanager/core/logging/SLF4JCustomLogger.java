package fr.naruse.servermanager.core.logging;

import fr.naruse.api.logging.GlobalLogger;
import org.slf4j.Logger;

import java.util.logging.Level;

public class SLF4JCustomLogger extends GlobalLogger.CustomLogger {

    private final Logger logger;

    public SLF4JCustomLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String log(Level level, String msg, GlobalLogger.Logger tag) {
        String s = (tag != null ? "["+ tag.getTag()+"] " : "")+msg;

        if(level == Level.WARNING){
            logger.warn(s);
        }else if(level == Level.SEVERE){
            logger.error(s);
        }else if(level == Level.OFF){
            logger.debug(s);
        }else{
            logger.info(s);
        }

        return s;
    }
}
