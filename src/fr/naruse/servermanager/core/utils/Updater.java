package fr.naruse.servermanager.core.utils;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class Updater {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("Updater");

    public static boolean needToUpdate(){
        String currentVersion = ServerManager.VERSION;
        LOGGER.info("=-------------------------------------------------------------=");
        LOGGER.info("Starting...");

        String url = "https://raw.githubusercontent.com/NaruseII/ServerManager-Updater/main/version.txt";
        String version = readStringFromURL(url);

        LOGGER.info("Current version is '"+currentVersion+"'");

        boolean b = false;

        if(version == null){
            LOGGER.error("Couldn't check version on URL '"+url+"'! Did you prevent my request ?");
            LOGGER.error("If this is intentional, it's a big mistake! Updates on ServerManager mean bug fixes, improvements and optimisation. You shouldn't miss that!");
            LOGGER.warn("Authorising start on Updater error...");
        }else{
            LOGGER.info("Online version is '"+version+"'");
            if(version.equals(currentVersion)){
                LOGGER.info("ServerManager is up to date!");
                LOGGER.info("Authorising start...");
            }else{
                b = true;
                LOGGER.warn("A new version has been found! Please make sure to update all Server-Manager!");
                LOGGER.warn("Preventing start. I won't allow you to start File-Manager and Packet-Manager before you update them!");
            }
        }

        LOGGER.info("=-------------------------------------------------------------=");
        return b;
    }


    private static String readStringFromURL(String stringUrl){
        try {
            URL url = new URL(stringUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                StringBuilder builder = new StringBuilder();
                try (Scanner scanner = new Scanner(inputStream)) {
                    while (scanner.hasNext()) {
                        builder.append(scanner.nextLine());
                    }
                }
                return builder.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
