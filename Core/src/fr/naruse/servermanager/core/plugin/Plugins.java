package fr.naruse.servermanager.core.plugin;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.api.config.Configuration;
import fr.naruse.api.logging.GlobalLogger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.jar.*;
import java.util.stream.Collectors;

public class Plugins extends ClassLoader {

    public static final String SM_PLUGIN_KEY = "smPlugins";
    private static final GlobalLogger.Logger LOGGER = new GlobalLogger.Logger("PluginLoader");
    private static final File PLUGIN_FOLDER = new File("plugins");
    private static final Set<SMPlugin> plugins = new HashSet<>();

    static {
        PLUGIN_FOLDER.mkdirs();
    }

    public static void loadPlugins(){
        LOGGER.info("Loading plugins...");
        if(PLUGIN_FOLDER.listFiles() != null){
            for (File file : PLUGIN_FOLDER.listFiles()) {
                if(file.getName().endsWith(".jar")){
                    SMPlugin smPlugin = getPlugin(file);
                    if(smPlugin == null){
                        continue;
                    }
                    smPlugin.init();
                }
            }
        }
        LOGGER.info(plugins.size()+" plugins loaded");
        ServerManager.get().getCurrentServer().getData().set(SM_PLUGIN_KEY, getAll().stream().map(new Function<SMPlugin, String>() {
            @Override
            public String apply(SMPlugin smPlugin) {
                return smPlugin.getPluginName();
            }
        }).collect(Collectors.toList()));
    }

    public static void shutdownPlugins(){
        for (SMPlugin plugin : plugins) {
            plugin.shutdown();
        }
    }

    public static void fireEvent(IEvent event) {
        for (SMPlugin plugin : new HashSet<>(plugins)) {
            plugin.handlePluginEvent(event);
        }
    }

    private static SMPlugin getPlugin(File file){
        try {
            JarFile jar = new JarFile(file, true);
            JarEntry entry = jar.getJarEntry("plugin.json");

            if (entry == null) {
                throw new FileNotFoundException("Jar does not contain plugin.yml");
            }

            InputStream stream = jar.getInputStream(entry);
            InputStreamReader streamReader;
            BufferedReader reader = new BufferedReader(streamReader = new InputStreamReader(stream));
            String json = reader.lines().collect(Collectors.joining());

            reader.close();
            streamReader.close();
            stream.close();

            Configuration configuration = new Configuration(json);
            String mainClassURL = configuration.get("main");
            String name = configuration.get("name");

            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()}, Plugins.class.getClassLoader());

            Class mainClass = Class.forName(mainClassURL, true, classLoader);
            Class<? extends SMPlugin> smPluginClass = mainClass.asSubclass(SMPlugin.class);

            File dataFolder = new File(file.getParentFile(), name);
            dataFolder.mkdirs();

            SMPlugin plugin = smPluginClass.getConstructor(String.class, File.class).newInstance(name, dataFolder);

            plugins.add(plugin);

            LOGGER.info("Plugin '"+name+"' loaded'");
            return plugin;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static Set<SMPlugin> getAll() {
        return new HashSet<>(plugins);
    }
}
