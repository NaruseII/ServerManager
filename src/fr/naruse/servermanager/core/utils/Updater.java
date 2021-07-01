package fr.naruse.servermanager.core.utils;

import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Updater {

    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("Updater");

    public static boolean needToUpdate(CoreServerType coreServerType){
        String currentVersion = ServerManager.VERSION;
        LOGGER.info("=-------------------------------------------------------------=");
        LOGGER.info("Starting...");

        String url = "https://raw.githubusercontent.com/NaruseII/ServerManager-Updater/main/version.json";
        String json = readStringFromURL(url);

        if(json == null){
            LOGGER.error("Couldn't check version on URL '"+url+"'! Did you prevent my request ?");
            LOGGER.error("If this is intentional, it's a big mistake! Updates on ServerManager mean bug fixes, improvements and optimisation. You shouldn't miss that!");
            LOGGER.warn("Authorising start on Updater error...");
            LOGGER.info("=-------------------------------------------------------------=");
            return false;
        }

        Configuration configuration = new Configuration(json);
        String version = configuration.get("latestVersion");


        LOGGER.info("Current version is '"+currentVersion+"'");

        boolean b = false;
        LOGGER.info("Online version is '"+version+"'");
        if(version.equals(currentVersion)){
            LOGGER.info("ServerManager is up to date!");
        }else{
            b = true;
            LOGGER.warn("A new version has been found! Please make sure to update all Server-Manager!");

            Configuration.ConfigurationSection acceptStartSection = configuration.getSection("acceptStart");
            Configuration.ConfigurationSection notUpToDateSection = configuration.getSection("notUpToDate");
            Configuration.ConfigurationSection sleepSection = notUpToDateSection.getSection("sleep");

            if(coreServerType.is(CoreServerType.PACKET_MANAGER) && (boolean) acceptStartSection.get("packet-manager")){
                b = false;
            }
            if(coreServerType.is(CoreServerType.FILE_MANAGER) && (boolean) acceptStartSection.get("file-manager")){
                b = false;
            }
            if(coreServerType.is(CoreServerType.BUKKIT_MANAGER) && (boolean) acceptStartSection.get("bukkit-manager")){
                b = false;
            }
            if(coreServerType.is(CoreServerType.BUNGEE_MANAGER) && (boolean) acceptStartSection.get("bungee-manager")){
                b = false;
            }
            if(coreServerType.is(CoreServerType.SPONGE_MANAGER) && (boolean) acceptStartSection.get("sponge-manager")){
                b = false;
            }
            if(coreServerType.is(CoreServerType.VELOCITY_MANAGER) && (boolean) acceptStartSection.get("velocity-manager")){
                b = false;
            }

            List<String> list = notUpToDateSection.get("bypassForVersion");
            if(list != null && !list.isEmpty() && list.contains(currentVersion)){
                b = false;
                LOGGER.warn("Your current version has a bypass and can start anyway, but remember to update Server-Manager!");
            }

            if(b){
                LOGGER.warn("Preventing start. I won't allow you to start before you update me!");
            }

            LOGGER.info("");
            List<String> downloadURLs = notUpToDateSection.get("downloadUrls");
            if(downloadURLs != null && !downloadURLs.isEmpty()){
                LOGGER.info("Donwload URLs: (Don't use CTRL+C to copy! Use RIGHT-CLICK!)");
                for (String downloadURL : downloadURLs) {
                    LOGGER.info("    - "+downloadURL);
                }
            }

            LOGGER.info("");
            LOGGER.info("Support URL: (Don't use CTRL+C to copy! Use RIGHT-CLICK!)");
            LOGGER.info("    - "+notUpToDateSection.get("support"));

            LOGGER.info("");
            if((boolean) sleepSection.get("enabled") && !b){
                try {
                    int duration = Math.abs(sleepSection.getInt("duration"));
                    LOGGER.info("Waiting "+ TimeUnit.MILLISECONDS.toSeconds(duration)+" seconds before starting...");
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        if(!b){
            LOGGER.info("Authorising start...");
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
