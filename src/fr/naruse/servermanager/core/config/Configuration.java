package fr.naruse.servermanager.core.config;

import fr.naruse.servermanager.core.utils.Utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Configuration {

    private final File file;
    private final String defaultResourceName;
    private final boolean loadDefaultResource;
    private Map<String, Object> map = new HashMap<>();

    public Configuration(File file) {
        this(file, true);
    }

    public Configuration(File file, boolean loadDefaultResource) {
        this(file, file.getName(), loadDefaultResource);
    }

    public Configuration(File file, String defaultResourceName) {
        this(file, defaultResourceName, true);
    }

    private InputStream defaultResourceStream;
    public Configuration(File file, InputStream defaultResourceStream) {
        this.file = file;
        this.defaultResourceName = "";
        this.loadDefaultResource = true;
        this.defaultResourceStream = defaultResourceStream;

        ConfigurationManager.LOGGER.info("Loading '"+file.getName()+"'...");
        try {
            this.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConfigurationManager.LOGGER.info("'"+file.getName()+"' loaded");
    }

    public Configuration(File file, String defaultResourceName, boolean loadDefaultResource) {
        this.file = file;
        this.defaultResourceName = defaultResourceName;
        this.loadDefaultResource = loadDefaultResource;

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

        if((this.map == null || this.map.isEmpty()) && this.loadDefaultResource){
            InputStream inputStream = this.defaultResourceStream != null ? this.defaultResourceStream : getClass().getClassLoader().getResourceAsStream("resources/"+this.defaultResourceName);
            if(inputStream != null){
                reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();
                FileWriter fileWriter = new FileWriter(file);

                reader.lines().forEach(s -> {
                    stringBuilder.append(s);
                });

                map = Utils.GSON.fromJson(stringBuilder.toString(), Utils.MAP_TYPE);
                if(map != null){
                    this.map = map;
                }
                fileWriter.write(Utils.GSON.toJson(map));

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

    public boolean contains(String path){
        return this.map.containsKey(path);
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
        private ConfigurationSection section;

        public ConfigurationSection(ConfigurationSection section, String initialPath) {
            this(initialPath);
            this.section = section;
        }

        public ConfigurationSection(String initialPath) {
            this.initialPath = initialPath;
        }

        public <T> T get(String path){
            if(this.section != null){
                return (T) (((Map<String, Object>) section.get(initialPath)).get(path));
            }
            return (T) (((Map<String, Object>) map.get(initialPath)).get(path));
        }

        public void set(String path, Object o){
            if(this.section != null){
                ((Map<String, Object>) section.get(initialPath)).put(path, o);
            }else{
                ((Map<String, Object>) map.get(initialPath)).put(path, o);
            }
        }

        public boolean contains(String path){
            if(this.section != null){
                return ((Map<String, Object>) section.get(initialPath)).containsKey(path);
            }
            return ((Map<String, Object>) map.get(initialPath)).containsKey(path);
        }

        public ConfigurationSection getSection(String path){
            return new ConfigurationSection(this, path);
        }

        public Map<String, Object> getAll(){
            if(this.section != null){
                return section.get(initialPath);
            }
            return ((Map<String, Object>) map.get(initialPath));
        }


        public String getInitialPath() {
            return initialPath;
        }
    }
}
