package fr.naruse.servermanager.core.logging;

import cn.nukkit.plugin.PluginLogger;

import java.util.logging.Level;

public class NukkitCustomLogger extends ServerManagerLogger.CustomLogger{

    private final PluginLogger logger;

    public NukkitCustomLogger(PluginLogger logger) {
        this.logger = logger;
    }

    @Override
    public String log(Level level, String msg, ServerManagerLogger.Logger tag) {
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
