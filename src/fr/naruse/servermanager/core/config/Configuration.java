package fr.naruse.servermanager.core.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.naruse.servermanager.core.Utils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Configuration {

    private final File file;
    private final String defaultResourceName;
    private Map<String, Object> map = new HashMap<>();

    public Configuration(File file) {
        this(file, file.getName());
    }

    public Configuration(File file, String defaultResourceName) {
        this.file = file;
        this.defaultResourceName = defaultResourceName;

        ConfigurationManager.LOGGER.info("Loading '"+file.getName()+"'...");
        try {
            this.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConfigurationManager.LOGGER.info("'"+file.getName()+"' loaded");
    }

    public void reload() throws IOException {
        if(!this.file.exists()){
            this.file.getParentFile().mkdirs();
            this.file.createNewFile();
        }

        BufferedReader reader = new BufferedReader(new FileReader(this.file));
        Map<String, Object> map = Utils.GSON.fromJson(reader.lines().collect(Collectors.joining()), Utils.MAP_TYPE);
        if(map != null){
            this.map = map;
        }
        reader.close();

        if(this.map == null || this.map.isEmpty()){
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("resources/"+this.defaultResourceName);
            if(inputStream != null){
                reader = new BufferedReader(new InputStreamReader(inputStream));

                FileWriter fileWriter = new FileWriter(file);
                reader.lines().forEach(s -> {
                    try {
                        fileWriter.write(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                map = Utils.GSON.fromJson(reader.lines().collect(Collectors.joining()), Utils.MAP_TYPE);
                if(map != null){
                    this.map = map;
                }
                fileWriter.close();
                reader.close();
                inputStream.close();
            }
        }
    }

    public <T> T get(String path){
        return (T) this.map.get(path);
    }

    public void set(String path, Object o){
        this.map.put(path, o);
    }

    public ConfigurationSection getSection(String path){
        return new ConfigurationSection(path);
    }

    public void save(){
        try{
            String json = Utils.GSON.toJson(this.map);
            FileWriter fileWriter = new FileWriter(this.file);
            fileWriter.write(json);
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public File getConfigFile() {
        return file;
    }


    public class ConfigurationSection {

        private final String initialPath;

        public ConfigurationSection(String initialPath) {
            this.initialPath = initialPath;
        }

        public <T> T get(String path){
            return (T) (((Map<String, Object>) map.get(initialPath)).get(path));
        }

        public void set(String path, Object o){
            ((Map<String, Object>) map.get(initialPath)).put(path, o);
        }
    }
}
