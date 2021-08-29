package fr.naruse.servermanager.core.config;

import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Configuration {

    private File file;
    private String defaultResourceName;
    private boolean loadDefaultResource;
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

    private String json;
    public Configuration(String json) {
        this.json = json;

        try {
            this.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration(Map<String, Object> map) {
        this(Utils.GSON.toJson(map));
    }

    private InputStream defaultResourceStream;
    public Configuration(File file, InputStream defaultResourceStream) {
        this.file = file;
        this.defaultResourceName = "";
        this.loadDefaultResource = true;
        this.defaultResourceStream = defaultResourceStream;

        ConfigurationManager.LOGGER.info("Loading '"+file.getName()+"'...");
        for (int i = 0; i < 5; i++) {
            try {
                this.reload();
                break;
            } catch (Exception e) {
                currentCharset++;
                if(ServerManagerLogger.isDebugMode()){
                    e.printStackTrace();
                }
            }
        }
        ConfigurationManager.LOGGER.info("'"+file.getName()+"' loaded");
    }

    public Configuration(File file, String defaultResourceName, boolean loadDefaultResource) {
        this.file = file;
        this.defaultResourceName = defaultResourceName;
        this.loadDefaultResource = loadDefaultResource;

        ConfigurationManager.LOGGER.info("Loading '"+file.getName()+"'...");
        for (int i = 0; i < 5; i++) {
            try {
                this.reload();
                break;
            } catch (Exception e) {
                currentCharset++;
                if(ServerManagerLogger.isDebugMode()){
                    e.printStackTrace();
                }
            }
        }
        ConfigurationManager.LOGGER.info("'"+file.getName()+"' loaded");
    }

    public void reload() throws IOException {
        if(this.json != null){
            this.map = Utils.GSON.fromJson(json, Utils.MAP_TYPE);
            return;
        }

        if(!this.file.exists()){
            this.file.getParentFile().mkdirs();
            this.file.createNewFile();
        }

        Charset charset = charset();

        Map<String, Object> map = Utils.GSON.fromJson(Files.lines(Paths.get(file.toURI()), charset).collect(Collectors.joining()), Utils.MAP_TYPE);
        if(map != null){
            this.map = map;
        }

        if((this.map == null || this.map.isEmpty()) && this.loadDefaultResource){
            InputStream inputStream = this.defaultResourceStream != null ? this.defaultResourceStream : Configuration.class.getClassLoader().getResourceAsStream("resources/"+this.defaultResourceName);
            if(inputStream != null){
                InputStreamReader inputStreamReader;
                BufferedReader reader = new BufferedReader(inputStreamReader = new InputStreamReader(inputStream, charset));

                FileOutputStream fileOutputStream;
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream = new FileOutputStream(file), StandardCharsets.UTF_8);

                String json = reader.lines().collect(Collectors.joining());

                map = Utils.GSON.fromJson(json, Utils.MAP_TYPE);
                if(map != null){
                    this.map = map;
                }

                writer.write(Utils.GSON.toJson(map));

                writer.close();
                reader.close();
                inputStream.close();
                inputStreamReader.close();
                fileOutputStream.close();
            }
        }

        this.fill();
    }

    public void fill() throws IOException {
        if(!this.loadDefaultResource) {
            return;
        }
        InputStream inputStream = this.defaultResourceStream != null ? this.defaultResourceStream : Configuration.class.getClassLoader().getResourceAsStream("resources/"+this.defaultResourceName);
        if(inputStream == null) {
            return;
        }
        InputStreamReader inputStreamReader;
        BufferedReader reader = new BufferedReader(inputStreamReader = new InputStreamReader(inputStream, charset()));

        String json = reader.lines().collect(Collectors.joining());

        Map<String, Object> resourceMap = Utils.GSON.fromJson(json, Utils.MAP_TYPE);

        if(this.map != null){
            this.fillMap(this.map, resourceMap);
        }

        reader.close();
        inputStream.close();
        inputStreamReader.close();

        this.save();
    }

    private void fillMap(Map<String, Object> map, Map<String, Object> resourceMap){
        for (String key : resourceMap.keySet()) {
            if(!map.containsKey(key)){
                map.put(key, resourceMap.get(key));
            }else if(resourceMap.get(key) instanceof Map){
                fillMap((Map<String, Object>) map.get(key), (Map<String, Object>) resourceMap.get(key));
            }
        }
    }

    public <T> T get(String path){
        return (T) this.map.get(path);
    }

    public int getInt(String path){
        Object o = get(path);
        if(o instanceof Double && (o.toString().endsWith(".0") || !o.toString().contains("."))){
            int i = (int) (double) o;
            o = i;
        }
        return (int) o;
    }

    public long getLong(String path){
        Object o = get(path);
        if(o instanceof Double && (o.toString().endsWith(".0") || !o.toString().contains("."))){
            long i = (long) (double) o;
            o = i;
        }
        return (long) o;
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

    public ConfigurationSectionMain getMainSection(){
        return new ConfigurationSectionMain(this);
    }

    public void save(File file){
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            if(this.json != null){
                fileWriter.write(this.json);
            }else{
                String json = Utils.GSON.toJson(this.map);
                fileWriter.write(json);
            }
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void save(){
        this.save(this.file);
    }

    public File getConfigFile() {
        return file;
    }

    private int currentCharset = 0;
    private Charset charset(){
        switch (currentCharset){
            case 0:
                return StandardCharsets.UTF_8;
            case 1:
                return StandardCharsets.ISO_8859_1;
            case 2:
                return StandardCharsets.US_ASCII;
            case 3:
                return StandardCharsets.UTF_16;
            case 4:
                return StandardCharsets.UTF_16BE;
            case 5:
                return StandardCharsets.UTF_16LE;
        }
        return null;
    }

    public String toJson() {
        return Utils.GSON.toJson(this.map);
    }


    public class ConfigurationSection {

        protected String initialPath;
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

        public int getInt(String path){
            if(this.section != null){
                return (int) (double) ((Map<String, Object>) section.get(initialPath)).get(path);
            }
            return (int) (double) ((Map<String, Object>) map.get(initialPath)).get(path);
        }

        public long getLong(String path){
            if(this.section != null){
                return (long) (double) ((Map<String, Object>) section.get(initialPath)).get(path);
            }
            return (long) (double) ((Map<String, Object>) map.get(initialPath)).get(path);
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

        public List<Configuration> getSectionList(String path){
            List<Configuration> list = new ArrayList<>();
            Object sectionObj = this.get(path);
            if(sectionObj instanceof ArrayList){
                ((ArrayList<Map<String, Object>>) sectionObj).forEach(hashMap -> list.add(new Configuration(hashMap)));
            }else{
                return null;
            }
            return list;
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

    public class ConfigurationSectionMain extends ConfigurationSection {

        private final Configuration configuration;

        public ConfigurationSectionMain(Configuration configuration) {
            super(null, null);
            this.configuration = configuration;
        }

        @Override
        public <T> T get(String path) {
            return this.configuration.get(path);
        }

        @Override
        public int getInt(String path) {
            return this.configuration.getInt(path);
        }

        @Override
        public long getLong(String path) {
            return this.configuration.getLong(path);
        }

        @Override
        public void set(String path, Object o) {
            this.configuration.set(path, o);
        }

        @Override
        public boolean contains(String path) {
            return this.configuration.contains(path);
        }

        @Override
        public ConfigurationSection getSection(String path) {
            return this.configuration.getSection(path);
        }

        @Override
        public List<Configuration> getSectionList(String path) {
            return super.getSectionList(path);
        }

        public void setInitialPath(String newInitialPath){
            this.initialPath = newInitialPath;
        }
    }
}
