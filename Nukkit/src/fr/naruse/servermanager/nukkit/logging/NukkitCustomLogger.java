package fr.naruse.servermanager.nukkit.logging;

import cn.nukkit.plugin.PluginLogger;
import fr.naruse.api.logging.GlobalLogger;

import java.util.logging.Level;

public class NukkitCustomLogger extends GlobalLogger.CustomLogger {

    private final PluginLogger logger;

    public NukkitCustomLogger(PluginLogger logger) {
        this.logger = logger;
    }

    @Override
    public String log(Level level, String msg, GlobalLogger.Logger tag) {
        String s = (tag != null ? "["+ tag.getTag()+"] " : "")+msg;

        if(level == Level.WARNING){
            logger.alert(s);
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
